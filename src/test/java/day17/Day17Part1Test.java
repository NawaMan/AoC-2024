package day17;

import static java.lang.Math.pow;

import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncListBuilder;

public class Day17Part1Test extends BaseTest {
    
    static class Context {
        
        int A;
        int B;
        int C;
        int instructionPointer = 0;
        final IntPredicate output;
        
        Context(int a, int b, int c, IntConsumer output) {
            this(a, b, c, 
                (output == null)
                    ? (i -> {                   return true; })
                    : (i -> { output.accept(i); return true; }));
        }
        Context(int a, int b, int c, IntPredicate output) {
            this.A = a;
            this.B = b;
            this.C = c;
            this.output = (output != null) ? output : i -> true;
        }
        
        int value(int operand) {
            if ((operand >= 0) && (operand <= 3))
                return operand;
            
            switch ((int)operand) {
                case 4: return A;
                case 5: return B;
                case 6: return C;
            }
            throw new IllegalArgumentException("operand: " + operand);
        }
        
        @Override
        public String toString() {
            return "Context ["
                    + "A=" + A + ", "
                    + "B=" + B + ", "
                    + "C=" + C + ", "
                    + "instPntr=" + instructionPointer + ", "
                    + "]";
        }
        
    }
    
    static interface OperatorBody {
        boolean work(Context context, int operand);
    }
    
    static class Operator implements OperatorBody {
        final String       name;
        final OperatorBody operator;
        public Operator(String name, OperatorBody operator) {
            this.name = name;
            this.operator = operator;
        }
        @Override
        public boolean work(Context context, int operand) {
            return operator.work(context, operand);
        }
        @Override
        public String toString() {
            return name;
        }
    }
    
    static OperatorBody Adv = new Operator("Adv#0", (context, operand) -> {
        operand   = context.value(operand);
        context.A = context.A / (int)pow(2, operand);
        return true;
    });

    static OperatorBody Bxl = new Operator("Bxl#1", (context, operand) -> {
        context.B = context.B ^ operand;
        return true;
    });

    static OperatorBody Bst = new Operator("Bst#2", (context, operand) -> {
        operand   = context.value(operand);
        context.B = operand & 7;
        return true;
    });

    static OperatorBody Jnz = new Operator("Jnz#3", (context, operand) -> {
        if (context.A != 0) {
            context.instructionPointer = (int)operand - 2;
        }
        return true;
    });

    static OperatorBody Bxc = new Operator("Bxc#4", (context, operand) -> {
        context.B = context.B ^ context.C; // Operand is ignored
        return true;
    });

    static OperatorBody Out = new Operator("Out#5", (context, operand) -> {
        operand   = context.value(operand);
        var value = operand & 7;
        return context.output.test(value);
    });

    static OperatorBody Bdv = new Operator("Bdv#6", (context, operand) -> {
        operand   = context.value(operand);
        context.B = context.A / (int) pow(2, operand);
        return true;
    });

    static OperatorBody Cdv = new Operator("Cdv#7", (context, operand) -> {
        operand   = context.value(operand);
        context.C = context.A / (int) pow(2, operand);
        return true;
    });
    
    static FuncList<OperatorBody> operators = FuncList.of(Adv, Bxl, Bst, Jnz, Bxc, Out, Bdv, Cdv);
    
    
    Object calulate(FuncList<String> lines) {
        lines.forEach(this::println);
        println();
        
        var output  = new IntFuncListBuilder();
        var context = new Context(0, 0, 0, out -> { output.add(out); });
        context.A = grab(regex("[0-9]+"), lines.get(0)).map(parseInt).get(0);
        context.B = grab(regex("[0-9]+"), lines.get(1)).map(parseInt).get(0);
        context.C = grab(regex("[0-9]+"), lines.get(2)).map(parseInt).get(0);
        
        var code = lines.get(4);
        runProgram(context, code, false);
        
        return output.build().mapToObj(String::valueOf).join(",");
    }

    boolean runProgram(Context context, String code, boolean isDebug) {
        var programs = grab(regex("[0-9]+"), code).map(parseInt).cache();
        while (context.instructionPointer < programs.size()) {
            var operator = operators.get(programs.get(context.instructionPointer));
            var operand  = programs.get(context.instructionPointer + 1).intValue();
            
            if (isDebug) System.out.print(operator + "(" + operand + "): ");
            
            var shouldContinue = operator.work(context, operand);
            context.instructionPointer += 2;
            
            if (!shouldContinue)
                return false;
            
            if (isDebug) System.out.println(" = " + context);
        }
        return true;
    }
    
    //== Test ==
    
    public void testProgram(int a, int b, int c, String program, String expectedContext, String expectedOutput) {
        var output  = new IntFuncListBuilder();
        var context = new Context(a, b, c, out -> { output.add(out); });
        runProgram(context, program, true);
        assertAsString(expectedContext, context);
        assertAsString(expectedOutput,  output.build().mapToObj(String::valueOf).join(","));
    }
    
    @Ignore
    @Test
    public void testOperator() {
        testProgram(   0,    0,     9, "2,6",         "Context [A=0, B=1, C=9, instPntr=2]",         "");
        testProgram(  10,    0,     0, "5,0,5,1,5,4", "Context [A=10, B=0, C=0, instPntr=6]",        "0,1,2");
        testProgram(2024,    0,     0, "0,1,5,4,3,0", "Context [A=0, B=0, C=0, instPntr=6]",         "4,2,5,6,7,7,7,7,3,1,0");
        testProgram(   0,   29,     0, "1,7",         "Context [A=0, B=26, C=0, instPntr=2]",        "");
        testProgram(   0, 2024, 43690, "4,0",         "Context [A=0, B=44354, C=43690, instPntr=2]", "");
    }
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("4,6,3,5,6,3,5,2,1,0", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("6,2,7,2,3,1,6,0,5", result);
    }
    
}
