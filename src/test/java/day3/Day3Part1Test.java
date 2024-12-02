package day3;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

@Ignore
public class Day3Part1Test extends BaseTest {
    
    
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
    
    @Ignore
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
