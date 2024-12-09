package day9;

import static functionalj.functions.StrFuncs.*;
import static functionalj.list.FuncList.repeat;
import static functionalj.list.intlist.IntFuncList.infinite;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;
import functionalj.stream.StreamPlus;

public class Day9Part1Test extends BaseTest {
    
    
    Object calulate(FuncList<String> lines) {
        var line = IntFuncList.from(lines.get(0).chars()).map(i -> i - '0').cache();
        println(line);
        
        var filesystem 
                = infinite()
                .zipToObjWith(line, (id, length) -> repeat((id % 2) == 1 ? null : id/2).limit(length))
                .flatMap(itself())
                .cache();
        filesystem.forEach(println);
        
        int usedSpace 
                = filesystem
                .excludeNull()
                .size();
        int fullSpace
                = filesystem.size();
        int emptySpace = fullSpace - usedSpace;
        
        
        var reverseFs = filesystem.reverse().cache().iterable().iterator();
        
        var compactFS
            = filesystem.map(i -> {
                if (i != null)
                    return i;
                
                var ch = reverseFs.next();
                while (ch == null)
                    ch = reverseFs.next();
                
                return ch;
            })
            .limit(usedSpace)
            .appendAll(FuncList.repeat((Integer)null).limit(emptySpace))
            .cache()
            ;
        println();
        
        var checksum
                = compactFS
                .mapWithIndex((index, id) -> {
                    if (id == null)
                        return 0L;
                    
                    return index*id.longValue();
                })
                .mapToObj(BigInteger::valueOf)
                .reduce(BigInteger.ZERO, BigInteger::add);
        println();
        
        return checksum;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("1928", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("6216544403458", result);
    }
    
}
