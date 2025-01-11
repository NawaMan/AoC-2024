package day9;

import static functionalj.list.FuncList.repeat;
import static functionalj.list.intlist.IntFuncList.infinite;
import static functionalj.stream.intstream.IntStreamPlus.loop;

import java.math.BigInteger;
import java.util.concurrent.atomic.LongAdder;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.function.Func;
import functionalj.function.FuncUnit3;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;
import functionalj.stream.intstream.IndexedInt;
import functionalj.tuple.Tuple3;
import functionalj.types.Struct;

/**
 * --- Day 9: Disk Fragmenter ---
 * 
 * Another push of the button leaves you in the familiar hallways of some friendly amphipods! Good thing you each 
 *   somehow got your own personal mini submarine. The Historians jet away in search of the Chief, mostly by driving 
 *   directly into walls.
 * 
 * While The Historians quickly figure out how to pilot these things, you notice an amphipod in the corner struggling 
 *   with his computer. He's trying to make more contiguous free space by compacting all of the files, but his program 
 *   isn't working; you offer to help.
 * 
 * He shows you the disk map (your puzzle input) he's already generated. For example:
 * 
 * 2333133121414131402
 * 
 * The disk map uses a dense format to represent the layout of files and free space on the disk. The digits alternate 
 *   between indicating the length of a file and the length of free space.
 * 
 * So, a disk map like 12345 would represent a one-block file, two blocks of free space, a three-block file, four blocks
 *   of free space, and then a five-block file. A disk map like 90909 would represent three nine-block files in a row 
 *   (with no free space between them).
 * 
 * Each file on disk also has an ID number based on the order of the files as they appear before they are rearranged, 
 *   starting with ID 0. So, the disk map 12345 has three files: a one-block file with ID 0, a three-block file with 
 *   ID 1, and a five-block file with ID 2. Using one character for each block where digits are the file ID and . is 
 *   free space, the disk map 12345 represents these individual blocks:
 * 
 * 0..111....22222
 * 
 * The first example above, 2333133121414131402, represents these individual blocks:
 * 
 * 00...111...2...333.44.5555.6666.777.888899
 * 
 * The amphipod would like to move file blocks one at a time from the end of the disk to the leftmost free space block 
 *   (until there are no gaps remaining between file blocks). For the disk map 12345, the process looks like this:
 * 
 * 0..111....22222
 * 02.111....2222.
 * 022111....222..
 * 0221112...22...
 * 02211122..2....
 * 022111222......
 * 
 * The first example requires a few more steps:
 * 
 * 00...111...2...333.44.5555.6666.777.888899
 * 009..111...2...333.44.5555.6666.777.88889.
 * 0099.111...2...333.44.5555.6666.777.8888..
 * 00998111...2...333.44.5555.6666.777.888...
 * 009981118..2...333.44.5555.6666.777.88....
 * 0099811188.2...333.44.5555.6666.777.8.....
 * 009981118882...333.44.5555.6666.777.......
 * 0099811188827..333.44.5555.6666.77........
 * 00998111888277.333.44.5555.6666.7.........
 * 009981118882777333.44.5555.6666...........
 * 009981118882777333644.5555.666............
 * 00998111888277733364465555.66.............
 * 0099811188827773336446555566..............
 * 
 * The final step of this file-compacting process is to update the filesystem checksum. To calculate the checksum, add 
 *   up the result of multiplying each of these blocks' position with the file ID number it contains. The leftmost block
 *   is in position 0. If a block contains free space, skip it instead.
 * 
 * Continuing the first example, the first few blocks' position multiplied by its file ID number are 
 *   0 * 0 = 0, 1 * 0 = 0, 2 * 9 = 18, 3 * 9 = 27, 4 * 8 = 32, and so on. In this example, the checksum is the sum of 
 *   these, 1928.
 * 
 * Compact the amphipod's hard drive using the process he requested. What is the resulting filesystem checksum? 
 *   (Be careful copy/pasting the input for this puzzle; it is a single, very long line.)
 * 
 * Your puzzle answer was 6216544403458.
 */
public class Day9Part1Test extends BaseTest {
    
