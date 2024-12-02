package dayX;

import org.junit.Ignore;
import org.junit.Test;

import common.AocCommon;
import common.Testable;
import functionalj.list.FuncList;

@Ignore
public class DayXPart1Test implements AocCommon, Testable {
    
    
    Object calulate(FuncList<String> lines) {
        return null;
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
        assertAsString("", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        lines.forEach(this::println);
        println();
        
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("", result);
    }
    
}
