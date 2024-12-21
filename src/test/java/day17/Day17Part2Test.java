package day17;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntPredicate;

import org.junit.Test;

import common.BaseTest;
import day17.Day17Part1Test.Context;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

public class Day17Part2Test extends BaseTest {
    
    Object calulate(FuncList<String> lines) {
        lines.forEach(this::println);
        println();
        
        return null;
    }
    
    long calculate(int a, IntFuncList code) {
        var checkDigit = new AtomicInteger(0);
        var output = (IntPredicate)((int num) -> {
//            if (code.get(checkDigit.get())) {
//                
//            }
            return true;
        });
        
        
        var context = new Context(0, 0, 0, output);
        context.A = a;
        context.B = 0;
        context.C = 0;
        
        var codeString = code.toString().replaceAll(" ", "");
        new Day17Part1Test().runProgram(context, codeString, false);
        
        return 0;
        
    }

//    void runProgram(Context context, String code) {
//        var programs = grab(regex("[0-9]+"), code).map(parseInt).cache();
//        while (context.instructionPointer < programs.size()) {
//            var operator = operators.get(programs.get(context.instructionPointer));
//            var operand  = programs.get(context.instructionPointer + 1).intValue();
//            System.out.print(operator + "(" + operand + "): ");
//            operator.work(context, operand);
//            System.out.println(" = " + context);
//        }
//    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString(lines.last().get().replaceAll("Program: ", ""), result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString(lines.last().get().replaceAll("Program: ", ""), result);
    }
    
}