    BigInteger calculateChecksum(FuncList<String> lines) {
        // [2, 3, 3, 3, 1, 3, 3, 1, 2, 1, 4, 1, 4, 1, 3, 1, 4, 0, 2]
        var line = IntFuncList.from(lines.get(0).chars()).map(i -> i - '0').cache();
        
        // [   0,    0, null, null, null,    1,    1,    1, null, null, null
        // ,   2, null, null, null,    3,    3,    3, null,    4,    4, null
        // ,   5,    5,    5,    5, null,    6,    6,    6,    6, null,    7
        // ,   7,    7, null,    8,    8,    8,    8,    9,    9
        // ]
        var filesystem 
                = infinite()
                .zipToObjWith(line, (id, length) -> repeat((id % 2) == 1 ? null : id/2).limit(length))
                .flatMap     (itself())
                .cache       ();
        
        // Get an iterator of the reverse of the file system so we can get its value out one-by-one.
        var reverseFs
                = filesystem
                .reverse()
                .excludeNull()
                .cache()
                .iterable()
                .iterator();
        
        // Calculate the used and empty space to know when to stop.
        int usedSpace
                = loop (line.size())
                .filter(theInt.thatIsEven())
                .map   (line::get)
                .sum();
        
        int emptySpace = filesystem.size() - usedSpace;
        var compactFS
            = filesystem
            .mapOnly  (i -> i == null, i -> reverseFs.next())       // Fill null from the front with value from the back
            .limit    (usedSpace)                                   // ... stop at where we know the use space is.
            .appendAll(repeat((Integer)null).limit(emptySpace));    // ... then fill the rest with empty space.
        
        return compactFS
                .mapWithIndex((index, id) -> (id == null) ? 0L : index*id.longValue())
                .mapToObj    (BigInteger::valueOf)
                .reduce      (BigInteger.ZERO, BigInteger::add);
    }
    
    @Struct
    static void Input(
            IntFuncList          diskMap,
            FuncList<IndexedInt> needs) {}
    
    @Struct
    static void Output(int id, int offset, int count) {}
    
    @Struct
    static void Loop(
            Input      input,
            int        offset,
            int        first,
            int        last,
            int        availSpace,
            IndexedInt neededPair,
            Output     output) {}
    
    long diskFragmenter(String line) {
        var inputs
                = IntFuncList.from(line.chars())
                .map(i -> i - '0')
                .toImmutableList();
        
        var usedRevSpaces
                = inputs
                .filterWithIndex((i, num) -> (i % 2) == 0)
                .reverse();
        
        var needs
                = loop(1 + (inputs.size() / 2))
                .toFuncList()
                .reverse()
                .zipToObjWith(usedRevSpaces, (id, size) -> new IndexedInt(id, size))
                .reverse()
                .toImmutableList();
        
        var accumulator = new LongAdder();
        var accepter = Func.f((Integer offset, Integer id, Integer count) -> {
            System.out.println("OUT: " + Tuple3.of(id, count, offset));
            accumulator.add(id * (count * (2 * offset + count - 1) / 2));
        });

        var data = new Loop.Builder()
                .input     (new Input(inputs, needs))
                .offset    (0)
                .first     (0)
                .last      (needs.size() - 2)
                .availSpace(0)
                .neededPair(needs.get(needs.size() - 1))
                .output    (new Output(-1, 0, -1))
                .build()
                ;
        do {
            data = updateData(data);
            accepter.accept(data.output().offset(), data.output().id(), data.output().count());     // Run out of space
        } while (data.first() <= data.last()*2);
        
        accepter.accept(data.offset(), data.neededPair().index(), data.neededPair().item());
        return accumulator.longValue();
    }
    
    private Loop updateData(Loop data) {
        if (data.availSpace() == 0) {
            return data
                    .withOffset    (data.offset() + data.input().diskMap().get(data.first()))
                    .withFirst     (data.first()  + 2)
                    .withAvailSpace(data.input().diskMap().get(data.first() + 1))
                    .withOutput    (out -> out.withId    (data.first() / 2)
                                              .withOffset(data.offset())
                                              .withCount (data.input().diskMap().get(data.first())));
        }
        
        if (data.availSpace() >= data.neededPair().item()) {
            return data
                    .withOffset    (data.offset()     + data.neededPair().item())
                    .withLast      (data.last()       - 1)
                    .withAvailSpace(data.availSpace() - data.neededPair().item())
                    .withNeededPair(data.input().needs().get(data.last()))
                    .withOutput    (out -> out.withId    (data.neededPair().index())
                                              .withOffset(data.offset())
                                              .withCount (data.neededPair().item()));
        }
        
        int leftOver = data.neededPair().item() - data.availSpace();
        return data
                .withOffset    (data.offset() + data.availSpace())
                .withAvailSpace(0)
                .withNeededPair(new IndexedInt(data.neededPair().index(), leftOver))
                .withOutput    (out -> out.withId    (data.neededPair().index())
                                          .withOffset(data.offset())
                                          .withCount (data.availSpace()));
    }
    
    //== Test ==
    
    @Test
    public void testExperiment() {
        var line        = "2333133121414131402";
        var accumulator = diskFragmenter(line);
        System.out.println(accumulator);
    }
    
    @Ignore
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculateChecksum(lines);
        println("result: " + result);
        assertAsString("1928", result);
    }

    @Ignore
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculateChecksum(lines);
        println("result: " + result);
        assertAsString("6216544403458", result);
    }
    
}
