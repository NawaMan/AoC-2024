package day9;

import static functionalj.list.FuncList.repeat;
import static functionalj.list.intlist.IntFuncList.infinite;

import java.math.BigInteger;
import java.util.Objects;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;
import functionalj.stream.StreamPlus;
import functionalj.tuple.IntIntTuple;

public class Day9Part2Test extends BaseTest {
    
    
    Object calulate(FuncList<String> lines) {
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
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("2858", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("6237075041489", result);
    }
    
}
