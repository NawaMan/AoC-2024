package day3;

import java.util.regex.Pattern;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day3Part1Test extends BaseTest {
    
    Pattern mulPattern    = regex("mul\\([0-9]{1,3},[0-9]{1,3}\\)");
    Pattern numberPattern = regex("[0-9]+");
    
    int calulate(FuncList<String> lines) {
        return lines.mapToInt(this::calculateTotal).sum();
    }
    
    int calculateTotal(String line) {
        return grab(mulPattern, line)
                .sumToInt(this::calculateMul);
    }
    
    private int calculateMul(String mul) {
        return grab(numberPattern, mul)
                .mapToInt(parseInt)
                .product()
                .getAsInt();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println(result);
        assertAsString("161", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println(result);
        assertAsString("181345830", result);
    }
    
}
