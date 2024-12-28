package day25;

import static functionalj.functions.StrFuncs.*;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

@SuppressWarnings("unused")
@Ignore
public class Day25Part2Test extends BaseTest {
    
    
    Object calculate(FuncList<String> lines) {
        lines.forEach(this::println);
        println();
        
        return null;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
    @Ignore
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
}
