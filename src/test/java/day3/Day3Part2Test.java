package day3;

import java.util.regex.Pattern;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

/**
 * --- Part Two ---
 * 
 * As you scan through the corrupted memory, you notice that some of the conditional statements are also still intact.
 *   If you handle some of the uncorrupted conditional statements in the program, you might be able to get an even more
 *   accurate result.
 * 
 * There are two new instructions you'll need to handle:
 * 
 *     The do() instruction enables future mul instructions.
 *     The don't() instruction disables future mul instructions.
 * 
 * Only the most recent do() or don't() instruction applies. At the beginning of the program, mul instructions are
 *   enabled.
 * 
 * For example:
 * 
 * xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))
 * 
 * This corrupted memory is similar to the example from before, but this time the mul(5,5) and mul(11,8) instructions
 *   are disabled because there is a don't() instruction before them. The other mul instructions function normally,
 *   including the one at the end that gets re-enabled by a do() instruction.
 * 
 * This time, the sum of the results is 48 (2*4 + 8*5).
 * 
 * Handle the new instructions; what do you get if you add up all of the results of just the enabled multiplications?
 * 
 * Your puzzle answer was 98729041.
 */
public class Day3Part2Test extends BaseTest {
    
    Pattern commandPattern = regex("(mul\\([0-9]{1,3},[0-9]{1,3}\\)|do\\(\\)|don't\\(\\))");
    Pattern numberPattern  = regex("[0-9]+");
    
    int calculate(FuncList<String> lines) {
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
        var result = calculate(lines);
        println(result);
        assertAsString("48", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println(result);
        assertAsString("98729041", result);
    }
    
}
