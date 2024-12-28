package day19;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import common.BaseTest;
import functionalj.lens.lenses.LongAccessPrimitive;
import functionalj.list.FuncList;
import functionalj.map.FuncMap;

public class Day19Part2Test extends BaseTest {
    
    Object calculate(FuncList<String> lines) {
        var availables = grab(regex("[a-z]+"), lines.get(0))
                       .groupingBy (line -> (Character)line.charAt(0))
                       .mapValue   (list -> list.map(String.class::cast))
                       .toImmutableMap();
        return lines
                .skip(2)
                .sumToLong(possibleWays(availables));
    }
    
    LongAccessPrimitive<String> possibleWays(FuncMap<Character, FuncList<String>> availables) {
        return pattern -> {
            var cache = new ConcurrentHashMap<Integer, Long>();
            return possibleWays(0, pattern, availables, cache);
        };
    }
    
    long possibleWays(int offset, String pattern, 
            FuncMap<Character, FuncList<String>> availables, 
            ConcurrentHashMap<Integer, Long>     cacheWays) {
        var ways = cacheWays.get(offset);
        if (ways != null)
            return ways;
        
        ways = determinePossibleWays(offset, pattern, availables, cacheWays);
        cacheWays.put(offset, ways);
        return ways;
    }
    
    long determinePossibleWays(int offset, String pattern, 
            FuncMap<Character, FuncList<String>>  availables,
            ConcurrentHashMap<Integer, Long>      cacheWays) {
        if (offset >= pattern.length())
            return 1L;
        
        var first   = pattern.charAt(offset);
        var choices = availables.get(first);
        if (choices == null)
            return 0L;
        
        return choices.filter   (choice -> pattern.substring(offset).startsWith(choice))
                      .sumToLong(choice -> possibleWays(offset + choice.length(), pattern, availables, cacheWays));
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("16", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("777669668613191", result);
    }
    
}
