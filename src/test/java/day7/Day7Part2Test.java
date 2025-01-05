package day7;

import static functionalj.stream.intstream.IntStreamPlus.range;
import static java.lang.Math.pow;

import java.math.BigInteger;
import java.util.function.BinaryOperator;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

/**
 * --- Part Two ---
 * 
 * The engineers seem concerned; the total calibration result you gave them is nowhere close to being within safety 
 *   tolerances. Just then, you spot your mistake: some well-hidden elephants are holding a third type of operator.
 * 
 * The concatenation operator (||) combines the digits from its left and right inputs into a single number. For example,
 *   12 || 345 would become 12345. All operators are still evaluated left-to-right.
 * 
 * Now, apart from the three equations that could be made true using only addition and multiplication, the above example
 *   has three more equations that can be made true by inserting operators:
 * 
 *     156: 15 6 can be made true through a single concatenation: 15 || 6 = 156.
 *     7290: 6 8 6 15 can be made true using 6 * 8 || 6 * 15.
 *     192: 17 8 14 can be made true using 17 || 8 + 14.
 * 
 * Adding up all six test values (the three that could be made before using only + and * plus the new three that can now
 *   be made by also using ||) produces the new total calibration result of 11387.
 * 
 * Using your new knowledge of elephant hiding spots, determine which equations could possibly be true. What is their
 *   total calibration result?
 * 
 * Your puzzle answer was 945341732469724.
 */
public class Day7Part2Test extends BaseTest {
    
    BinaryOperator<BigInteger> newOperator(String name, BinaryOperator<BigInteger> body) {
        return new BinaryOperator<BigInteger>() {
            @Override public BigInteger apply(BigInteger left, BigInteger right) { return body.apply(left, right); }
            @Override public String toString() { return name; }
        };
    }
    
    FuncList<BinaryOperator<BigInteger>> operators = FuncList.of(
            newOperator("+", BigInteger::add),
            newOperator("*", BigInteger::multiply),
            newOperator("||", (left, right) -> new BigInteger(left.toString() + right.toString()))
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
        var caseCount = (int)pow(3, operands.size() - 1);
        return range(0, caseCount)
                .mapToObj(caseIndex -> calculate(operands, caseIndex))
                .anyMatch(value     -> result.equals(value));
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
        assertAsString("11387", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = countValidExpression(lines);
        println("result: " + result);
        assertAsString("945341732469724", result);
    }
    
}
