package day1;

import static java.lang.Math.abs;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day1Part1Test extends BaseTest {
    
    int calulate(FuncList<String> lines) {
        var pairs = lines.map(line -> line.split("[ ]+"));
        var left  = pairs.map(pair -> pair[0]).mapToInt(parseInt).sorted();
        var right = pairs.map(pair -> pair[1]).mapToInt(parseInt).sorted();
        return left.zipWith(right, (a, b) -> abs(a - b)).sum();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("11", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("1834060", result);
    }
    
}


