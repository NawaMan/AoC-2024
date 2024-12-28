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
