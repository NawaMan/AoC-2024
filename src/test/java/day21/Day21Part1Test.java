package day21;

import static functionalj.function.Func.f;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import common.BaseTest;
import functionalj.function.Func1;
import functionalj.list.FuncList;
import functionalj.map.FuncMap;
import functionalj.map.FuncMapBuilder;
import functionalj.pipeable.Pipeable;

/**
 * --- Day 21: Keypad Conundrum ---
 * 
 * As you teleport onto Santa's Reindeer-class starship, The Historians begin to panic: someone from their search party 
 *   is missing. A quick life-form scan by the ship's computer reveals that when the missing Historian teleported, he 
 *   arrived in another part of the ship.
 * 
 * The door to that area is locked, but the computer can't open it; it can only be opened by physically typing the door 
 *   codes (your puzzle input) on the numeric keypad on the door.
 * 
 * The numeric keypad has four rows of buttons: 789, 456, 123, and finally an empty gap followed by 0A. Visually, they 
 *   are arranged like this:
 * 
 * +---+---+---+
 * | 7 | 8 | 9 |
 * +---+---+---+
 * | 4 | 5 | 6 |
 * +---+---+---+
 * | 1 | 2 | 3 |
 * +---+---+---+
 *     | 0 | A |
 *     +---+---+
 * 
 * Unfortunately, the area outside the door is currently depressurized and nobody can go near the door. A robot needs to 
 *   be sent instead.
 * 
 * The robot has no problem navigating the ship and finding the numeric keypad, but it's not designed for button pushing:
 *   it can't be told to push a specific button directly. Instead, it has a robotic arm that can be controlled remotely 
 *   via a directional keypad.
 * 
 * The directional keypad has two rows of buttons: a gap / ^ (up) / A (activate) on the first row and < (left) / v (down) 
 *   / > (right) on the second row. Visually, they are arranged like this:
 * 
 *     +---+---+
 *     | ^ | A |
 * +---+---+---+
 * | < | v | > |
 * +---+---+---+
 * 
 * When the robot arrives at the numeric keypad, its robotic arm is pointed at the A button in the bottom right corner. 
 *   After that, this directional keypad remote control must be used to maneuver the robotic arm: the up / down / left 
 *   / right buttons cause it to move its arm one button in that direction, and the A button causes the robot to briefly
 *   move forward, pressing the button being aimed at by the robotic arm.
 * 
 * For example, to make the robot type 029A on the numeric keypad, one sequence of inputs on the directional keypad you 
 *   could use is:
 * 
 *     < to move the arm from A (its initial position) to 0.
 *     A to push the 0 button.
 *     ^A to move the arm to the 2 button and push it.
 *     >^^A to move the arm to the 9 button and push it.
 *     vvvA to move the arm to the A button and push it.
 * 
 * In total, there are three shortest possible sequences of button presses on this directional keypad that would cause 
 *   the robot to type 029A: <A^A>^^AvvvA, <A^A^>^AvvvA, and <A^A^^>AvvvA.
 * 
 * Unfortunately, the area containing this directional keypad remote control is currently experiencing high levels of 
 *   radiation and nobody can go near it. A robot needs to be sent instead.
 * 
 * When the robot arrives at the directional keypad, its robot arm is pointed at the A button in the upper right corner.
 *   After that, a second, different directional keypad remote control is used to control this robot (in the same way as
 *   the first robot, except that this one is typing on a directional keypad instead of a numeric keypad).
 * 
 * There are multiple shortest possible sequences of directional keypad button presses that would cause this robot to 
 *   tell the first robot to type 029A on the door. One such sequence is v<<A>>^A<A>AvA<^AA>A<vAAA>^A.
 * 
 * Unfortunately, the area containing this second directional keypad remote control is currently -40 degrees! Another 
 *   robot will need to be sent to type on that directional keypad, too.
 * 
 * There are many shortest possible sequences of directional keypad button presses that would cause this robot to tell 
 *   the second robot to tell the first robot to eventually type 029A on the door. One such sequence is 
 *   <vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A.
 * 
 * Unfortunately, the area containing this third directional keypad remote control is currently full of Historians, 
 *   so no robots can find a clear path there. Instead, you will have to type this sequence yourself.
 * 
 * Were you to choose this sequence of button presses, here are all of the buttons that would be pressed on your 
 *   directional keypad, the two robots' directional keypads, and the numeric keypad:
 * 
 * <vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A
 * v<<A>>^A<A>AvA<^AA>A<vAAA>^A
 * <A^A>^^AvvvA
 * 029A
 * 
 * In summary, there are the following keypads:
 * 
 *     One directional keypad that you are using.
 *     Two directional keypads that robots are using.
 *     One numeric keypad (on a door) that a robot is using.
 * 
 * It is important to remember that these robots are not designed for button pushing. In particular, if a robot arm is 
 *   ever aimed at a gap where no button is present on the keypad, even for an instant, the robot will panic 
 *   unrecoverably. So, don't do that. All robots will initially aim at the keypad's A key, wherever it is.
 * 
 * To unlock the door, five codes will need to be typed on its numeric keypad. For example:
 * 
 * 029A
 * 980A
 * 179A
 * 456A
 * 379A
 * 
 * For each of these, here is a shortest sequence of button presses you could type to cause the desired code to be typed
 *   on the numeric keypad:
 * 
 * 029A: <vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A
 * 980A: <v<A>>^AAAvA^A<vA<AA>>^AvAA<^A>A<v<A>A>^AAAvA<^A>A<vA>^A<A>A
 * 179A: <v<A>>^A<vA<A>>^AAvAA<^A>A<v<A>>^AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
 * 456A: <v<A>>^AA<vA<A>>^AAvAA<^A>A<vA>^A<A>A<vA>^A<A>A<v<A>A>^AAvA<^A>A
 * 379A: <v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
 * 
 * The Historians are getting nervous; the ship computer doesn't remember whether the missing Historian is trapped in 
 *   the area containing a giant electromagnet or molten lava. You'll need to make sure that for each of the five codes,
 *   you find the shortest sequence of button presses necessary.
 * 
 * The complexity of a single code (like 029A) is equal to the result of multiplying these two values:
 * 
 *     The length of the shortest sequence of button presses you need to type on your directional keypad in order 
 *       to cause the code to be typed on the numeric keypad; for 029A, this would be 68.
 *     The numeric part of the code (ignoring leading zeroes); for 029A, this would be 29.
 * 
 * In the above example, complexity of the five codes can be found by calculating 68 * 29, 60 * 980, 68 * 179, 64 * 456,
 *   and 64 * 379. Adding these together produces 126384.
 * 
 * Find the fewest number of button presses you'll need to perform in order to cause the robot in front of the door 
 *   to type each code. What is the sum of the complexities of the five codes on your list?
 * 
 * Your puzzle answer was 188384.
 */
