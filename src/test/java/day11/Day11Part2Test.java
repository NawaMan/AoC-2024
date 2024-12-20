package day11;

import org.junit.Test;

import common.BaseTest;

public class Day11Part2Test extends BaseTest {
    
    static Day11Part1Test part1 = new Day11Part1Test();
    
    //== Test ==

    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = part1.calulate(lines, 75);
        println("result: " + result);
        assertAsString("65601038650482", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = part1.calulate(lines, 75);
        println("result: " + result);
        assertAsString("232454623677743", result);
    }
    
}
