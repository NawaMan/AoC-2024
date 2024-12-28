package day21;

import static functionalj.function.Func.f;

import org.junit.Test;

import common.BaseTest;
import day21.Day21Part1Test.ArrowPad;
import day21.Day21Part1Test.NumberPad;
import functionalj.function.Func;
import functionalj.list.FuncList;
import functionalj.pipeable.Pipeable;

public class Day21Part2Test extends BaseTest {
    
    Object calculate(FuncList<String> lines) {
        var doorPad    = new NumberPad();
        int robotCount = 2;
        var robotPads  = FuncList.generate(() -> f(ArrowPad::new)).limit(robotCount);
        var calculate  = Func.f((String target) -> {
            // Reset all pads
            doorPad.reset();
            robotPads.forEach(ArrowPad::reset);
            
            var shortest
                    = Pipeable.of(target)
                    .pipeTo(k -> doorPad.determineKeyPressed(k));
            
            for (ArrowPad robotPad : robotPads) {
                shortest = shortest.flatMap(k -> robotPad.determineKeyPressed(k));
            }
            
            var shortestPath = shortest.minBy(String::length);
            var numberPart   = Long.parseLong(target.replaceAll("[^0-9]+", ""));
            println(target + ": " + (long) shortestPath.get().length() + " -- " + numberPart);
            return numberPart * (long) shortestPath.get().length();
        });
        
        return lines.sumToLong(calculate::apply);
    }
    
    //== Test ==
    
//    @Ignore("Not asked.")
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
}
