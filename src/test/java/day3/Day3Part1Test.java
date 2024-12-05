package day3;

import static functionalj.functions.StrFuncs.grab;

import java.util.regex.Pattern;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day3Part1Test extends BaseTest {
    
    Pattern mulPattern    = Pattern.compile("mul\\([0-9]{1,3},[0-9]{1,3}\\)");
    Pattern numberPattern = Pattern.compile("[0-9]+");
    
    int calulate(FuncList<String> lines) {
        return lines.mapToInt(this::calculateTotal).sum();
    }
    
    int calculateTotal(String line) {
        return grab(line, mulPattern)
                .peek(this::println)
                .sumToInt(this::calculateMul);
    }
    
    private int calculateMul(String mul) {
        return grab(mul, numberPattern)
                .mapToInt(Integer::parseInt)
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
