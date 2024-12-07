package day7;

import static java.lang.Math.pow;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.function.BinaryOperator;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

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
    
    Object calulate(FuncList<String> lines) {
        return lines
            .map   (grab(regex("[0-9]+")))
            .map   (each -> each.map(BigInteger::new))
            .filter(each -> checkIfPossible(each))
            .map   (each -> each.first().get())
            .reduce(BigInteger::add)
            .get();
    }
    
    boolean checkIfPossible(FuncList<BigInteger> each) {
        var result = each.first().get();
        var nums   = each.tail().cache();
        
        var caseCount = (int)pow(3, nums.size() - 1);
        for (int thisCase = 0; thisCase < caseCount; thisCase++) {
            if (result.equals(checkForBits(nums, thisCase)))
                return true;
        }
        return false;
    }
    
    BigInteger checkForBits(FuncList<BigInteger> nums, int thisCase) {
        var thisOperators = new ArrayList<BinaryOperator<BigInteger>>();
        for (int operatorIndex = 0; operatorIndex < nums.size() - 1; operatorIndex++) {
            var thisBit = thisCase % 3;
            var thisOperator = operators.get(thisBit);
            thisOperators.add(thisOperator);
            thisCase /= 3;
        }
        BigInteger total = nums.get(0);
        for (int operationIndex = 1; operationIndex < nums.size(); operationIndex++) {
            var operator = thisOperators.get(operationIndex - 1);
            var operand  = nums.get(operationIndex);
            total = operator.apply(total, operand); 
        }
        return total;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("11387", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("945341732469724", result);
    }
    
}
