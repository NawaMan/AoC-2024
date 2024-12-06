package day3;

import java.util.regex.Pattern;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day3Part2Test extends BaseTest {
    
    Pattern commandPattern = regex("(mul\\([0-9]{1,3},[0-9]{1,3}\\)|do\\(\\)|don't\\(\\))");
    Pattern numberPattern  = regex("[0-9]+");
    
    int calulate(FuncList<String> lines) {
        var code = lines.join(" ");
        
        return grab(commandPattern, code)
                .prepend    ("do()")
                .segmentWhen(cmd     -> cmd.startsWith("do"))
                .exclude    (segment -> segment.contains("don't()"))
                .mapToInt   (segment -> segmentSum(segment))
                .sum();
    }
    
    int segmentSum(FuncList<String> segment) {
        return segment
                .skip(1)
                .mapToInt(this::calculateMul).sum();
    }
    
    int calculateMul(String mul) {
        return grab(numberPattern, mul)
                .mapToInt(parseInt)
                .product()
                .orElse(1);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println(result);
        assertAsString("48", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println(result);
        assertAsString("98729041", result);
    }
    
}
