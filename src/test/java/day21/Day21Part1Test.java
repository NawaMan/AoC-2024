package day21;

import static functionalj.stream.StreamPlus.repeat;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
        boolean  moveTo(char direction, Consumer<String> action);
        String   currentKey();
        void     currentKey(String key);
        Position currentPosition();
        Position positionOf(String key);
        
        default boolean walkTo(String step, Consumer<String> action) {
            return FuncList.of(step.split(""))
                    .allMatch(each -> moveTo(each.charAt(0), action));
        }
        
        String toString(int lineIndex);
        
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
                if (direction == 'A') {
                    return true;
                }
                
                int diffRow = 0;
                int diffCol = 0;
                if      (direction == '^') diffRow--;
                else if (direction == 'v') diffRow++;
                else if (direction == '>') diffCol++;
                else if (direction == '<') diffCol--;
                
                int newRow = currRow + diffRow;
                int newCol = currCol + diffCol;
                if (!isValidPosition(newRow, newCol)) {
//                    System.out.println("Not valid from curr(" + currRow + "," + currCol + ") new(" + newRow + "," + newCol + ") via direction:" + direction + " step:" + step);
                    
                    isValid = false;
                    break;
                }   
                currRow = newRow;
                currCol = newCol;
            }
            
            currentKey(savedCurrent);
            
            if (!isValid) {
//                System.out.println("Invalid step: startKey=" + startKey + ", step=" + step);
            }
            
            return isValid;
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

        @Override public void currentKey(String key) {
            this.current = key;
        }
        
        @Override public boolean isValidPosition(int row, int col) {
            var isValid = true;
            if ((row < 0) || (row >= 4))
                isValid = false;
            else if ((col < 0) || (col >= 3))
                isValid = false;
            else if ((row == 3) && (col == 0))
                isValid = false;
            else isValid = true;
            
            return isValid;
        }