public class Day21Part1Test extends BaseTest {
    
    static Func1<FuncList<FuncList<String>>, FuncList<FuncList<String>>> 
            cartesianProduct = f(Day21Part1Test::cartesianProduct);
    
    static record Position(int row, int col) {}
    static record Movement(int row, int col) {}
    
    static abstract class KeyPad {
        
        private final FuncMap<String, Position> keyToPos;
         
        private String current = "A";
        
        KeyPad(FuncMap<String, Position> keyToPos) {
            this.keyToPos = keyToPos;
        }
        
        void reset() { current = "A"; }
        
        abstract boolean isValidPosition(int row, int col);
        
        Movement movementTo(String key) {
            var currentPos = keyToPos.get(current);
            var targetPos  = keyToPos.get(key);
            return new Movement(targetPos.row - currentPos.row, targetPos.col - currentPos.col);
        }
        
        boolean validateStep(String startKey, String step) {
            var currPostion  = keyToPos.get(startKey);
            var currRow = currPostion.row;
            var currCol = currPostion.col;
            for (var walk : step.split("")) {
                switch (walk.charAt(0)) {
                    case '^': currRow -= 1; break;
                    case 'v': currRow += 1; break;
                    case '>': currCol += 1; break;
                    case '<': currCol -= 1; break;
                    case 'A': return true;
                    default: break;
                }
                if (!isValidPosition(currRow, currCol))
                    return false;
            }
            return true;
        }
        
        FuncList<String> determineKeyPressed(String target) {
            return FuncList.of(target.split(""))
            .map(key -> {
                var str = current;
                var mov = movementTo(key);
                var cmb = generateCombinations(mov);
                var seq = cmb
                        .map   (stp -> stp + "A")
                        .filter(stp -> validateStep(str, stp));
                current = key;
                return seq;
            })
            .pipe(cartesianProduct)
            .map (FuncList::join)
            .cache();
        }
    }
    
