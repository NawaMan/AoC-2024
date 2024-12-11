package day11;

import java.math.BigInteger;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.AsIntFuncList;
import functionalj.list.intlist.IntFuncList;

public class Day11Part1Test extends BaseTest {
    
    
    Object calulate(FuncList<String> lines) {
        var stones = 
                lines
                .toEager()
                .flatMap(grab(regex("[0-9]+")))
                .map    (BigInteger::new);
        
        for (int i = 0; i < 75; i++) {
            println(i + ": " + stones.size());
            stones = blink(stones).toEager();
        }
        
        return stones.size();
    }
    
    FuncList<BigInteger> blink(FuncList<BigInteger> stones) {
        return stones.flatMap(num -> {
            return (num.equals(BigInteger.ZERO)) ? FuncList.of(BigInteger.ONE) :
                   hasEvenDigits(num)            ? splitNum(num)     :
                                                   FuncList.of(num.multiply(BigInteger.valueOf(2024)));
        });
    }

    boolean hasEvenDigits(BigInteger num) {
        return ("" + num).length() % 2 == 0;
    }
    
    FuncList<BigInteger> splitNum(BigInteger num) {
        var str  = ("" + num);
        var len  = str.length();
        var str1 = str.substring(0, len / 2);
        var str2 = str.substring(len / 2);
        var int1 = new BigInteger(str1);
        var int2 = new BigInteger(str2);
        return FuncList.of(int1, int2);
    }

    //== Test ==
    
    @Ignore
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("55312", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("194482", result);
    }
    
}
