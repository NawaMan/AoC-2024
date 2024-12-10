package day7;

import static java.lang.Math.pow;

import java.math.BigInteger;
import java.util.function.BinaryOperator;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

/**
 * --- Day 7: Bridge Repair ---
 * 
 * The Historians take you to a familiar rope bridge over a river in the middle of a jungle. The Chief isn't on this 
 *   side of the bridge, though; maybe he's on the other side?
 * 
 * When you go to cross the bridge, you notice a group of engineers trying to repair it. (Apparently, it breaks pretty 
 *   frequently.) You won't be able to cross until it's fixed.
 * 
 * You ask how long it'll take; the engineers tell you that it only needs final calibrations, but some young elephants 
 *   were playing nearby and stole all the operators from their calibration equations! They could finish the 
 *   calibrations if only someone could determine which test values could possibly be produced by placing any 
 *   combination of operators into their calibration equations (your puzzle input).
 * 
 * For example:
 * 
 * 190: 10 19
 * 3267: 81 40 27
 * 83: 17 5
 * 156: 15 6
 * 7290: 6 8 6 15
 * 161011: 16 10 13
 * 192: 17 8 14
 * 21037: 9 7 18 13
 * 292: 11 6 16 20
 * 
 * Each line represents a single equation. The test value appears before the colon on each line; it is your job to 
 *   determine whether the remaining numbers can be combined with operators to produce the test value.
 * 
 * Operators are always evaluated left-to-right, not according to precedence rules. Furthermore, numbers in the 
 *   equations cannot be rearranged. Glancing into the jungle, you can see elephants holding two different types of 
 *   operators: add (+) and multiply (*).
 * 
 * Only three of the above equations can be made true by inserting operators:
 * 
 *     190: 10 19 has only one position that accepts an operator: between 10 and 19. Choosing + would give 29, but 
 *           choosing * would give the test value (10 * 19 = 190).
 *     3267: 81 40 27 has two positions for operators. Of the four possible configurations of the operators, two cause 
 *           the right side to match the test value: 81 + 40 * 27 and 81 * 40 + 27 both equal 3267 (when evaluated 
 *           left-to-right)!
 *     292: 11 6 16 20 can be solved in exactly one way: 11 + 6 * 16 + 20.
 * 
 * The engineers just need the total calibration result, which is the sum of the test values from just the equations 
 *   that could possibly be true. In the above example, the sum of the test values for the three equations listed above 
 *   is 3749.
 * 
 * Determine which equations could possibly be true. What is their total calibration result?
 * 
 * Your puzzle answer was 1611660863222.
 */
public class Day7Part1Test extends BaseTest {
    
    BinaryOperator<BigInteger> newOperator(String name, BinaryOperator<BigInteger> body) {
        return new BinaryOperator<BigInteger>() {
            @Override public BigInteger apply(BigInteger left, BigInteger right) { return body.apply(left, right); }
            @Override public String toString() { return name; }
        };
    }
    
    FuncList<BinaryOperator<BigInteger>> operators = FuncList.of(
            newOperator("+", BigInteger::add),
            newOperator("*", BigInteger::multiply)
    );
    
    BigInteger countValidExpression(FuncList<String> lines) {
        return lines
            .map   (grab(regex("[0-9]+")))
            .map   (each -> each.map(BigInteger::new))
            .filter(each -> checkIfPossible(each))
            .map   (each -> each.first().get())
            .reduce(BigInteger::add)
            .get();
    }
    
    boolean checkIfPossible(FuncList<BigInteger> each) {
        var result    = each.first().get();
        var operands  = each.tail().cache();
        var caseCount = (int)pow(2, operands.size() - 1);
        for (int thisCase = 0; thisCase < caseCount; thisCase++) {
            if (result.equals(calculate(operands, thisCase)))
                return true;
        }
        return false;
    }
    
    BigInteger calculate(FuncList<BigInteger> operands, int thisCase) {
        var total         = operands.get(0);
        var operatorCount = operators.size();
        for (int index = 0; index < operands.size() - 1; index++) {
            var operator = operators.get  (thisCase % operatorCount);
            var operand  = operands .get  (index + 1);
            total        = operator .apply(total, operand);
            thisCase /= operatorCount;
        }
        return total;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = countValidExpression(lines);
        println("result: " + result);
        assertAsString("3749", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = countValidExpression(lines);
        println("result: " + result);
        assertAsString("1611660863222", result);
    }
    
}
