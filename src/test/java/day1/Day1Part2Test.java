package day1;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day1Part2Test extends BaseTest {
    
    int calulate(FuncList<String> lines) {
        var pairs = lines.map(line -> line.split("[ ]+")).toFuncList();
        var left  = pairs.map(pair -> pair[0]).mapToInt(Integer::parseInt);
        var right = pairs.map(pair -> pair[1]).mapToInt(Integer::parseInt);
        return left.map(i -> i * (int)right.filterIn(i).count()).sum();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines = readAllLines();
        lines.forEach(this::println);
        
        var result = calulate(lines);
        assertAsString("31", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        lines.forEach(this::println);
        
        var result = calulate(lines);
        assertAsString("21607792", result);
    }
    
}
