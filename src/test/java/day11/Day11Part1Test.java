package day11;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day11Part1Test extends BaseTest {
    
    static final BigInteger _2024 = BigInteger.valueOf(2024);
    
    Object calulate(FuncList<String> lines) {
        var stones
                = new LinkedList<>( 
                lines
                .toEager()
                .flatMap(grab(regex("[0-9]+")))
                .map    (parseInt));
        
        
        var totalStones = 0;
        while (stones.size() != 0) {
            var num0 = stones.poll();
            var num  = num0;
            for (int i = 0; i < 25; i++) {
                if (num == 0) {
                    num = 1;
                } else if (hasEvenDigits(num)) {
                    var str  = ("" + num);
                    var len  = str.length();
                    var str1 = str.substring(0, len / 2);
                    var str2 = str.substring(len / 2);
                    var int1 = parseInt(str1);
                    var int2 = parseInt(str2);
                    stones.add(0, int2);
                    
                    num = int1;
                } else {
                    if ((num*2024 / 2024) != num) {
                        throw new IllegalAccessError("Bad: " + num0);
                    }
                    num = num*2024;
                }
            }
            totalStones++;
        }
        
        return totalStones;
    }

    boolean hasEvenDigits(int num) {
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
    
    // A chain is a series of changing number on the stop and ended when it split in two. 
    record BlinkChain(
            // list of singles in this chain
            FuncList<BigInteger> singles,
            // the starting index of this value
            BigInteger end1,
            // the second ending of the chain
            BigInteger end2) {
        
    }
    
    BlinkChain blinkFull(BigInteger num) {
        return null;
    }
    
    ConcurrentHashMap<BigInteger, BlinkChain> chains = new ConcurrentHashMap<>();
    
    BlinkChain blinkChain(BigInteger num) {
        chains.computeIfAbsent(num, __ -> {
            while (true) {
                var str = ("" + num);
                var len = str.length();
                if ((len % 2) == 0) {
                    var str1 = str.substring(0, len / 2);
                    var str2 = str.substring(len / 2);
                    var end1 = new BigInteger(str1);
                    var end2 = new BigInteger(str2);
                    return new BlinkChain(FuncList.of(num), end1, end2);
                }
                
                if (num.equals(BigInteger.ZERO)) {
                    var chain = blinkChain(BigInteger.ONE);
                    return new BlinkChain(FuncList.of(num).appendAll(chain.singles), chain.end1, chain.end2);
                }
            }
        });
        
        return chains.get(num);
    }
    
    @Ignore
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("55312", result);
    }
    
    @Ignore
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("194482", result);
    }
    
}
