package day11;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    
    // Solution -- Dynamic programming.
    // - Use an object to hold already determined -- BlinkChain
    // - The BlinkChain will holder the chain of numbers up until it splitted into two.
    // - The BlinkChain object is mutable by adding more number in the chain. 
    
    static final Long _2024 = 2024L;
    
    @RequiredArgsConstructor
    static class BlinkChain {
        final List<Long> singles = new ArrayList<Long>();
        final long       end1;
        final long       end2;
        int add(long number) {
            singles.add(number);
            return (singles.size() - 1);
        }
    }
    
    // This class can represent a reference to a point in the BlinkChain.
    static record Blink(BlinkChain chain, int index) {
        
        private static ConcurrentHashMap<Long, Blink> blinks = new ConcurrentHashMap<>();
        
        // Create a Blink for a number.
        static Blink of(long number) {
            var blink = blinks.get(number);
            if (blink != null)
                return blink;
            
            var str = ("" + number);
            var len = str.length();
            if ((len % 2) == 0) {
                // Split --- So the chain ends.
                var end1  = Long.parseLong(str.substring(0, len / 2));
                var end2  = Long.parseLong(str.substring(len / 2));
                var chain = new BlinkChain(end1, end2);
                var index = chain.add(number);
                
                var newBlink = new Blink(chain, index);
                blinks.put(number, newBlink);
                return newBlink;
            }
            
            if (number == 0L) {
                // Case of 0 - Just one of the case which a know next value.
                var oneBlink  = Blink.of(1L);
                var oneChain  = oneBlink.chain;
                var zeroIndex = oneChain.add(0L);
                
                var zeroBlink = new Blink(oneChain, zeroIndex);
                blinks.put(number, zeroBlink);
                return zeroBlink;
            }
            
            // Other number which will add the the chain.
            var nextNumber = number * _2024;
            var nextBlink  = Blink.of(nextNumber);
            var nextChain  = nextBlink.chain;
            var thisIndex  = nextChain.add(number);
            
            var thisBlink = new Blink(nextChain, thisIndex);
            blinks.put(number, thisBlink);
            return thisBlink;
        }
    }
    
    // Memoization of the number to count.
    private static ConcurrentHashMap<Long, ConcurrentHashMap<Integer, Long>> counts = new ConcurrentHashMap<>();
    
    // Determine the count with the memoization.
    long stoneCount(long number, int times) {
        var numCounts = counts.computeIfAbsent(number, __ -> new ConcurrentHashMap<>());
        var numCount  = numCounts.get(times);
        if (numCount == null) {
            numCount = determineStoneCount(number, times);
            numCounts.put(times, numCount);
        }
        return numCount;
    }
    
    // Actually determine the count.
    private long determineStoneCount(long number, int times) {
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
    
    long calculate(FuncList<String> lines, int times) {
        return grab(regex("[0-9]+"), lines.get(0))
                .map      (Long::parseLong)
                .sumToLong(num -> stoneCount(num, times));
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines, 25);
        assertAsString("55312", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines, 25);
        assertAsString("194482", result);
    }
    
}
