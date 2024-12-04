package day1;

import static java.lang.Integer.parseInt;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day1Part2Test extends BaseTest {
    
    int calulate(FuncList<String> lines) {
        var pairs = lines.map(line -> line.split("[ ]+")).cache();
        var left  = pairs.mapToInt(pair -> parseInt(pair[0]));
        var right = pairs.mapToInt(pair -> parseInt(pair[1]));
        return left.map(i -> i * right.filterIn(i).size()).sum();
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
        assertAsString("31", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        lines.forEach(this::println);
        println();
        
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("21607792", result);
    }
    
}
