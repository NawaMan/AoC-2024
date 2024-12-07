package day7;

import static functionalj.functions.StrFuncs.*;
import static java.lang.Math.pow;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;
import lombok.ToString;

public class Day7Part1Test extends BaseTest {
    
    
    Object calulate(FuncList<String> lines) {
        return lines
            .map(grab(regex("[0-9]+")))
            .map(each -> each.map(BigInteger::new))
            .filter(each -> checkIfPossible(each))
            .map(each -> each.first().get())
            .reduce(BigInteger::add)
            .get();
    }
    
    boolean checkIfPossible(FuncList<BigInteger> each) {
        var result = each.first().get();
        var nums   = each.tail().cache();
        
        var posibility = (int)pow(2, nums.size() - 1);
        var operators = new BinaryOperator[] {
                new BinaryOperator<BigInteger>() {
                    @Override
                    public BigInteger apply(BigInteger left, BigInteger right) {
                        return left.add(right);
                    }
                    @Override
                    public String toString() {
                        return "+";
                    }
                    
                },
               new BinaryOperator<BigInteger>() {
                    @Override
                    public BigInteger apply(BigInteger left, BigInteger right) {
                        return left.multiply(right);
                    }
                    @Override
                    public String toString() {
                        return "*";
                    }
                    
                }
        };
//        println(Arrays.toString(operators));
        for (int bits = 0; bits < posibility; bits++) {
            if (result.equals(checkForBits(nums, operators, bits)))
                return true;
        }
        return false;
    }
    
    BigInteger checkForBits(FuncList<BigInteger> nums, BinaryOperator[] operators, int bits) {
//        println("nums: " + nums);
//        println("bits: " + bits);
        int bit = 1;
        var thisOperators = new ArrayList<BinaryOperator>();
        for (int b = 0; b < nums.size() - 1; b++) {
            var thisBit = bits & bit;
            var thisOperator = operators[thisBit != 0 ? 1 : 0];
            thisOperators.add(thisOperator);
            bit *= 2;
        }
        BigInteger total = nums.get(0);
//        System.out.print(total);
        for (int i = 1; i < nums.size(); i++) {
            var thisOperator = thisOperators.get(i - 1);
            var newNum       = nums.get(i);
//            System.out.print(thisOperator);
//            System.out.print(newNum);
            total = (BigInteger)thisOperator.apply(total, newNum); 
        }
//        System.out.print("=" + total);
        return total;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("3749", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("1611660863222", result);
    }
    
}
