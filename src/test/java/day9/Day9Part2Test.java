package day9;

import static day9.File.theFile;
import static day9.Segment.theSegment;
import static functionalj.function.Operators.sumInts;
import static functionalj.list.FuncList.combine;
import static functionalj.list.FuncList.repeat;
import static functionalj.list.intlist.IntFuncList.infinite;
import static functionalj.list.intlist.IntFuncList.range;
import static functionalj.stream.intstream.IntStep.StartFrom;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.ImmutableFuncList;
import functionalj.list.intlist.IntFuncList;
import functionalj.stream.StreamPlus;
import functionalj.tuple.IntIntTuple;
import functionalj.tuple.IntTuple2;
import functionalj.types.Struct;

/**
 * --- Part Two ---
 * 
 * Upon completion, two things immediately become clear. First, the disk definitely has a lot more contiguous free 
 *   space, just like the amphipod hoped. Second, the computer is running much more slowly! Maybe introducing all of 
 *   that file system fragmentation was a bad idea?
 * 
 * The eager amphipod already has a new plan: rather than move individual blocks, he'd like to try compacting the files 
 * on his disk by moving whole files instead.
 * 
 * This time, attempt to move whole files to the leftmost span of free space blocks that could fit the file. Attempt to 
 *   move each file exactly once in order of decreasing file ID number starting with the file with the highest file ID 
 *   number. If there is no span of free space to the left of a file that is large enough to fit the file, the file does 
 *   not move.
 * 
 * The first example from above now proceeds differently:
 * 
 * 00...111...2...333.44.5555.6666.777.888899
 * 0099.111...2...333.44.5555.6666.777.8888..
 * 0099.1117772...333.44.5555.6666.....8888..
 * 0099.111777244.333....5555.6666.....8888..
 * 00992111777.44.333....5555.6666.....8888..
 * 
 * The process of updating the filesystem checksum is the same; now, this example's checksum would be 2858.
 * 
 * Start over, now compacting the amphipod's hard drive using this new method instead. What is the resulting filesystem 
 *   checksum?
 * 
 * Your puzzle answer was 6237075041489.
 */
public class Day9Part2Test extends BaseTest {
    
    static IntUnaryOperator charToNum = i -> i - '0';
    
    static IntUnaryOperator intAt(int[] ints) {
        return i -> ints[i];
    }
    
    // Expanding method
    
    BigInteger calculate(FuncList<String> lines) {
        var diskMap
                = IntFuncList.from(lines.get(0).chars())
                .map(i -> i - '0')
                .cache();
        var reveredDataBlocks
                = diskMap
                .filterWithIndex((index, num) -> index % 2 == 0)
                .mapToObjWithIndex(IntIntTuple::new)
                .reverse()
                .cache();
        var filesystem 
                = infinite()
                .zipToObjWith(diskMap, (id, length) -> repeat((id % 2) == 1 ? null : id/2).limit(length))
                .map(FuncList::toArray)
                .cache()
                ;
        var sectorByIds
                = filesystem
                .filterWithIndex((index, array) -> index % 2 != 1)
                .toArray();
        
        reveredDataBlocks.forEach(pair -> {
            var id          = pair._1;
            var neededSpace = pair._2;
            for (int i = 0; i < (id * 2); i++) {
                var foundArray = filesystem.get(i);
                var hasEnough  = StreamPlus.of(foundArray).filter(Objects::isNull).size() >= neededSpace;
                if(hasEnough) {
                    migrateData(sectorByIds, id, neededSpace, foundArray);
                    break;
                }
            }
        });
        
        return filesystem
                .flatMap     (FuncList::of)
                .mapWithIndex((index, id) -> (id == null) ? 0L :(long)index*((Integer)id).intValue())
                .mapToObj    (BigInteger::valueOf)
                .reduce      (BigInteger.ZERO, BigInteger::add);
    }
    
    int migrateData(Object[] sectorByIds, int id, int neededSpace, Object[] foundArray) {
        for (int j = 0; j < foundArray.length && neededSpace > 0; j++) {
            if (foundArray[j] == null) {
                foundArray[j] = id;
                neededSpace--;
            }
        }
        
        var array = (Object[])sectorByIds[id];
        for (int j = 0; j < array.length; j++)
            array[j] = null;
        return neededSpace;
    }
    
    // Non expanding method.
    
