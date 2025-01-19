package day17;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongPredicate;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import day17.Day17Part1Test.Context;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

/**
 * --- Part Two ---
 * 
 * Digging deeper in the device's manual, you discover the problem: this program is supposed to output another copy of the program! Unfortunately, the value in register A seems to have been corrupted. You'll need to find a new value to which you can initialize register A so that the program's output instructions produce an exact copy of the program itself.
 * 
 * For example:
 * 
 * Register A: 2024
 * Register B: 0
 * Register C: 0
 * 
 * Program: 0,3,5,4,3,0
 * 
 * This program outputs a copy of itself if register A is instead initialized to 117440. (The original initial value of register A, 2024, is ignored.)
 * 
 * What is the lowest positive initial value for register A that causes the program to output a copy of itself?
 */
@Ignore
public class Day17Part2Test extends BaseTest {
    
    static final long START = 100000000000L;
    static final long END   = 110000000000L;
    
    long calculate(FuncList<String> lines) {
        var code = grab(regex("[0-9]+"), lines.get(4)).mapToInt(parseInt).cache();
        
        for (long a = START; a < END; a++) {
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
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("117440", result);
    }
    
    @Ignore
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
}
