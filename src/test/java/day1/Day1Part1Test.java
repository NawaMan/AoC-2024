package day1;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day1Part1Test extends BaseTest {
    
    int calulate(FuncList<String> lines) {
        var pairs = lines.map(line -> line.split("[ ]+")).toFuncList();
        var left  = pairs.map(pair -> pair[0]).mapToInt(Integer::parseInt).sorted();
        var right = pairs.map(pair -> pair[1]).mapToInt(Integer::parseInt).sorted();
        return left.zipWith(right, (a, b) -> a - b).map(Math::abs).sum();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines = readAllLines();
        lines.forEach(this::println);
        
        var result = calulate(lines);
        
        assertAsString("11", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        lines.forEach(this::println);
        
        var result = calulate(lines);
        
        assertAsString("1834060", result);
    }
    
}


