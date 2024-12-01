package day2;

import org.junit.Ignore;
import org.junit.Test;

import common.AocCommon;
import common.Testable;
import functionalj.list.FuncList;

@Ignore
public class Day2Part1Test implements AocCommon, Testable {
    
    
    Object calulate(FuncList<String> lines) {
        return null;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines = readAllLines();
        System.out.println(lines);
        
        var result = calulate(lines);
        assertAsString("", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        System.out.println(lines);
        
        var result = calulate(lines);
        assertAsString("", result);
    }
    
}
