package day9;


import static functionalj.list.intlist.IntFuncList.infinite;
import static functionalj.list.intlist.IntFuncList.loop;
import static functionalj.list.intlist.IntFuncList.repeat;
import static java.lang.Math.max;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import org.junit.Test;

import common.BaseTest;
import functionalj.function.IntIntBiFunction;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

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
    
    static IntIntBiFunction<IntFuncList> expandSegment = (id, length) -> {
        return repeat((id % 2) == 1 ? -1 : id/2).limit(length);
    };
    
    long calculateChecksum(FuncList<String> lines) {
        // General Idea
        // - Expand the code into the actual file system.
        // - Use -1 to represent empty space
        // - Create iterator of the needed files from the back (revert)
        // - Loop through the expanded file system and replace all -1 with the value from the iterator.
        
        var diskMap
                = IntFuncList.from(lines.get(0).chars())
                .map(charToNum)
                .toImmutableList();
        
        var fileSystem 
                = infinite   ()
                .zipToObjWith(diskMap, expandSegment)
                .flatMapToInt(itself())
                .toFuncList  ();
        
        var filesInReverse
                = fileSystem
                .reverse ()
                .exclude (-1)
                .cache   ()
                .iterable()
                .iterator();
        
        var usedSpace
                = loop (diskMap.size())
                .filter(theInt.thatIsEven())
                .sum   (diskMap::get);
        
        var compactFileSystem
            = fileSystem
            .map  (i -> (i != -1) ? i : filesInReverse.next())   // Fill -1 from the front with value from the back
            .limit(usedSpace);                                   // ... stop at where we know the use space is.
        
        return compactFileSystem
                .mapWithIndex(weightedSum)
                .mapToLong()
                .sum();
    }
    
    // diskMap: 
    // [2, 3, 3, 3, 1, 3, 3, 1, 2, 1, 4, 1, 4, 1, 3, 1, 4, 0, 2]
    
    // fileSystem: 
    // [   0,  0, -1, -1, -1,  1,  1,  1, -1, -1, -1
    // ,   2, -1, -1, -1,  3,  3,  3, -1,  4,  4, -1
    // ,   5,  5,  5,  5, -1,  6,  6,  6,  6, -1,  7
    // ,   7,  7, -1,  8,  8,  8,  8,  9,  9
    // ]
    
    // filesInReverse   : [9, 9, 8, 8, 8, 8, 7, 7, 7, 6, 6, 6, 6, 5, 5, 5, 5, 4, 4, 3, 3, 3, 2, 1, 1, 1, 0, 0]
    // usedSpace        : 28
    // compactFileSystem: [0, 0, 9, 9, 8, 1, 1, 1, 8, 8, 8, 2, 7, 7, 7, 3, 3, 3, 6, 4, 4, 6, 5, 5, 5, 5, 6, 6]
    
    //== Test ==
    
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
