package day1;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day1Part2Test extends BaseTest {
    
    int calulate(FuncList<String> lines) {
        var pairs = lines.map(line -> line.split("[ ]+"));
        var left  = pairs.map(pair -> pair[0]).mapToInt(parseInt);
        var right = pairs.map(pair -> pair[1]).mapToInt(parseInt);
        return left.map(i -> i * right.filterIn(i).size()).sum();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("31", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("21607792", result);
    }
    
}
