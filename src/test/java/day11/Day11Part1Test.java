package day11;

import static java.lang.Math.log10;
import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    
    static record Blink(BlinkChain chain, int index) {
        
        private static final Map<Long, Blink> knownBlinks = new ConcurrentHashMap<>();
        
        @RequiredArgsConstructor
        private static class BlinkChain {
            final List<Long> singles = new ArrayList<Long>();
            final long       end1;
            final long       end2;
            int add(long number) {
                singles.add(number);
                return (singles.size() - 1);
            }
            Blink chain(long nextNumber) {
                return new Blink(this, add(nextNumber));
            }
        }
        
        static Blink of(long number) {
            var blink = knownBlinks.get(number);
            if (blink != null)
                return blink;
            
            if (number == 0L)
                return register(Blink.of(1L).chain(0L));
            
            var digitCount = (int)log10(number) + 1;
            if ((digitCount % 2) == 0) {
                var divisor = (long)pow(10, digitCount / 2);
                var end1    = number / divisor;
                var end2    = number % divisor;
                return register(new BlinkChain(end1, end2).chain(number));
            }
            
            return register(Blink.of(number * 2024L).chain(number));
        }
        
        private Blink chain(long nextNumber) {
            return new Blink(chain, chain.add(nextNumber));
        }
        
        private static Blink register(Blink blink) {
            knownBlinks.put(blink.chain.singles.get(blink.index), blink);
            return blink;
        }
    }
    
    static class StoneCounter {
        
        private static record CountKey(long number, int times) {}
        private static Map<CountKey, Long> knownCounts = new ConcurrentHashMap<>();
        
        long count(long number, int times) {
            var key   = new CountKey(number, times);
            var count = knownCounts.get(key);
            if (count == null) {
                count = determineCount(number, times);
                knownCounts.put(key, count);
            }
            return count;
        }
        
        private long determineCount(long number, int times) {
            var info  = Blink.of(number);
            var index = info.index;
            if (times <= index)
                return 1L;
            if (times == (index + 1))
                return 2L;

            var chain = info.chain;
            int left  = times - index - 1;
            return count(chain.end1, left)
                 + count(chain.end2, left);
        }
        
    }
    
    long calculate(FuncList<String> lines, int times) {
        var stoneCount = new StoneCounter();
        return grab(regex("[0-9]+"), lines.get(0))
                .map      (Long::parseLong)
                .sumToLong(num -> stoneCount.count(num, times));
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines, 25);
        println(result);
        assertAsString("55312", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines, 25);
        println(result);
        assertAsString("194482", result);
    }
    
}
