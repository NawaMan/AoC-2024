package day21;

import static functionalj.stream.StreamPlus.repeat;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.map.FuncMap;
import functionalj.map.FuncMapBuilder;
import functionalj.pipeable.Pipeable;

public class Day21Part1Test extends BaseTest {

    public static final String BOLD = "\033[1m";
    public static final String BLUE  = "\033[34m";
    public static final String RESET = "\033[0m"; // Reset to default color
    
    record Position(int row, int col) {}
    record Movement(int row, int col) {}
    
    static Movement walk(Position from, Position to) {
        return new Movement(to.row - from.row, to.col - from.col);
    }
    
    interface Pad {
        boolean  moveTo(String direction, Consumer<String> action);
        String   currentKey();
        Position currentPosition();
        Position positionOf(String key);
        
        default boolean walkTo(String step, Consumer<String> action) {
            return FuncList.of(step.split(""))
                    .allMatch(each -> moveTo(each, action));
        }
        
        String toString(int lineIndex);
        
        default Movement movementTo(String key) {
            return walk(currentPosition(), positionOf(key));
        }
    }
    
    class NumberPad implements Pad {
    
        static FuncMap<String, Position> numberPad
                = new FuncMapBuilder<String, Position>()
                .with("7", new Position(0, 0))
                .with("8", new Position(0, 1))
                .with("9", new Position(0, 2))
                .with("4", new Position(1, 0))
                .with("5", new Position(1, 1))
                .with("6", new Position(1, 2))
                .with("1", new Position(2, 0))
                .with("2", new Position(2, 1))
                .with("3", new Position(2, 2))
                .with("0", new Position(3, 1))
                .with("A", new Position(3, 2))
                .build();
        
        private String name;
        private String current;
        
        public NumberPad(String name, String start) {
            this.name    = name;
            this.current = start;
        }
        
        @Override public String   currentKey()           { return current; }
        @Override public Position currentPosition()      { return numberPad.get(current); }
        @Override public Position positionOf(String key) { return numberPad.get(key);     }

        @Override
        public boolean moveTo(String direction, Consumer<String> action) {
            if (direction.equals("A")) {
                if (action != null)
                    action.accept(current);
                return true;
            }
            
            int diffRow = 0;
            int diffCol = 0;
            if      (direction.equals("^")) diffRow--;
            else if (direction.equals("v")) diffRow++;
            else if (direction.equals(">")) diffCol++;
            else if (direction.equals("<")) diffCol--;
            
            var curPos = numberPad.get(current);
            int newRow = curPos.row + diffRow;
            int newCol = curPos.col + diffCol;
            if ((newRow == 3) && (newCol == 0))
                return false;
            
            var newPos = new Position(newRow, newCol);
            var keyPoses = numberPad.entries().filter(e -> e.getValue().equals(newPos)).map(e -> e.getKey());
            if (keyPoses.isEmpty())
                println("newPos: " + newPos);
            
            current = keyPoses.get(0);
            
            return true;
        }

        @Override
        public String toString(int lineIndex) {
            var text = """
                    +---+---+---+
                    | 7 | 8 | 9 |
                    +---+---+---+
                    | 4 | 5 | 6 |
                    +---+---+---+
                    | 1 | 2 | 3 |
                    +---+---+---+
                        | 0 | A |
                        +---+---+
                    """;
            var lines = FuncList.of(text.split("\n"));
            return lines.orElse(lineIndex, "             ").replaceAll("\\Q" + current + "\\E", BOLD + BLUE + current + RESET);
        }
    }

    class ArrowPad implements Pad {
    
        static FuncMap<String, Position> arrowPad
                = new FuncMapBuilder<String, Position>()
                .with("^", new Position(0, 1))
                .with("A", new Position(0, 2))
                .with("<", new Position(1, 0))
                .with("v", new Position(1, 1))
                .with(">", new Position(1, 2))
                .build();

        private String name;
        private String current;
        
        public ArrowPad(String name, String start) {
            this.name    = name;
            this.current = start;
        }
        
        @Override public String   currentKey()           { return current; }
        @Override public Position currentPosition()      { return arrowPad.get(current); }
        @Override public Position positionOf(String key) { return arrowPad.get(key);     }
        
        @Override
        public boolean moveTo(String direction, Consumer<String> action) {
            if (direction.equals("A")) {
                action.accept(current);
                return true;
            }
            
            int diffRow = 0;
            int diffCol = 0;
            if      (direction.equals("^")) diffRow--;
            else if (direction.equals("v")) diffRow++;
            else if (direction.equals(">")) diffCol++;
            else if (direction.equals("<")) diffCol--;
            
            var curPos = arrowPad.get(current);
            int newRow = curPos.row + diffRow;
            int newCol = curPos.col + diffCol;
            if ((newRow == 0) && (newCol == 0))
                return false;
            
            var newPos = new Position(newRow, newCol);
            current = arrowPad.entries().filter(e -> e.getValue().equals(newPos)).map(e -> e.getKey()).get(0);
            
            return true;
        }

        @Override
        public String toString(int lineIndex) {
            var text = """
                        +---+---+
                        | ^ | A |
                    +---+---+---+
                    | < | v | > |
                    +---+---+---+
                    """;
            var lines = FuncList.of(text.split("\n"));
            return lines.orElse(lineIndex, "             ").replaceAll("\\Q" + current + "\\E", BOLD + BLUE + current + RESET);
        }
    }
    
