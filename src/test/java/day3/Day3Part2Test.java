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
                .prepend    ("do()")                                    // -- A
                .segmentWhen(cmd     -> cmd.startsWith("do"))           // -- B
                .exclude    (segment -> segment.contains("don't()"))    // -- C
                .mapToInt   (segment -> segmentSum(segment))            // -- D
                .sum();                                                 // -- E
        
        // == Input ==
        // xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))
        
        // == A ==
        //    do()
        //    mul(2,4)
        //    don't()
        //    mul(5,5)
        //    mul(11,8)
        //    do()
        //    mul(8,5)
        
        // == B ==
        //    [do(), mul(2,4)]
        //    [don't(), mul(5,5), mul(11,8)]
        //    [do(), mul(8,5)]
        
        // == C ==
        //    [do(), mul(2,4)]
        //    [do(), mul(8,5)]
        
        // == D ==
        //    8
        //    40
        
        // == E ==
        //    48
    }
    
    int segmentSum(FuncList<String> segment) {
        return segment
                .skip(1)    // Skip the first `do()`
                .mapToInt(this::calculateMul)
                .sum();
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
