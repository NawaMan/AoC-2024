package day11;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.tuple.Tuple2;

public class Day11Part2Test extends BaseTest {
    
    ConcurrentHashMap<BigInteger, FuncList<BigInteger>> cache = new ConcurrentHashMap<>();
    
    ConcurrentHashMap<Integer, Integer> levels = new ConcurrentHashMap<>();
    
    ConcurrentHashMap<Tuple2<Integer, BigInteger>, Long> sums = new ConcurrentHashMap<>();
    
    Object calulate(FuncList<String> lines) {
        var stones = new LinkedList<>(
            lines
            .toEager()
            .flatMap(grab(regex("[0-9]+")))
            .map    (BigInteger::new)
        );
        println(stones);
        
        long total = FuncList.from(stones)
                .mapToLong(stone -> numOfStone(25, stone))
                .sum();
        
        return total;
    }
    
    long numOfStone(int level, BigInteger first) {
//        var key = Tuple.of(level, first);
//        var sum = sums.get(key);
//        if (sum != null)
//            return sum;
//            
        var sum = (level == 0) ? 0L : FuncList.from(blink(first)).mapToLong(stone -> numOfStone(level - 1, stone)).sum();
//        
//        sums.put(key, sum);
        return sum;
    }
    
    FuncList<BigInteger> blink(BigInteger first) {
        cache.computeIfAbsent(first, __ -> {
            if (first.equals(BigInteger.ZERO))
                return FuncList.of(BigInteger.ONE);
            
            var str  = ("" + first);
            var len  = str.length();
            if (len % 2 != 0)
                return FuncList.of(first.multiply(BigInteger.valueOf(2024)));
            
            var str1 = str.substring(0, len / 2);
            var str2 = str.substring(len / 2);
            var int1 = new BigInteger(str1);
            var int2 = new BigInteger(str2);
            return FuncList.of(int1, int2);
        });
        return cache.get(first);
    }
    
    //== Test ==

    @Ignore
    @Test
    public void testThing() {
        println(125 + ": " + blink(BigInteger.valueOf(125)));
        println();
        
        println(253000 + ": " + blink(BigInteger.valueOf(253000)));
        println();
        
        println(253 + ": " + blink(BigInteger.valueOf(253)));
        println(0   + ": " + blink(BigInteger.valueOf(0)));
        println();
        
        println(512  + ": " + blink(BigInteger.valueOf(512)));
        println(72   + ": " + blink(BigInteger.valueOf(72)));
        println(2024 + ": " + blink(BigInteger.valueOf(2024)));
        println();
        
        println(1036288 + ": " + blink(BigInteger.valueOf(1036288)));
        println(7       + ": " + blink(BigInteger.valueOf(7)));
        println(2       + ": " + blink(BigInteger.valueOf(2)));
        println(20      + ": " + blink(BigInteger.valueOf(20)));
        println(24      + ": " + blink(BigInteger.valueOf(24)));
        println();
        
        println(2097446912 + ": " + blink(BigInteger.valueOf(2097446912)));
        println(14168      + ": " + blink(BigInteger.valueOf(14168)));
        println(4048       + ": " + blink(BigInteger.valueOf(4048)));
        println(2          + ": " + blink(BigInteger.valueOf(2)));
        println(0          + ": " + blink(BigInteger.valueOf(0)));
        println(2          + ": " + blink(BigInteger.valueOf(2)));
        println(4          + ": " + blink(BigInteger.valueOf(4)));
        println();
        
        println(20974 + ": " + blink(BigInteger.valueOf(20974)));
        println(46912      + ": " + blink(BigInteger.valueOf(46912)));
        println(28676032       + ": " + blink(BigInteger.valueOf(28676032)));
        println(40         + ": " + blink(BigInteger.valueOf(40)));
        println(48          + ": " + blink(BigInteger.valueOf(48)));
        println(4048          + ": " + blink(BigInteger.valueOf(4048)));
        println(1          + ": " + blink(BigInteger.valueOf(1)));
        println(4048        + ": " + blink(BigInteger.valueOf(4048)));
        println(8096          + ": " + blink(BigInteger.valueOf(8096)));
        println();
    }
    
    @Ignore
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
