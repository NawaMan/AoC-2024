package day17;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongPredicate;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import day17.Day17Part1Test.Context;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

public class Day17Part2Test extends BaseTest {
    
    long calulate(FuncList<String> lines) {
        var code = grab(regex("[0-9]+"), lines.get(4)).mapToInt(parseInt).cache();
        
        for (long a = 0; a < Long.MAX_VALUE; a++) {
            if (a % 1000 == 999)
                println("a: " + a);
            if (calculate(a, code)) {
                return a;
            }
        }
        return -1;
    }
    
    boolean calculate(long a, IntFuncList code) {
        var checkDigit = new AtomicInteger(0);
        var output = (LongPredicate)((long num) -> {
            int digit = checkDigit.get();
            if (digit >= code.size())
                return false;
            if (num != code.get(digit)) {
                return false;
            }
            checkDigit.incrementAndGet();
            return true;
        });
        
        
        var context = new Context(0, 0, 0, output);
        context.A = a;
        context.B = 0;
        context.C = 0;
        
        var codeString = code.toString().replaceAll(" ", "");
        return new Day17Part1Test().runProgram(context, codeString, false)
                && checkDigit.get() == code.size();
        
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("117440", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
}
