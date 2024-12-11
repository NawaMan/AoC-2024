package day11;

import static functionalj.functions.StrFuncs.*;

import java.math.BigInteger;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

@SuppressWarnings("unused")
@Ignore
public class Day11Part2Test extends BaseTest {
    
    static final int MAX = Integer.MAX_VALUE / 2024;
    
    Object calulate(FuncList<String> lines) {
        var stones = 
                lines
                .toEager()
                .flatMap(grab(regex("[0-9]+")))
                .map    (BigInteger::new);
        
        for (int i = 0; i < 10; i++) {
            println(i + ": " + stones.size());
            stones = blink(stones);
        }

        
        println("Total " + stones.size());
        
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
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
    @Ignore
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
}
