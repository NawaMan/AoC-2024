package day24;

import static day24.Node.theNode;
import static functionalj.stream.intstream.IntStreamPlus.range;
import static java.util.Comparator.comparing;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import day24.Day24Part1Test.Computer;
import day24.Day24Part1Test.Operator;
import functionalj.list.FuncList;
import functionalj.tuple.Tuple;

/**
 * --- Part Two ---
 * 
 * After inspecting the monitoring device more closely, you determine that the system you're simulating is trying to add
 *   two binary numbers.
 * 
 * Specifically, it is treating the bits on wires starting with x as one binary number, treating the bits on wires 
 *   starting with y as a second binary number, and then attempting to add those two numbers together. The output of 
 *   this operation is produced as a binary number on the wires starting with z. (In all three cases, wire 00 is the 
 *   least significant bit, then 01, then 02, and so on.)
 * 
 * The initial values for the wires in your puzzle input represent just one instance of a pair of numbers that sum to 
 *   the wrong value. Ultimately, any two binary numbers provided as input should be handled correctly. That is, for any
 *   combination of bits on wires starting with x and wires starting with y, the sum of the two numbers those bits 
 *   represent should be produced as a binary number on the wires starting with z.
 * 
 * For example, if you have an addition system with four x wires, four y wires, and five z wires, you should be able to
 *   supply any four-bit number on the x wires, any four-bit number on the y numbers, and eventually find the sum of 
 *   those two numbers as a five-bit number on the z wires. One of the many ways you could provide numbers to such 
 *   a system would be to pass 11 on the x wires (1011 in binary) and 13 on the y wires (1101 in binary):
 * 
 * x00: 1
 * x01: 1
 * x02: 0
 * x03: 1
 * y00: 1
 * y01: 0
 * y02: 1
 * y03: 1
 * 
 * If the system were working correctly, then after all gates are finished processing, you should find 24 (11+13) on 
 *   the z wires as the five-bit binary number 11000:
 * 
 * z00: 0
 * z01: 0
 * z02: 0
 * z03: 1
 * z04: 1
 * 
 * Unfortunately, your actual system needs to add numbers with many more bits and therefore has many more wires.
 * 
 * Based on forensic analysis of scuff marks and scratches on the device, you can tell that there are exactly four pairs
 *   of gates whose output wires have been swapped. (A gate can only be in at most one such pair; no gate's output was 
 *   swapped multiple times.)
 * 
 * For example, the system below is supposed to find the bitwise AND of the six-bit number on x00 through x05 and 
 *   the six-bit number on y00 through y05 and then write the result as a six-bit number on z00 through z05:
 * 
 * x00: 0
 * x01: 1
 * x02: 0
 * x03: 1
 * x04: 0
 * x05: 1
 * y00: 0
 * y01: 0
 * y02: 1
 * y03: 1
 * y04: 0
 * y05: 1
 * 
 * x00 AND y00 -> z05
 * x01 AND y01 -> z02
 * x02 AND y02 -> z01
 * x03 AND y03 -> z03
 * x04 AND y04 -> z04
 * x05 AND y05 -> z00
 * 
 * However, in this example, two pairs of gates have had their output wires swapped, causing the system to produce wrong
 *   answers. The first pair of gates with swapped outputs is x00 AND y00 -> z05 and x05 AND y05 -> z00; the second pair
 *   of gates is x01 AND y01 -> z02 and x02 AND y02 -> z01. Correcting these two swaps results in this system that works
 *   as intended for any set of initial values on wires that start with x or y:
 * 
 * x00 AND y00 -> z00
 * x01 AND y01 -> z01
 * x02 AND y02 -> z02
 * x03 AND y03 -> z03
 * x04 AND y04 -> z04
 * x05 AND y05 -> z05
 * 
 * In this example, two pairs of gates have outputs that are involved in a swap. By sorting their output wires' names 
 *   and joining them with commas, the list of wires involved in swaps is z00,z01,z02,z05.
 * 
 * Of course, your actual system is much more complex than this, and the gates that need their outputs swapped could 
 *   be anywhere, not just attached to a wire starting with z. If you were to determine that you need to swap output 
 *   wires aaa with eee, ooo with z99, bbb with ccc, and aoc with z24, your answer would be aaa,aoc,bbb,ccc,eee,ooo,z24,z99.
 * 
 * Your system of gates and wires has four pairs of gates which need their output wires swapped - eight wires in total. 
 *   Determine which four pairs of gates need their outputs swapped so that your system correctly performs addition; 
 *   what do you get if you sort the names of the eight wires involved in a swap and then join those names with commas?
 */
public class Day24Part2Test extends BaseTest {
    
    Object calculate(FuncList<String> lines) {
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
//        var expecteds = new ConcurrentHashMap<String, String>();
        
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
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("z00,z01,z02,z05", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
}