    Object calulate(FuncList<String> lines) {
        var doorPad   = new NumberPad("    DoorPad", "A");
        var robot1Pad = new ArrowPad ("  Rbt1Pad", "A");
        var robot2Pad = new ArrowPad ("Rbt1Pad", "A");
        
        return lines
                .mapToLong(target -> {
                    var shotestLenght = shortestPathForKey(doorPad, robot1Pad, robot2Pad, target);
                    var numberPart    = Long.parseLong(target.replaceAll("[^0-9]+", ""));
                    println(target + ": " + shotestLenght + " -- " + numberPart);
                    return shotestLenght*numberPart;
                })
                .sum();
    }
    
    static String shortestWalk(int diffRow, int diffCol) {
        var upOrDown    = (diffRow < 0) ? "^" : "v";
        var leftOrRight = (diffCol < 0) ? "<" : ">";
        
        
        var goUpDown    = repeat(upOrDown).limit(abs(diffRow)).join();
        var goLeftRight = repeat(leftOrRight).limit(abs(diffCol)).join();
        return goUpDown + goLeftRight;
    }
    
    // Method to generate all combinations
    public static FuncList<String> generateCombinations(Movement movement) {
        return generateCombinations(movement.row, movement.col);
    }
    
    // Method to generate all combinations
    public static FuncList<String> generateCombinations(int up, int left) {
        return generateCombinations(max(-up, 0), max(left, 0), max(up, 0), max(-left, 0));
    }
    // Method to generate all combinations
    public static FuncList<String> generateCombinations(int up, int left, int down, int right) {
        if ((up == 0) && (left == 0) && (down == 0) && (right == 0))
            return FuncList.of("");
        
        // Create a list with the specified number of steps for each direction
        List<Character> steps = new ArrayList<>();
        for (int i = 0; i < up; i++) {
            steps.add('^'); // Up
        }
        for (int i = 0; i < left; i++) {
            steps.add('>'); // Left
        }
        for (int i = 0; i < down; i++) {
            steps.add('v'); // Down
        }
        for (int i = 0; i < right; i++) {
            steps.add('<'); // Right
        }

        // Use a set to store unique combinations
        Set<String> uniqueCombinations = new HashSet<>();
        generatePermutations(steps, 0, uniqueCombinations);

        // Convert the set to a list and return
        return FuncList.from(uniqueCombinations);
    }

    // Helper method to generate permutations
    private static void generatePermutations(List<Character> steps, int start, Set<String> result) {
        if (start == steps.size() - 1) {
            // Add the current permutation as a string to the set
            result.add(listToString(steps));
            return;
        }

        for (int i = start; i < steps.size(); i++) {
            // Swap characters at `start` and `i`
            Collections.swap(steps, start, i);
            // Recurse for the next position
            generatePermutations(steps, start + 1, result);
            // Swap back to restore the original list
            Collections.swap(steps, start, i);
        }
    }

    // Helper method to convert a list of characters to a string
    private static String listToString(List<Character> list) {
        StringBuilder sb = new StringBuilder();
        for (char c : list) {
            sb.append(c);
        }
        return sb.toString();
    }

    public FuncList<FuncList<String>> cartesianProduct(FuncList<FuncList<String>> lists) {
        FuncList<List<String>> list = FuncList.from(lists.stream().reduce(
            List.of(List.of()), // Start with a list containing an empty list
            (acc, nextList) -> acc.stream()
                .flatMap(existing -> nextList.stream()
                    .map(item -> {
                        List<String> newCombination = new ArrayList<>(existing);
                        newCombination.add(item);
                        return newCombination;
                    })
                )
                .collect(Collectors.toList()),
            (list1, list2) -> {
                // Combine results in case of parallel streams (not used here but needed for reduce)
                List<List<String>> combined = new ArrayList<>(list1);
                combined.addAll(list2);
                return FuncList.from(combined);
            }
        ));
        return list.map(FuncList::from);
    }
    
    //== Test ==
//    
//    @Test
//    public void testManual() {
//        var doorPad   = new NumberPad("    DoorPad", "A");
//        var robot1Pad = new ArrowPad ("  Rbt1Pad", "A");
//        var robot2Pad = new ArrowPad ("Rbt1Pad", "A");
////        
////        for (int i = 0; i < 10; i++) {
////            System.out.println(robot1Pad.toString(i) + "    " + doorPad.toString(i));
////        }
//        var target = "379A";
//
//        println("Presses: ");
//        var minLength = shortestPathForKey(doorPad, robot1Pad, robot2Pad, target);
//        
//        println(minLength);
//    }

    private long shortestPathForKey(NumberPad doorPad, ArrowPad robot1Pad, ArrowPad robot2Pad, String target) {
        var shorest
                = Pipeable.of(target)
                .pipeTo (k -> determineKeyPressed(k, doorPad))
                .flatMap(k -> determineKeyPressed(k, robot1Pad))
                .flatMap(k -> determineKeyPressed(k, robot2Pad))
                .minBy(String::length)
                ;
        var minLength = shorest.get().length();
        println(shorest + ": " + minLength);
        return (long)minLength;
    }

    private FuncList<String> determineKeyPressed(String target, Pad keypad) {
        return FuncList.of(target.split(""))
        .map (key -> keypad.movementTo(key))
        .map (mov -> generateCombinations(mov))
        .map (cmb -> cmb.map(p -> p + "A"))
        .map (cmd -> cmd.filter(step -> keypad.walkTo(step, null)).cache())
        .pipe(this::cartesianProduct)
        .map (FuncList::join)
        .cache();
    }
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("126384", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("187808", result);
    }
    
}
