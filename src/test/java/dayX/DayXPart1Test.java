package dayX;

import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import common.AocCommon;
import common.Testable;

@Ignore
public class DayXPart1Test implements AocCommon, Testable {
    
    
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