    long defragment(String line) {
        var inputs    = IntFuncList.from(line.chars()).map(charToNum).toArray();
        int fileCount = (inputs.length / 2) + 1;
        
        var fileSegments
                = StartFrom(0).step(2).limit(fileCount)
                .map              (intAt(inputs))
                .mapToObjWithIndex(File::new);
        var segmentsRevert
                = fileSegments
                .reverse          ()
                .toImmutableList  ();
        
        var movedFiles
                = range(0, fileCount)
                .mapToObj   (segmentIndex -> moveFiles(segmentIndex, inputs, segmentsRevert))
                .toImmutableList();
        
        var leftEmptySpaces
                = FuncList.from(movedFiles)
                .toImmutableList()
                .filter (output -> output.hasMoved())
                .map    (output -> output.file())
                .map    (file   -> file.id()*2)
                .map    (index  -> new Segment(index, new File(0, inputs[index]), false));
        
        var unfilledSpaces
                = IntFuncList.of(inputs)
                .mapToObjWithIndex(File::new)
                .filter (theFile.id  .thatIsOdd())
                .filter (theFile.size.thatIsNotZero())
                .map    (file -> new Segment(file.id(), new File(0, file.size()), false));
        
        var outputSegments
                = combine(movedFiles, leftEmptySpaces, unfilledSpaces)
                .sortedBy(theSegment.index);
        
        var segmentOffsets
                = FuncList.from(outputSegments)
                .mapToInt  (segment -> segment.file().size())
                .accumulate(sumInts)
                .prepend   (0);
        var segmentFiles
                = FuncList.from(outputSegments)
                .map(segment -> segment.file());
        
        var segmentOffsetWithFiles
                = segmentOffsets.zipWith(segmentFiles);
        
        return segmentOffsetWithFiles
                .sumToLong (this::weightedSum);
    }
    
    @Struct
    void File(int id, int size) {}
    
    @Struct
    void Segment(int index, File file, boolean hasMoved) {}
    
    static Segment moveFiles(int revFileId, int[] inputs, ImmutableFuncList<File> filesRev) {
        var file     = filesRev.get(revFileId);
        var fileId   = file.id();
        var fileIdx  = fileId*2;
        var fileSize = file.size();
        
        var availIdx = -1;
        for (int i = 1; i < inputs.length; i += 2) {
            var availSize = inputs[i];
            if (availSize >= fileSize) {
                availIdx = i;
                break;
            }
        }
        
        if ((availIdx == -1) || (availIdx >= fileIdx)) {
            return new Segment(fileIdx, file, false);
        } else {
            inputs[availIdx] -= fileSize;
            return new Segment(availIdx, file, true);
        }
    }
    
    long weightedSum(IntTuple2<day9.File> t2) {
        return IntFuncList.range(t2._1(), t2._1() + t2._2().size()).mapToLong(i -> (long)i*(long)t2._2().id()).sum();
    }
    
    // Example
    // inputs:         [2, 3, 3, 3, 1, 3, 3, 1, 2, 1, 4, 1, 4, 1, 3, 1, 4, 0, 2]
    // fileCount:      10
    // 
    // Desired
    // 00...111...2...333.44.5555.6666.777.888899
    // 0099.111...2...333.44.5555.6666.777.8888..
    // 0099.1117772...333.44.5555.6666.....8888..
    // 0099.111777244.333....5555.6666.....8888..
    // 00992111777.44.333....5555.6666.....8888..
    // 
    // fileSegments:   (0,2),(1,3),(2,1),(3,3),(4,2),(5,4),(6,4),(7,3),(8,4),(9,2)
    // segmentsRevert: (9,2),(8,4),(7,3),(6,4),(5,4),(4,2),(3,3),(2,1),(1,3),(0,2)
    // movedFiles:
    //    (1,(9,2),true),
    //    (16,(8,4),false),
    //    (3,(7,3),true),
    //    (12,(6,4),false),
    //    (10,(5,4),false),
    //    (5,(4,2),true),
    //    (6,(3,3),false),
    //    (1,(2,1),true),
    //    (2,(1,3),false),
    //    (0,(0,2),false)
    // leftEmptySpaces
    //    (18,(0,2),false),
    //    (14,(0,3),false),
    //    (8,(0,2),false),
    //    (4,(0,1),false)
    // unfilledSpaces
    //    (5,(0,1),false),
    //    (7,(0,1),false),
    //    (9,(0,1),false),
    //    (11,(0,1),false),
    //    (13,(0,1),false),
    //    (15,(0,1),false)
    // 
    // outputSegments
    //    (0,(0,2),false),
    //    (1,(9,2),true),
    //    (1,(2,1),true),
    //    (2,(1,3),false),
    //    (3,(7,3),true),
    //    (4,(0,1),false),
    //    (5,(4,2),true),
    //    (5,(0,1),false),
    //    (6,(3,3),false),
    //    (7,(0,1),false),
    //    (8,(0,2),false),
    //    (9,(0,1),false),
    //    (10,(5,4),false),
    //    (11,(0,1),false),
    //    (12,(6,4),false),
    //    (13,(0,1),false),
    //    (14,(0,3),false),
    //    (15,(0,1),false),
    //    (16,(8,4),false),
    //    (18,(0,2),false)
    // 
    // segmentOffsetWithFiles
    // (0,(0,2))
    // (2,(9,2))
    // (4,(2,1))
    // (5,(1,3))
    // (8,(7,3))
    // (11,(0,1))
    // (12,(4,2))
    // (14,(0,1))
    // (15,(3,3))
    // (18,(0,1))
    // (19,(0,2))
    // (21,(0,1))
    // (22,(5,4))
    // (26,(0,1))
    // (27,(6,4))
    // (31,(0,1))
    // (32,(0,3))
    // (35,(0,1))
    // (36,(8,4))
    // (40,(0,2))
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
//        var result = calculate(lines);
        var result = defragment(lines.get(0));
        println("result: " + result);
        assertAsString("2858", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
//        var result = calculate(lines);
        var result = defragment(lines.get(0));
        println("result: " + result);
        assertAsString("6237075041489", result);
    }
    
}