    static class NumberPad extends KeyPad {
        
        static FuncMap<String, Position> keyToPos
                = new FuncMapBuilder<String, Position>()
                .with("7", new Position(0, 0)).with("8", new Position(0, 1)).with("9", new Position(0, 2))
                .with("4", new Position(1, 0)).with("5", new Position(1, 1)).with("6", new Position(1, 2))
                .with("1", new Position(2, 0)).with("2", new Position(2, 1)).with("3", new Position(2, 2))
                                              .with("0", new Position(3, 1)).with("A", new Position(3, 2))
                .build();
                
        NumberPad() { super(keyToPos); }
        
        @Override public boolean isValidPosition(int row, int col) {
            return !((row == 3) && (col == 0));
        }
    }
    
    static class ArrowPad extends KeyPad {
        
        static FuncMap<String, Position> keyToPos
                = new FuncMapBuilder<String, Position>()
                                              .with("^", new Position(0, 1)).with("A", new Position(0, 2))
                .with("<", new Position(1, 0)).with("v", new Position(1, 1)).with(">", new Position(1, 2))
                .build();
                
        ArrowPad() { super(keyToPos); }
        
        @Override
        public boolean isValidPosition(int row, int col) {
            return !((row == 0) && (col == 0));
        }
    }
    
    static FuncList<String> generateCombinations(Movement movement) {
        if ((movement.row == 0) && (movement.col == 0))
            return FuncList.of("");
            
        var up    = max(-movement.row, 0);
        var left  = max(movement.col,  0);
        var down  = max(movement.row,  0);
        var right = max(-movement.col, 0);
        
        var length = 0;
        var steps  = new char[abs(movement.row) + abs(movement.col)];
        for (int i = 0; i < up;    i++) steps[length++] = '^';
        for (int i = 0; i < left;  i++) steps[length++] = '>';
        for (int i = 0; i < down;  i++) steps[length++] = 'v';
        for (int i = 0; i < right; i++) steps[length++] = '<';
        
        var uniqueCombinations = new HashSet<String>();
        generatePermutations(steps, 0, uniqueCombinations);
        return FuncList.from(uniqueCombinations);
    }
    
    static void generatePermutations(char[] steps, int start, Set<String> result) {
        if (start == steps.length - 1) {
            result.add(new String(steps));
            return;
        }
        
        for (int i = start; i < steps.length; i++) {
            swap(steps, start, i);
            generatePermutations(steps, start + 1, result);
            swap(steps, start, i);
        }
    }
    
    static void swap(char[] array, int i, int j) {
        var temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
    
    static FuncList<FuncList<String>> cartesianProduct(FuncList<FuncList<String>> lists) {
        var list = FuncList.from(lists.stream().reduce(
            List.of(List.of()),
            (acc, nextList) -> acc.stream()
                .flatMap(existing -> 
                    nextList
                    .stream()
                    .map(item -> {
                        List<String> newCombination = new ArrayList<>(existing);
                        newCombination.add(item);
                        return newCombination;
                    })
                )
                .collect(toList()),
            (list1, list2) -> {
                var combined = new ArrayList<List<String>>(list1);
                combined.addAll(list2);
                return FuncList.from(combined);
            }
        ));
        return list.map(FuncList::from);
    }
    
    Object calculate(FuncList<String> lines) {
        var doorPad   = new NumberPad();
        var robot1Pad = new ArrowPad ();
        var robot2Pad = new ArrowPad ();
        var calculate  = f((String target) -> {
            doorPad.reset();
            robot1Pad.reset();
            robot2Pad.reset();
            var shorest
                    = Pipeable.of(target)
                    .pipeTo (k -> doorPad.determineKeyPressed(k))
                    .flatMap(k -> robot1Pad.determineKeyPressed(k))
                    .flatMap(k -> robot2Pad.determineKeyPressed(k))
                    .minBy(String::length)
                    ;
            var numberPart = Long.parseLong(target.replaceAll("[^0-9]+", ""));
            println(target + ": " + (long)shorest.get().length() + " -- " + numberPart);
            return numberPart * (long)shorest.get().length();
        });
        return lines.sumToLong(calculate::apply);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("126384", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("188384", result);
    }
    
}
