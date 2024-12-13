package day17;

import static java.lang.Math.pow;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day17Part1Test extends BaseTest {
    
    static class Context {
        
        long A;
        long B;
        long C;
        int instructionPointer = 0;
        StringBuilder outputBuffer = new StringBuilder();
        
        Context() {}
        Context(long A, long B, long C) {
            this.A = A;
            this.B = B;
            this.C = C;
        }
        
        long value(long operand) {
            switch ((int)operand) {
                case 4: return A;
                case 5: return B;
                case 6: return C;
            }
            return operand;
        }
        
        @Override
        public String toString() {
            return "Context ["
                    + "A=" + A + ", "
                    + "B=" + B + ", "
                    + "C=" + C + ", "
                    + "instPntr=" + instructionPointer + ", "
                    + "output=" + outputBuffer
                    + "]";
        }
        
    }
    
    static interface Operator {
        void work(Context context, long operand);
    }
    
    static class OperatorImpl implements Operator {
        final String   name;
        final Operator operator;
        public OperatorImpl(String name, Operator operator) {
            this.name = name;
            this.operator = operator;
        }
        @Override
        public void work(Context context, long operand) {
            operator.work(context, operand);
        }
        @Override
        public String toString() {
            return name;
        }
    }
    
    static Operator Adv = new OperatorImpl("Adv#0", (context, operand) -> {
        operand   = context.value(operand);
        context.A = context.A / (int)pow(2, operand);
        context.instructionPointer += 2;
    });

    static Operator Bxl = new OperatorImpl("Bxl#1", (context, operand) -> {
        operand   = context.value(operand);
        context.B = context.B ^ operand;
        context.instructionPointer += 2;
    });

    static Operator Bst = new OperatorImpl("Bst#2", (context, operand) -> {
        operand   = context.value(operand);
        context.B = operand % 8;
        context.instructionPointer += 2;
    });

    static Operator Jnz = new OperatorImpl("Jnz#3", (context, operand) -> {
        operand   = context.value(operand);
        if (context.A != 0) {
            context.instructionPointer = (int)operand;
        } else {
            context.instructionPointer += 2;
        }
    });

    static Operator Bxc = new OperatorImpl("Bxc#4", (context, operand) -> {
        operand   = context.value(operand);
        context.B = context.B ^ context.C; // Operand is ignored
        context.instructionPointer += 2;
    });

    static Operator Out = new OperatorImpl("Out#5", (context, operand) -> {
        operand    = context.value(operand);
        long value = operand % 8;
        if (context.outputBuffer.length() > 0) {
            context.outputBuffer.append(",");
        }
        context.outputBuffer.append(value);
        context.instructionPointer += 2;
    });

    static Operator Bdv = new OperatorImpl("Bdv#6", (context, operand) -> {
        operand   = context.value(operand);
        context.B = context.A / (int) pow(2, operand);
        context.instructionPointer += 2;
    });

    static Operator Cdv = new OperatorImpl("Cdv#7", (context, operand) -> {
        operand   = context.value(operand);
        context.C = context.A / (int) pow(2, operand);
        context.instructionPointer += 2;
    });
    
    static FuncList<Operator> operators = FuncList.of(Adv, Bxl, Bst, Jnz, Bxc, Out, Bdv, Cdv);
    
    
    Object calulate(FuncList<String> lines) {
        lines.forEach(this::println);
        println();
        
        var context = new Context();
        context.A = grab(regex("[0-9]+"), lines.get(0)).map(parseInt).get(0);
        context.B = grab(regex("[0-9]+"), lines.get(1)).map(parseInt).get(0);
        context.C = grab(regex("[0-9]+"), lines.get(2)).map(parseInt).get(0);
        context.instructionPointer = 0;
        
        var code = lines.get(4);
        runProgram(context, code);
        
        return context.outputBuffer.toString();
    }

    void runProgram(Context context, String code) {
        var programs = grab(regex("[0-9]+"), code).map(parseInt).cache();
        while (context.instructionPointer < programs.size()) {
            var operator = operators.get(programs.get(context.instructionPointer));
            var operand  = programs.get(context.instructionPointer + 1).intValue();
            System.out.print(operator + "(" + operand + "): ");
            operator.work(context, operand);
            System.out.println(" = " + context);
        }
    }
    
    //== Test ==
    
    public void testProgram(Context context, String program, String expected) {
        runProgram(context, program);
        assertAsString(expected, context);
    }
    
    @Test
    public void testOperator() {
        testProgram(new Context(   0,    0,     9), "2,6",         "Context [A=0, B=1, C=9, instPntr=2, output=]");
        testProgram(new Context(  10,    0,     0), "5,0,5,1,5,4", "Context [A=10, B=0, C=0, instPntr=6, output=0,1,2]");
        testProgram(new Context(2024,    0,     0), "0,1,5,4,3,0", "Context [A=0, B=0, C=0, instPntr=6, output=4,2,5,6,7,7,7,7,3,1,0]");
        testProgram(new Context(   0,   29,     0), "1,7",         "Context [A=0, B=26, C=0, instPntr=2, output=]");
        testProgram(new Context(   0, 2024, 43690), "4,0",         "Context [A=0, B=44354, C=43690, instPntr=2, output=]");
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
        assertAsString("3,0,4,4,1,5,3,0,1", result);
    }
    
}
