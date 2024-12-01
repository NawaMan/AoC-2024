package day1;

import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day1Part1Test extends BaseTest {
    
    int calulate(FuncList<String> lines) {
        var pairs = lines.map(line -> line.split("[ ]+")).cache();
        var left  = pairs.mapToInt(pair -> parseInt(pair[0])).sorted();
        var right = pairs.mapToInt(pair -> parseInt(pair[1])).sorted();
        return left.zipWith(right, (a, b) -> abs(a - b)).sum();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        assertAsString("11", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        assertAsString("1834060", result);
    }
    
}


