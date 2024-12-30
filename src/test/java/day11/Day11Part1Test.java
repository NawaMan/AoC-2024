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

/**
 * --- Day 11: Plutonian Pebbles ---
 * 
 * The ancient civilization on Pluto was known for its ability to manipulate spacetime, and while The Historians explore
 *   their infinite corridors, you've noticed a strange set of physics-defying stones.
 * 
 * At first glance, they seem like normal stones: they're arranged in a perfectly straight line, and each stone has 
 *   a number engraved on it.
 * 
 * The strange part is that every time you blink, the stones change.
 * 
 * Sometimes, the number engraved on a stone changes. Other times, a stone might split in two, causing all the other 
 *   stones to shift over a bit to make room in their perfectly straight line.
 * 
 * As you observe them for a while, you find that the stones have a consistent behavior. Every time you blink, 
 *   the stones each simultaneously change according to the first applicable rule in this list:
 * 
 *     If the stone is engraved with the number 0, it is replaced by a stone engraved with the number 1.
 *     If the stone is engraved with a number that has an even number of digits, it is replaced by two stones. 
 *       The left half of the digits are engraved on the new left stone, and the right half of the digits are engraved 
 *       on the new right stone. (The new numbers don't keep extra leading zeroes: 1000 would become stones 10 and 0.)
 *     If none of the other rules apply, the stone is replaced by a new stone; the old stone's number multiplied by 
 *       2024 is engraved on the new stone.
 * 
 * No matter how the stones change, their order is preserved, and they stay on their perfectly straight line.
 * 
 * How will the stones evolve if you keep blinking at them? You take a note of the number engraved on each stone in 
 *   the line (your puzzle input).
 * 
 * If you have an arrangement of five stones engraved with the numbers 0 1 10 99 999 and you blink once, the stones 
 *   transform as follows:
 * 
 *     The first stone, 0, becomes a stone marked 1.
 *     The second stone, 1, is multiplied by 2024 to become 2024.
 *     The third stone, 10, is split into a stone marked 1 followed by a stone marked 0.
 *     The fourth stone, 99, is split into two stones marked 9.
 *     The fifth stone, 999, is replaced by a stone marked 2021976.
 * 
 * So, after blinking once, your five stones would become an arrangement of seven stones engraved with 
 *   the numbers 1 2024 1 0 9 9 2021976.
 * 
 * Here is a longer example:
 * 
 * Initial arrangement:
 * 125 17
 * 
 * After 1 blink:
 * 253000 1 7
 * 
 * After 2 blinks:
 * 253 0 2024 14168
 * 
 * After 3 blinks:
 * 512072 1 20 24 28676032
 * 
 * After 4 blinks:
 * 512 72 2024 2 0 2 4 2867 6032
 * 
 * After 5 blinks:
 * 1036288 7 2 20 24 4048 1 4048 8096 28 67 60 32
 * 
 * After 6 blinks:
 * 2097446912 14168 4048 2 0 2 4 40 48 2024 40 48 80 96 2 8 6 7 6 0 3 2
 * 
 * In this example, after blinking six times, you would have 22 stones. After blinking 25 times, you would 
 *   have 55312 stones!
 * 
 * Consider the arrangement of stones in front of you. How many stones will you have after blinking 25 times?
 * 
 * Your puzzle answer was 194482.
 */
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
    
    Object calculate(FuncList<String> lines, int times) {
        return grab(regex("[0-9]+"), lines.get(0))
                .map(BigInteger::new)
                .sumToLong(num -> stoneCount(num, times));
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines, 25);
        println("result: " + result);
        assertAsString("55312", result);
    }
    
    @Ignore
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines, 25);
        println("result: " + result);
        assertAsString("194482", result);
    }
    
}
