package day21;

import static functionalj.stream.StreamPlus.repeat;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        Position currentPosition();
        
        String   currentKey();
        void     currentKey(String key);
        
        Position positionOf(String key);
        String   keyOf(Position pos);
        
        default Movement movementTo(String key) {
            var currentPos = currentPosition();
            var targetPos  = positionOf(key);
            return walk(currentPos, targetPos);
        }
        
        boolean isValidPosition(int row, int col);
        
        default boolean validateStep(String startKey, String step) {
            var savedCurrent = currentKey();
            var currPostion  = positionOf(startKey);
            var currRow = currPostion.row;
            var currCol = currPostion.col;
            
            var isValid = true;
            for (var walk : step.split("")) {
                var direction = walk.charAt(0);
                if (direction == 'A') return true;
                
                int diffRow = 0;
                int diffCol = 0;
                if      (direction == '^') diffRow--;
                else if (direction == 'v') diffRow++;
                else if (direction == '>') diffCol++;
                else if (direction == '<') diffCol--;
                
                int newRow = currRow + diffRow;
                int newCol = currCol + diffCol;
                if (!isValidPosition(newRow, newCol)) {
                    isValid = false;
                    break;
                }   
                
                currRow = newRow;
                currCol = newCol;
            }
            
            currentKey(savedCurrent);
            return isValid;
        }
        
        String toString(int lineIndex);
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
        
        static FuncMap<Position, String> posToKey = numberPad.entries().toMap(e -> e.getValue(), e -> e.getKey());
        
        private String name;
        private String current;
        
        public NumberPad(String name, String start) {
            this.name    = name;
            this.current = start;
        }
        
        @Override public Position currentPosition()      { return numberPad.get(current); }
        @Override public String   currentKey()           { return current; }
        @Override public void     currentKey(String key) { this.current = key; }
        @Override public Position positionOf(String key) { return numberPad.get(key);     }
        
        @Override
        public String keyOf(Position pos) {
            return posToKey.get(pos);
        }
        
        @Override public boolean isValidPosition(int row, int col) {
            return !((row == 3) && (col == 0));
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
            return lines
                    .orElse(lineIndex, "             ")
                    .replaceAll("\\Q" + current + "\\E", BOLD + BLUE + current + RESET);
        }
        
        @Override
        public String toString() {
            return name;
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
        
        static FuncMap<Position, String> posToKey = arrowPad.entries().toMap(e -> e.getValue(), e -> e.getKey());
        
        private String name;
        private String current;
        
        public ArrowPad(String name, String start) {
            this.name    = name;
            this.current = start;
        }
        
        @Override
        public String keyOf(Position pos) {
            return posToKey.get(pos);
        }
        
        @Override public String   currentKey()           { return current; }
        @Override public void     currentKey(String key) { this.current = key; }
        @Override public Position currentPosition()      { return arrowPad.get(current); }
        @Override public Position positionOf(String key) { return arrowPad.get(key);     }
        
        @Override
        public boolean isValidPosition(int row, int col) {
            return !((row == 0) && (col == 0));
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
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    Object calulate(FuncList<String> lines) {
        var doorPad   = new NumberPad("    DoorPad", "A");
        var robot1Pad = new ArrowPad ("  Rbt1Pad", "A");
        var robot2Pad = new ArrowPad ("Rbt1Pad", "A");
        
        return lines
                .mapToLong(target -> {
                    doorPad.current   = "A";
                    robot1Pad.current = "A";
                    robot2Pad.current = "A";
                    var shotestLenght = shortestPathForKey(doorPad, robot1Pad, robot2Pad, target);
                    var numberPart    = Long.parseLong(target.replaceAll("[^0-9]+", ""));
                    println(target + ": " + shotestLenght + " -- " + numberPart);
                    return shotestLenght*numberPart;
                })
                .sum();
    }
    
    public static FuncList<String> generateCombinations(Movement movement) {
        if ((movement.row == 0) && (movement.col == 0))
            return FuncList.of("");
            
        var up    = max(-movement.row, 0);
        var left  = max(movement.col,  0);
        var down  = max(movement.row,  0);
        var right = max(-movement.col, 0);
        
        var steps = new ArrayList<Character>();
        for (int i = 0; i < up;    i++) steps.add('^');
        for (int i = 0; i < left;  i++) steps.add('>');
        for (int i = 0; i < down;  i++) steps.add('v');
        for (int i = 0; i < right; i++) steps.add('<');
        
        var uniqueCombinations = new HashSet<String>();
        generatePermutations(steps, 0, uniqueCombinations);
        return FuncList.from(uniqueCombinations);
    }
    
    private static void generatePermutations(List<Character> steps, int start, Set<String> result) {
        if (start == steps.size() - 1) {
            result.add(FuncList.from(steps).join());
            return;
        }
        
        for (int i = start; i < steps.size(); i++) {
            Collections.swap(steps, start, i);
            generatePermutations(steps, start + 1, result);
            Collections.swap(steps, start, i);
        }
    }
    
    public FuncList<FuncList<String>> cartesianProduct(FuncList<FuncList<String>> lists) {
        FuncList<List<String>> list = FuncList.from(lists.stream().reduce(
            List.of(List.of()),
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
                List<List<String>> combined = new ArrayList<>(list1);
                combined.addAll(list2);
                return FuncList.from(combined);
            }
        ));
        return list.map(FuncList::from);
    }
    
    //== Test ==
    
    private long shortestPathForKey(NumberPad doorPad, ArrowPad robot1Pad, ArrowPad robot2Pad, String target) {
        var shorest
                = Pipeable.of(target)
                .pipeTo (k -> determineKeyPressed(k, doorPad))
                .flatMap(k -> determineKeyPressed(k, robot1Pad))
                .flatMap(k -> determineKeyPressed(k, robot2Pad))
                .minBy(String::length)
                ;
        return (long)shorest.get().length();
    }
    
    private FuncList<String> determineKeyPressed(String target, Pad keypad) {
        return FuncList.of(target.split(""))
        .map(key -> {
            var str = keypad.currentKey();
            var mov = keypad.movementTo(key);
            var cmb = generateCombinations(mov);
            var seq = cmb
                    .map   (stp -> stp + "A")
                    .filter(stp -> keypad.validateStep(str, stp));
            keypad.currentKey(key);
            return seq;
        })
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
        assertAsString("188384", result);
    }
    
}
