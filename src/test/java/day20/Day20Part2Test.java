package day20;

import org.junit.Test;

import common.BaseTest;

public class Day20Part2Test extends BaseTest {
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = new Day20Part1Test().calulate(lines, 20, 100);
        println("result: " + result);
        assertAsString("0", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = new Day20Part1Test().calulate(lines, 20, 100);
        println("result: " + result);
        assertAsString("982474", result);
    }
    
}
