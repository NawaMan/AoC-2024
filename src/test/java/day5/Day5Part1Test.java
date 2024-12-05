package day5;

import static functionalj.functions.StrFuncs.grab;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day5Part1Test extends BaseTest {
    
    
    Object calulate(FuncList<String> lines) {
        var firstPart = lines.acceptUntil(""::equals);
        var lastPart  = lines.skip(firstPart.count() + 1);
        
        var rules = firstPart.map(grab("[0-9]+"));
        
        rules
        .forEach(this::println);
        println();
        
        var updates = lastPart.map(grab("[0-9]+"));
        
        updates
        .forEach(this::println);
        println();
        
        return updates
        .filter(update -> {
            return rules.allMatch(rule -> {
                var clone = update.toMutableList();
                clone.retainAll(rule);
                if (clone.size() != 2) {
                    return true;
                }
                
                return clone.toString().equals(rule.toString());
            });
        })
        .mapToInt(update -> Integer.parseInt(update.get(update.size() / 2)))
        .sum();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines = readAllLines();
        lines.forEach(this::println);
        println();
        
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("143", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        lines.forEach(this::println);
        println();
        
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("5747", result);
        assertAsString("", result);
    }
    
}
