package day21;

import static functionalj.function.Func.f;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import day21.Day21Part1Test.ArrowPad;
import day21.Day21Part1Test.NumberPad;
import functionalj.function.Func;
import functionalj.list.FuncList;
import functionalj.pipeable.Pipeable;

/**
 * --- Part Two ---
 * 
 * Just as the missing Historian is released, The Historians realize that a second member of their search party has also
 *   been missing this entire time!
 * 
 * A quick life-form scan reveals the Historian is also trapped in a locked area of the ship. Due to a variety of hazards,
 *   robots are once again dispatched, forming another chain of remote control keypads managing robotic-arm-wielding robots.
 * 
 * This time, many more robots are involved. In summary, there are the following keypads:
 * 
 *     One directional keypad that you are using.
 *     25 directional keypads that robots are using.
 *     One numeric keypad (on a door) that a robot is using.
 * 
 * The keypads form a chain, just like before: your directional keypad controls a robot which is typing on a directional
 *   keypad which controls a robot which is typing on a directional keypad... and so on, ending with the robot which is
 *   typing on the numeric keypad.
 * 
 * The door codes are the same this time around; only the number of robots and directional keypads has changed.
 * 
 * Find the fewest number of button presses you'll need to perform in order to cause the robot in front of the door 
 *   to type each code. What is the sum of the complexities of the five codes on your list?
 */
@Ignore("Out of memory .... .")
public class Day21Part2Test extends BaseTest {
    
    Object calculate(FuncList<String> lines) {
        var doorPad    = new NumberPad();
        int robotCount = 2; // 25
        var robotPads  = FuncList.generate(() -> f(ArrowPad::new)).limit(robotCount);
        var calculate  = Func.f((String target) -> {
            // Reset all pads
            doorPad.reset();
            robotPads.forEach(ArrowPad::reset);
            
            var shortest
                    = Pipeable.of(target)
                    .pipeTo(key -> doorPad.determineKeyPressed(key));
            
            for (ArrowPad robotPad : robotPads) {
                shortest = shortest.flatMap(key -> robotPad.determineKeyPressed(key));
            }
            
            var shortestPath = shortest.minBy(String::length);
            var numberPart   = Long.parseLong(target.replaceAll("[^0-9]+", ""));
            println(target + ": " + (long) shortestPath.get().length() + " -- " + numberPart);
            return numberPart * (long) shortestPath.get().length();
        });
        
        return lines.sumToLong(calculate::apply);
    }
    
    //== Test ==
    
    @Ignore("Not asked.")
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