//        @Override
//        public boolean moveTo(char direction, Consumer<String> action) {
//            System.out.print("direction: " + direction);
//            
//            if (direction == 'A') {
//                if (action != null)
//                    action.accept(current);
//                System.out.println();
//                return true;
//            }
//            
//            int diffRow = 0;
//            int diffCol = 0;
//            if      (direction == '^') diffRow--;
//            else if (direction == 'v') diffRow++;
//            else if (direction == '>') diffCol++;
//            else if (direction == '<') diffCol--;
//            
//            var curPos = numberPad.get(current);
//            int newRow = curPos.row + diffRow;
//            int newCol = curPos.col + diffCol;
//            if ((newRow == 3) && (newCol == 0))
//                return false;
//            
//            var newPos = new Position(newRow, newCol);
//            System.out.print(", new position: " + newPos);
//            
//            var keyPoses = numberPad.entries().filter(e -> e.getValue().equals(newPos)).map(e -> e.getKey());
//            System.out.print(", key position: " + keyPoses);
//            if (keyPoses.isEmpty())
//                println(", newPos: " + newPos);
//            
//            current = keyPoses.get(0);
//            System.out.println(", current position: " + current);
//            
//            return true;
//        }
        
        @Override
        public boolean moveTo(char direction, Consumer<String> action) {
            if (direction == 'A') {
                action.accept(current);
                return true;
            }
            
            int diffRow = 0;
            int diffCol = 0;
            if      (direction == '^') diffRow--;
            else if (direction == 'v') diffRow++;
            else if (direction == '>') diffCol++;
            else if (direction == '<') diffCol--;
            
            var curPos = numberPad.get(current);
            var newPos = new Position(curPos.row + diffRow, curPos.col + diffCol);
//            System.out.println(name + ": direction: " + direction + ", curPos: " + curPos + ", newPos: " + newPos);
            
            var filtered = numberPad.entries().filter(e -> e.getValue().equals(newPos)).map(e -> e.getKey());
//            if (filtered.isEmpty())
//                println("New position key is missing for position: " + newPos);
            
            current = filtered.get(0);
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

        private String name;
        private String current;
        
        public ArrowPad(String name, String start) {
            this.name    = name;
            this.current = start;
        }
        
        @Override public String   currentKey()           { return current; }
        @Override public Position currentPosition()      { return arrowPad.get(current); }
        @Override public Position positionOf(String key) { return arrowPad.get(key);     }

        @Override public void currentKey(String key) {
            this.current = key;
        }
        
        @Override public boolean isValidPosition(int row, int col) {
            var isValid = true;
            if ((row < 0) || (row >= 2))
                isValid = false;
            else if ((col < 0) || (col >= 3))
                isValid = false;
            else if ((row == 0) && (col == 0))
                isValid = false;
            else isValid = true;
            
            return isValid;
        }
        
//        @Override
//        public boolean moveTo(char direction, Consumer<String> action) {
//            if (direction == 'A') {
//                action.accept(current);
//                return true;
//            }
//            
//            int diffRow = 0;
//            int diffCol = 0;
//            if      (direction == '^') diffRow--;
//            else if (direction == 'v') diffRow++;
//            else if (direction == '>') diffCol++;
//            else if (direction == '<') diffCol--;
//            
//            var curPos = arrowPad.get(current);
//            int newRow = curPos.row + diffRow;
//            int newCol = curPos.col + diffCol;
//            if ((newRow == 0) && (newCol == 0))
//                return false;
//            
//            var newPos = new Position(newRow, newCol);
//            current = arrowPad.entries().filter(e -> e.getValue().equals(newPos)).map(e -> e.getKey()).get(0);
//            
//            return true;
//        }
        @Override
        public boolean moveTo(char direction, Consumer<String> action) {
            if (direction == 'A') {
                action.accept(current);
                return true;
            }
            
            int diffRow = 0;
            int diffCol = 0;
            if      (direction == '^') diffRow--;
            else if (direction == 'v') diffRow++;
            else if (direction == '>') diffCol++;
            else if (direction == '<') diffCol--;
            
            var curPos = arrowPad.get(current);
            var newPos = new Position(curPos.row + diffRow, curPos.col + diffCol);
//            System.out.println(name + ": direction: " + direction + ", curPos: " + curPos + ", newPos: " + newPos);
            var filtered = arrowPad.entries().filter(e -> e.getValue().equals(newPos)).map(e -> e.getKey());
//            if (filtered.isEmpty())
//                println("New position key is missing for position: " + newPos);
            
            current = filtered.get(0);
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
                .pipe   (p -> {
                    if (p.isEmpty())
                        println("No path at the door for : " + target);
                    return p;
                })
                .flatMap(k -> determineKeyPressed(k, robot1Pad))
                .pipe   (p -> {
                    if (p.isEmpty())
                        println("No path at robot1 for : " + target);
                    return p;
                })
                .flatMap(k -> determineKeyPressed(k, robot2Pad))
                .pipe   (p -> {
                    if (p.isEmpty())
                        println("No path at robot2 for : " + target);
                    return p;
                })
                .minBy(String::length)
                ;
        if (shorest.isEmpty()) {
            println("No path for : " + target);
        }
        
        var minLength = shorest.get().length();
        println(shorest + ": " + minLength);
        return (long)minLength;
    }

    private FuncList<String> determineKeyPressed(String target, Pad keypad) {
        return FuncList.of(target.split(""))
        .map(key -> {
            var str = keypad.currentKey();
            var mov = keypad.movementTo(key);
            var cmb = generateCombinations(mov);
//            println("keypad: " + keypad + ", key: " + key + ", cmb" + cmb);
            var seq = cmb
                    .map   (stp -> stp + "A")
                    .filter(stp -> {
                        var valid = keypad.validateStep(str, stp);
                        return valid;
                    });
            keypad.currentKey(key);
            return seq;
        })
        .pipe(this::cartesianProduct)
        .map (FuncList::join)
        .cache();
    }
    
    @Test
    public void testExample() {
//        Optional[<v<AA>A^>AvAA<^A>A<v<A>^>AvA^Av<A^>A<v<A^>A>AAvA^Av<A<A>^>AAA<A>vA^A]: 68
//        029A: 68 -- 29
//        Optional[<v<A>^>AAAvA^A<v<AA>A^>AvAA<^A>Av<A<A>^>AAA<A>vA^Av<A^>A<A>A]: 60
//        980A: 60 -- 980
//        Optional[<v<AA>A^>AA<A>vA^AvA^A<v<A>^>AAvA^Av<A^>AA<A>Av<A<A>^>AAA<A>vA^A]: 64
//        179A: 64 -- 179
//        Optional[<v<AA>A^>AA<A>vA^AAvA^Av<A^>A<A>Av<A^>A<A>Av<A<A>^>AA<A>vA^A]: 60
//        456A: 60 -- 456
//        Optional[<v<A>^>AvA^A<v<AA>A^>AA<A>vA^AAvA^Av<A^>AA<A>Av<A<A>^>AAA<A>vA^A]: 64
//        379A: 64 -- 379

//      Optional[<v<AA>A^>AvAA<^A>A<v<A>^>AvA^Av<A^>A<v<A^>A>AAvA^Av<A<A>^>AAA<A>vA^A]: 68
//      029A: 68 -- 29
//      Optional[<v<A>^>AAAvA^A<v<AA>A^>AvAA<^A>Av<A<A>^>AAA<A>vA^Av<A^>A<A>A]: 60
//      980A: 60 -- 980
//      Optional[<v<AA>A^>AA<A>vA^AvA^A<v<A>^>AAvA^Av<A^>AA<A>Av<A<A>^>AAA<A>vA^A]: 64
//      179A: 64 -- 179
//      Optional[<v<AA>A^>AA<A>vA^AAvA^Av<A^>A<A>Av<A^>A<A>Av<A<A>^>AA<A>vA^A]: 60
//      456A: 60 -- 456
//      Optional[<v<A>^>AvA^A<v<AA>A^>AA<A>vA^AAvA^Av<A^>AA<A>Av<A<A>^>AAA<A>vA^A]: 64
//      379A: 64 -- 379
        
//      var doorPad   = new NumberPad(   "    DoorPad", "A");
//      var robot1Pad = new ArrowPad ("  Rbt1Pad", "A");
//      var robot2Pad = new ArrowPad ("Rbt2Pad", "A");
      
//      var humanPress = "<v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A";
//      var humanPress = "v<<A>>^AvA^Av<<A>>^AAv<A<A>>^AAvAA^<A>Av<A>^AA<A>Av<A<A>>^AAAvA^<A>A";
      
//      var humanPress = "<v<AA>A^>AA<A>vA^AvA^A<v<A>^>AAvA^Av<A^>AA<A>Av<A<A>^>AAA<A>vA^A";
//      var step = new AtomicInteger();
//      for (var humanKey : humanPress.split("")) {
//          println("move: " + humanKey);
//          robot2Pad.moveTo(humanKey.charAt(0), key1 -> 
//              robot1Pad.moveTo(key1.charAt(0), keyDoor -> 
//                  doorPad.moveTo(keyDoor.charAt(0), num -> {
//                      println();
//                      println("Step: " + step);
//                      println("Door pressed: " + num);
//                      println();
//                  })));
//          
//          for (int i = 0; i < 10; i++) {
//              System.out.println(robot2Pad.toString(i) + "    " + robot1Pad.toString(i) + "    " + doorPad.toString(i));
//          }
//          step.incrementAndGet();
//      }
        
//        ==| testExample |==
//        Optional[<A^A>^^AvvvA]: 12
//        029A: 12 -- 29
//        Optional[^^^A<AvvvA>A]: 12
//        980A: 12 -- 980
//        Optional[<^<A^^A>>AvvvA]: 14
//        179A: 14 -- 179
//        Optional[^<^<A>A>AvvA]: 12
//        456A: 12 -- 456
//        Optional[^A^<^<A>>AvvvA]: 14
//        379A: 14 -- 379
//        result: 25392
//        --| testExample |--
      
//          ==| testExample |==
//          Optional[<v<A>^>A<A>AvA<^AA>Av<AAA^>A]: 28
//          029A: 28 -- 29
//          Optional[<AAA>A<v<A>^>Av<AAA^>AvA^A]: 26
//          980A: 26 -- 980
//          Optional[<v<AA^>A>A<AA>AvAA^Av<AAA^>A]: 28
//          179A: 28 -- 179
//          Optional[<Av<AA^>A>AvA^AvA^Av<AA^>A]: 26
//          456A: 26 -- 456
//          Optional[<A>A<Av<AA^>A>AvAA^Av<AAA^>A]: 28
//          379A: 28 -- 379
//          result: 53772
//          --| testExample |--
        
        
        
//        var robot1Press = "<v<AA^>A>A<AA>AvAA^Av<AAA^>A]";
//        var step = new AtomicInteger();
//        var logs = new ArrayList<String>();
//        for (var robot1Key : robot1Press.split("")) {
//            robot1Pad.moveTo(robot1Key.charAt(0), doorKey -> 
//                doorPad.moveTo(doorKey.charAt(0), num -> {
//                    println();
//                    println("Step: " + step);
//                    println("Door pressed: " + num);
//                    println();
//                    logs.add("#" + step + ": " + num);
//                }));
//            
//            for (int i = 0; i < 10; i++) {
//                System.out.println(doorPad.toString(i));
//            }
//            step.incrementAndGet();
//        }
//        
//        for (var log : logs) {
//            println(log);
//        }
        
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("126384", result);
        
//        var doorPad   = new NumberPad("    DoorPad", "A");
////        var robot1Pad = new ArrowPad ("  Rbt1Pad", "A");
////        var robot2Pad = new ArrowPad ("Rbt1Pad", "A");
//        
//        var target = "379A";
//        var keypad = doorPad;
//        
//        var options
//            = Pipeable.of(target)
//            .pipeTo (k -> determineKeyPressed(k, doorPad))
//            .cache();
//        
//        options
//            .forEach(println);
//        
////        println(doorPad.currentKey());
////        
////        println(doorPad.walkTo(options.get(0), null));
////        
////        FuncList.of(target.split(""))
////        .map(key -> {
////            var mov = keypad.movementTo(key);
////            var cmb = generateCombinations(mov);
////            var seq = cmb.map(p -> p + "A");
////            keypad.current = key;
////            return seq;
////        })
////        .pipe(this::cartesianProduct)
////        .map (FuncList::join)
////        .cache()
////        .forEach(println);
        
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("188384", result);
    }
    
}
