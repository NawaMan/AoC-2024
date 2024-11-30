package day2;

import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import common.AocCommon;
import common.Testable;

@Ignore
public class Day2Part2Test implements AocCommon, Testable {

    
    //== Test ==
    
    @Test
    public void testDemo() {
        var lines = readAllLines(test, challengeName());
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
