package day24;

import static day24.Node.theNode;
import static day24.Node.Eval.theEval;
import static functionalj.stream.intstream.IntStreamPlus.range;
import static java.util.Comparator.comparing;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import day24.Day24Part1Test.Computer;
import day24.Day24Part1Test.Operator;
import functionalj.list.FuncList;
import functionalj.map.FuncMap;
import functionalj.tuple.Tuple;
import functionalj.tuple.Tuple3;

public class Day24Part2Test extends BaseTest {
    
    Object calulate(FuncList<String> lines) {
        // Input of any expression is always right and output may be wrong.
        
        var logicCode
                = lines
                .skipUntil(line -> line.isEmpty())
                .skip     (1)
                .cache();
        var computer = new Computer(logicCode);
        
        var maxDigit
                = logicCode
                .map     (grab(regex("-> z[0-9]+")))
                .filter  (theList.thatIsNotEmpty())
                .map     (theList.first().asString().replaceAll("[^0-9]", ""))
                .mapToInt(parseInt)
                .max()
                .getAsInt();
        
        var inputs = new ConcurrentHashMap<String, Integer>();
        range    (0, maxDigit + 1)
        .mapToObj("%02d"::formatted)
        .forEach (index -> {
            inputs.put("x" + index, 0);
            inputs.put("y" + index, 0);
        });
        println(computer.calculate(inputs));
        println();
        
        // Full adder
        //  A + B = Z
        //  Zn = An XOR Bn XOR Cn_1
        //  Cn = (An AND Bn) OR (Cn_1 AND (An_1 XOR Bn_1))
        
        // An XOR Bn ... is known as partial adder
        // An AND Bn ... will be known now as "has carry"
        
        // So,
        // Any XOR with an input is always for the same digit of the input.
        // Any XOR that are not from input will be same digit with the input that comes from XOR and one after the one that come from OR
        // Any AND are if the same digit
        // Any OR are of the different digits.
        // There is only one OR for output z ... that is the last carry.
        
        var aliases   = new ConcurrentHashMap<String, String>();
        var expecteds = new ConcurrentHashMap<String, String>();
        
        var evals
                = FuncList.from(computer.nodes.values())
                .map        (theNode.asEval.get())
                .excludeNull()
                .map        (e -> Tuple.of(e.operator(), e.name(), FuncList.of(e.input1().name(), e.input2().name()).sorted()))
                .sortedBy   (t -> t._1)
                .cache();
        evals.forEach(println);
        println();
        
        println("Parial adders ");
        var partialAdders
            = evals
            .filter(t -> t._1.equals(Operator.XOR))
            .filter(t -> t._3.allMatch(s -> s.matches("^[xy][0-9]+$")))
            .cache();
        
        partialAdders.forEach(t -> aliases.put(t._2, "p" + t._3.get(0).replaceAll("[^0-9]", "")));
        partialAdders.forEach(println);
        println();
        
        println("Aliases: ");
        aliases.entrySet().forEach(println);
        println();
        
        // TO-CHECK: z00 must be the same with p00
        
        // For everything but 00, pXX will be operated XOR with cYY where YY is one less than XX.
        
        var outputExprs
                = evals
                .filter(t -> t._1.equals(Operator.XOR))
                .filter(t -> t._3.anyMatch(s -> aliases.containsKey(s)))
                .map   (t -> t.map3(operands -> operands.filter(aliases::containsKey).map(aliases::get).get(0)))
                .cache ();
        
        println("Ouptut exprs: ");
        outputExprs.forEach(println);
        println();
        
        var outputNodeWrongs
                = outputExprs
                .filter(t -> t._2().matches("^z[0-9]+$"))
                .filter(t -> t._2().replaceAll("z", "").equals(t._3().replaceAll("p", "")))
                .cache();
        
        println("Output Node Wrongs: ");
        outputNodeWrongs.forEach(println);
        println();
        
        var expectedOutputNodes
                = outputExprs
                .filter(t -> !t._2().matches("^z[0-9]+$"))
                .toMap(t -> t._2(), t -> t._3().replaceAll("p", "z"));
        
        println("Expected Output Node: ");
        expectedOutputNodes
        .entries()
        .forEach(println);
        println();
        
        var outputExprPrevious
                = evals
                .filter(t -> t._1.equals(Operator.XOR))
                .filter(t -> t._3.anyMatch(s -> aliases.containsKey(s)))
                .map   (t -> FuncList.of(aliases.getOrDefault(t._3.get(0), t._3.get(0)),
                                         aliases.getOrDefault(t._3.get(1), t._3.get(1)))
                                     .sorted(comparing(name -> (name.matches("^p[0-9]+") ? 1 : 2))))
                .cache ();
        
        outputExprPrevious
        .forEach(pair -> {
            var partialAdder  = pair.get(0);
            var previousCarry = pair.get(1);
            
            var previous = parseInt(partialAdder.replaceAll("p", "")) - 1;
            aliases.put(previousCarry, "c%02d".formatted(previous));
        });
        
        println("Output Expr Previous: ");
        outputExprPrevious
        .forEach(println);
        println();
        
        println("Aliases: ");
        aliases.entrySet().forEach(println);
        println();
        
        var hasCarries
                = evals
                .filter(t -> t._1.equals(Operator.AND))
                .filter(t -> t._3.allMatch(s -> s.matches("^[xy][0-9]+$")))
                .cache();
        hasCarries
        .forEach(println);
        println();
        
        // Any of the name (that is not pXX) are carry of the previous digit.
        // And they must be a result of an OR operation of the previous digit
        
        
//        
//        // Output of XOR of partial adders must be z
//        evals
//        .filter(t -> t._1.equals(Operator.XOR))
//        .filter(t -> partialAdderNames.contains(t._2))
//        .filter(t -> aliases.get(t._2).equals(t))
//        ;
//        
//        var inputExprs
//                = FuncList.from(computer.nodes.values())
//                .map        (theNode.asEval.get())
//                .excludeNull()
//                .filter     (theEval.input1.asRef.get().name.thatMatches("[xy][0-9]{2}"))
//                .map        (e -> Tuple.of(e.operator(), e.input1().asRef().get().name().replaceAll("[xy]", ""), e.name()))
//                .sortedBy   (String::valueOf)
//                .cache();
//        
//        println("Input expressions: ");
//        inputExprs
//        .forEach(println);
//        println();
//        
//        // We must check that 
//        
//        var outputExprs
//                = FuncList.from(computer.nodes.values())
//                .map        (theNode.asEval.get())
//                .excludeNull()
//                .filter     (theEval.name.thatMatches("z[0-9]{2}"))
//                .map        (e -> Tuple.of(e.operator(), e.name(), e.input1().name(), e.input2().name()))
//                .sortedBy   (String::valueOf)
//                .cache();
//        
//        println("Output expressions: ");
//        outputExprs
//        .forEach(println);
//        println();
        
        return computer.calculate(inputs);
    }
    
    //== Test ==
    
    @Ignore
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("z00,z01,z02,z05", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
}
