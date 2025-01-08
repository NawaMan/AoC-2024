package day9;

import static functionalj.list.FuncList.repeat;
import static functionalj.list.intlist.IntFuncList.infinite;
import static functionalj.stream.intstream.IntStreamPlus.range;

import java.math.BigInteger;
import java.util.Objects;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;
import functionalj.stream.StreamPlus;
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
    
    BigInteger calculate(FuncList<String> lines) {
        var diskMap
                = IntFuncList.from(lines.get(0).chars())
                .map(i -> i - '0')
                .cache();
        var reveredDataBlocks
                = diskMap
                .filterWithIndex  ((index, num) -> index % 2 == 0)
                .mapToObjWithIndex(IntIntTuple::new)
                .reverse();
        var filesystem 
                = infinite()
                .zipToObjWith(diskMap, (id, length) -> repeat((id % 2) == 1 ? null : id/2).limit(length).toArray())
                .cache()
                ;
        var sectorByIds
                = filesystem
                .filterWithIndex((index, array) -> index % 2 != 1)
                .toArray();

        
        reveredDataBlocks.forEach(pair -> {
            var id          = pair._1;
            var neededSpace = pair._2;
//            range(0, id * 2)
//            .mapToObj(filesystem::get)
//            .filter(foundArray -> StreamPlus.of(foundArray).filter(Objects::isNull).size() >= neededSpace)
//            .findFirst()
//            .ifPresent(foundArray -> migrateData(sectorByIds, id, neededSpace, foundArray));
            for (int i = 0; i < (id * 2); i++) {
                var foundArray = filesystem.get(i);
                var hasEnough  = StreamPlus.of(foundArray).filter(Objects::isNull).size() >= neededSpace;
                if(hasEnough) {
                    migrateData(sectorByIds, id, neededSpace, foundArray);
                    break;
                }
            }
        });
        
        var checksum
                = filesystem
                .flatMap     (FuncList::of)
                .mapWithIndex((index, id) -> (id == null) ? 0L :(long)index*((Integer)id).intValue())
                .mapToObj    (BigInteger::valueOf)
                .reduce      (BigInteger.ZERO, BigInteger::add);
        
        return checksum;
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
