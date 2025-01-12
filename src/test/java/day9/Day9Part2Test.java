package day9;

import static functionalj.list.intlist.IntFuncList.infinite;
import static functionalj.list.intlist.IntFuncList.repeat;
import static functionalj.stream.intstream.IntStreamPlus.range;
import static java.lang.Math.max;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import org.junit.Test;

import common.BaseTest;
import functionalj.function.IntIntBiFunction;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;
import functionalj.stream.intstream.IntStreamPlus;
import functionalj.tuple.IntIntTuple;

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
    
    static IntUnaryOperator  charToNum   = i -> i - '0';
    static IntBinaryOperator weightedSum = (index, id) -> index*max(0, id);
    
    static IntIntBiFunction<IntFuncList> expand = (id, length) -> {
        return repeat((id % 2) == 1 ? -1 : id/2).limit(length);
    };
    
    long calculate(FuncList<String> lines) {
        // General Idea
        // - Convert the file system section to array. 
        // - Going through empty section and find from the back section that can fit in.
        
        // Convert the disk map as string to disk map as IntFuncList.
        var diskMap
                = IntFuncList.from(lines.get(0).chars())
                .map  (i -> i - '0')
                .cache()
                ;
        
        // Create pairs between file ID and needed space -- then revert.
        // (id, neededSpace): (9,2), (8,4), (7,3), (6,4), (5,4), (4,2), (3,3), (2,1), (1,3), (0,2)
        var reveredDataBlocks
                = diskMap
                .filterWithIndex  ((index, num) -> index % 2 == 0)
                .mapToObjWithIndex(IntIntTuple::new)
                .reverse          ()
                ;
        
        // Expand the file system.
        // Each of the section is an array of ints where -1 means empty.
        // 
        // [   0,  0, -1, -1, -1,  1,  1,  1, -1, -1, -1
        // ,   2, -1, -1, -1,  3,  3,  3, -1,  4,  4, -1
        // ,   5,  5,  5,  5, -1,  6,  6,  6,  6, -1,  7
        // ,   7,  7, -1,  8,  8,  8,  8,  9,  9
        // ]
        var filesystem 
                = infinite()
                .zipToObjWith(diskMap, (id, length) -> repeat((id % 2) == 1 ? -1 : id/2).limit(length).toArray())
                .cache()
                ;
        
        // Extract file sections
        // [0, 0], [1, 1, 1], [2], [3, 3, 3], [4, 4], [5, 5, 5, 5], [6, 6, 6, 6], [7, 7, 7], [8, 8, 8, 8], [9, 9]
        var fileSections
                = filesystem
                .filterWithIndex((index, array) -> index % 2 != 1)
                .toArray(int[][]::new);
        
        // For each needed space, look into the file-system (from the front to find the big enough empty space.)
        reveredDataBlocks
        .forEach(pair -> {
            var id          = pair._1;
            var neededSpace = pair._2;
            
            // Loop over each empty spaces (the even-indexed of the file system).
            // Starting just after the pair position (id*2).
            range(0, id * 2)
            .mapToObj (filesystem::get)
            
            .filter   (foundArray -> IntStreamPlus.of(foundArray).filter(i -> i == -1).size() >= neededSpace)
            .findFirst()
            .ifPresent(foundArray -> migrateData(fileSections, id, neededSpace, foundArray));
        });
        
        return filesystem
                .flatMapToInt(IntFuncList::of)
                .mapWithIndex(weightedSum)
                .sum();
    }
    
     int migrateData(int[][] source, int id, int neededSpace, int[] target) {
        for (int j = 0; j < target.length && neededSpace > 0; j++) {
            if (target[j] == -1) {
                target[j] = id;
                neededSpace--;
            }
        }
        
        var array = source[id];
        for (int j = 0; j < array.length; j++)
            array[j] = -1;
        
        return neededSpace;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("2858", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("6237075041489", result);
    }
    
}
