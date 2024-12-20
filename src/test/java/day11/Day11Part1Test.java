package day11;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import lombok.RequiredArgsConstructor;

public class Day11Part1Test extends BaseTest {
    
    static final BigInteger _2024 = BigInteger.valueOf(2024);
    
    @RequiredArgsConstructor
    static class BlinkChain {
        final List<BigInteger> singles = new ArrayList<BigInteger>();
        final BigInteger       end1;
        final BigInteger       end2;
        int add(BigInteger number) {
            singles.add(number);
            return (singles.size() - 1);
        }
    }
    
    static record Blink(BlinkChain chain, int index) {
        
        private static ConcurrentHashMap<BigInteger, Blink> blinks = new ConcurrentHashMap<>();
        
        static Blink of(BigInteger number) {
            var blink = blinks.get(number);
            if (blink != null)
                return blink;
            
            var str = ("" + number);
            var len = str.length();
            if ((len % 2) == 0) {
                var end1  = new BigInteger(str.substring(0, len / 2));
                var end2  = new BigInteger(str.substring(len / 2));
                var chain = new BlinkChain(end1, end2);
                var index = chain.add(number);
                
                var newBlink = new Blink(chain, index);
                blinks.put(number, newBlink);
                return newBlink;
            }
            
            if (number.equals(BigInteger.ZERO)) {
                var oneBlink  = Blink.of(BigInteger.ONE);
                var oneChain  = oneBlink.chain;
                var zeroIndex = oneChain.add(BigInteger.ZERO);

                var zeroBlink = new Blink(oneChain, zeroIndex);
                blinks.put(number, zeroBlink);
                return zeroBlink;
            }
            
            var nextNumber = number.multiply(_2024);
            var nextBlink  = Blink.of(nextNumber);
            var nextChain  = nextBlink.chain;
            var thisIndex  = nextChain.add(number);

            var thisBlink = new Blink(nextChain, thisIndex);
            blinks.put(number, thisBlink);
            return thisBlink;
        }
    }
    
    private static ConcurrentHashMap<BigInteger, ConcurrentHashMap<Integer, Long>> counts = new ConcurrentHashMap<>();
    
    long stoneCount(BigInteger number, int times) {
        var numCounts = counts.computeIfAbsent(number, __ -> new ConcurrentHashMap<>());
        var numCount  = numCounts.get(times);
        if (numCount == null) {
            numCount = determineStoneCount(number, times);
            numCounts.put(times, numCount);
        }
        return numCount;
        
    }

    long determineStoneCount(BigInteger number, int times) {
        var info  = Blink.of(number);
        var index = info.index;
        var chain = info.chain;
        if (times <= index)
            return 1L;
        if (times == (index + 1))
            return 2L;
        
        int left   = times - index - 1;
        var count1 = stoneCount(chain.end1, left);
        var count2 = stoneCount(chain.end2, left);
        return count1 + count2;
    }
    
    
    Object calulate(FuncList<String> lines, int times) {
        return grab(regex("[0-9]+"), lines.get(0))
                .map(BigInteger::new)
                .sumToLong(num -> stoneCount(show("num: ", num), times));
    }
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines, 25);
        println("result: " + result);
        assertAsString("55312", result);
    }
    
    @Ignore
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines, 25);
        println("result: " + result);
        assertAsString("194482", result);
    }
    
}
