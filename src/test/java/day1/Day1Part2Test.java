package day1;

import static org.junit.Assert.fail;

import org.junit.Test;

import common.AocCommon;
import common.Testable;

public class Day1Part2Test implements AocCommon, Testable {

    
    //== Test ==
    
    @Test
    public void testDemo() {
        var lines = readAllLines(demo, challengeName());
        System.out.println(lines);
        
        
        //assertAsString("", ...);
        fail("No assertion!");
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines(prod, challengeName());
        System.out.println(lines);
        
        
        
        //assertAsString("", ...);
        fail("No assertion!");
    }
    
}
