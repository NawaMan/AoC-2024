package day9;


import static functionalj.list.intlist.IntFuncList.AllOf;
import static functionalj.list.intlist.IntFuncList.infinite;
import static functionalj.list.intlist.IntFuncList.loop;
import static functionalj.list.intlist.IntFuncList.repeat;
import static functionalj.stream.intstream.IntStep.StartFrom;
import static functionalj.stream.intstream.IntStreamPlus.range;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;
import static java.util.Comparator.comparing;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import org.junit.Test;

import common.BaseTest;
import functionalj.function.IntIntBiFunction;
import functionalj.list.FuncList;
import functionalj.list.ImmutableFuncList;
import functionalj.list.intlist.IntFuncList;
import functionalj.stream.intstream.IndexedInt;
import functionalj.tuple.Tuple2;
import functionalj.tuple.Tuple3;

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
    
    static IntUnaryOperator  charToNum   = i -> i - '0';
    static IntBinaryOperator weightedSum = (index, id) -> index*max(0, id);
    
    static IntIntBiFunction<IntFuncList> expand = (id, length) -> {
        return repeat((id % 2) == 1 ? -1 : id/2).limit(length);
    };
    
    long calculateChecksum(FuncList<String> lines) {
        // General Idea
        // - Expand the code into the actual file system.
        // - Use -1 to represent empty space
        // - Create iterator of the needed files from the back (revert)
        // - Loop through the expanded file system and replace all -1 with the value from the iterator.
        
        // Convert the disk map as string to disk map as IntFuncList.
        var diskMap 
                = IntFuncList.from(lines.get(0).chars())
                .map(charToNum)
                .cache();
        
        // Input line: 
        // [2, 3, 3, 3, 1, 3, 3, 1, 2, 1, 4, 1, 4, 1, 3, 1, 4, 0, 2]
        
        // [   0,  0, -1, -1, -1,  1,  1,  1, -1, -1, -1
        // ,   2, -1, -1, -1,  3,  3,  3, -1,  4,  4, -1
        // ,   5,  5,  5,  5, -1,  6,  6,  6,  6, -1,  7
        // ,   7,  7, -1,  8,  8,  8,  8,  9,  9
        // ]
        
        // Expand the file system.
        var filesystem 
                = infinite()
                .zipToObjWith(diskMap, expand)
                .flatMapToInt(itself())
                .toFuncList();
        
        // Get an iterator of the reverse of the file system so we can get its value out one-by-one.
        var filesInReverse
                = filesystem
                .reverse()
                .exclude(-1)
                .cache()
                .iterable()
                .iterator();
        
        // Calculate the used and empty space to know when to stop.
        var usedSpace
                = loop (diskMap.size())
                .filter(theInt.thatIsEven())
                .sum   (diskMap::get);
        
        // Compact the file-system
        var compactFS
            = filesystem
            .map  (i -> (i != -1) ? i : filesInReverse.next())   // Fill -1 from the front with value from the back
            .limit(usedSpace);                                   // ... stop at where we know the use space is.
        
        return compactFS
                .mapWithIndex(weightedSum)
                .mapToLong()
                .sum();
    }
    
    //== Test ==
    
    
    public static void main(String[] args) {
        var line = "2333133121414131402";
        System.out.println(defragment(line));
    }
    
    public static BigInteger defragment(String line) {
        var inputs    = IntFuncList.from(line.chars()).map(i -> i - '0').toArray();
        int fileCount = (inputs.length / 2) + 1;
        var filesRev  = StartFrom(0).step(2).limit(fileCount).map(i -> inputs[i]).mapWithIndex().reverse().toImmutableList();
        
        // Move when possible and leave the rest.
        var outputs = IntFuncList.range(0, filesRev.size()).mapToObj(i -> oneLoop(i, inputs, filesRev)).toArrayList();
        
        // Add the space from the files that has been moved.
        var additions
                = outputs
                .stream()
                .filter(t3 -> t3._3())
                .map   (t3 -> Tuple3.of(t3._2()._1()*2, new IndexedInt(0, inputs[t3._2()._1()*2]), false))
                .toList();
        additions.forEach(outputs::add);
        
        // Add the left-over or not filled space.
        IntFuncList.of(inputs)
        .mapWithIndex()
        .filter(theIndexedInt.index().thatIsOdd())
        .filter(theIndexedInt.item().thatIsNotZero())
        .map   (pair -> Tuple3.of(pair._1(), new IndexedInt(0, pair._2()), false))
        .forEach(outputs::add);
        
        outputs.sort(comparing(Tuple3::_1));
        
        return FuncList.from(outputs)
                .map(t3 -> t3._2()._2())
                .accumulate((a, b) -> a + b)
                .prepend(0)
                .zipWith(FuncList.from(outputs).map(t3 -> t3._2()))  // ------------------------- D
                .mapToObj(t2 -> IntFuncList.range(t2._1(), t2._1() + t2._2()._2()).mapToObj(i -> BigInteger.valueOf(i*t2._2()._1())).reduce(BigInteger::add).orElse(BigInteger.ZERO))
                .reduce(BigInteger::add)
                .orElse(BigInteger.ZERO);
//        .forEach(System.out::println);
        
        // D
//        (0,(0,2))
//        (2,(9,2))
//        (4,(2,1))
//        (5,(1,3))
//        (8,(7,3))
//        (11,(0,1))
//        (12,(4,2))
//        (14,(0,1))
//        (15,(3,3))
//        (18,(0,1))
//        (19,(0,2))
//        (21,(0,1))
//        (22,(5,4))
//        (26,(0,1))
//        (27,(6,4))
//        (31,(0,1))
//        (32,(0,3))
//        (35,(0,1))
//        (36,(8,4))
//        (40,(0,2))
    }
    
    private static Tuple3<Integer, IndexedInt, Boolean> oneLoop(int revFileId, int[] inputs, ImmutableFuncList<IndexedInt> filesRev) {
        var file     = filesRev.get(revFileId);
        var fileId   = file._1();
        var fileIdx  = fileId*2;
        var fileSize = file._2();
        
        var availIndex = -1;
        for (int i = 1; i < inputs.length; i += 2) {
            var availSize = inputs[i];
            if (availSize >= fileSize) {
                availIndex = i;
                break;
            }
        }
        
        if ((availIndex == -1) || (availIndex >= fileIdx)) {
            return Tuple3.of(fileIdx, file, false);
        } else {
            inputs[availIndex] -= fileSize;
            return Tuple3.of(availIndex, file, true);
        }
    }
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculateChecksum(lines);
        println("result: " + result);
        assertAsString("1928", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculateChecksum(lines);
        println("result: " + result);
        assertAsString("6216544403458", result);
    }
    
}
