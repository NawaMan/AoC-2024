package day21;

import static functionalj.stream.StreamPlus.repeat;
import static java.lang.Math.abs;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.map.FuncMap;
import functionalj.map.FuncMapBuilder;
import functionalj.stream.StreamPlus;

public class Day21Part1Test extends BaseTest {

    public static final String BOLD = "\033[1m";
    public static final String BLUE  = "\033[34m";
    public static final String RESET = "\033[0m"; // Reset to default color
    
    record Position(int row, int col) {}
    
    static int walk(Position from, Position to) {
        return abs(to.row  - from.row) + abs(to.col  - from.col);
    }
    static int walk(Position from, Position mid, Position to) {
        return walk(from, mid) + walk(mid, to);
    }
    static String shortestWalk(int diffRow, int diffCol) {
        var upOrDown    = (diffRow < 0) ? "^" : "v";
        var leftOrRight = (diffCol < 0) ? "<" : ">";
        var goUpDown    = repeat(upOrDown).limit(abs(diffRow)).join();
        var goLeftRight = repeat(leftOrRight).limit(abs(diffCol)).join();
        return goUpDown + goLeftRight;
    }
    
    interface Pad {
        String   walkTo(String next);
        void     moveTo(String direction, Consumer<String> action);
        String   currentKey();
        Position currentPosition();
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
        
        @Override public String   currentKey()      { return current; }
        @Override public Position currentPosition() { return numberPad.get(current); }
        
        @Override
        public String walkTo(String next) {
            var curPos = currentPosition();
            var nxtPos = numberPad.get(next);
            var diffRow = nxtPos.row - curPos.row;
            var diffCol = nxtPos.col - curPos.col;
            
            current = next;

            // To avoid walking into the empty space.
            // Go up+right or down+left does not matter
            // Go up+left,    should go up first
            // Go down+right, should go right right
//            var isUpLeft = (diffRow < 0) && (diffCol < 0);
//            if (isUpLeft) {
//                return repeat("^").limit(abs(diffRow)).join() + repeat("<").limit(abs(diffCol)).join() + "A";
//            }
//            var isDownRight = (diffRow > 0) && (diffCol > 0);
//            if (isDownRight) {
//                return repeat(">").limit(abs(diffCol)).join() + repeat("v").limit(abs(diffRow)).join() + "A";
//            }
            
            return shortestWalk(diffRow, diffCol) + "A";
        }
        
        public void moveTo(String direction, Consumer<String> action) {
            if (direction.equals("A")) {
                action.accept(current);
                return;
            }
            
            int diffRow = 0;
            int diffCol = 0;
            if      (direction.equals("^")) diffRow--;
            else if (direction.equals("v")) diffRow++;
            else if (direction.equals(">")) diffCol++;
            else if (direction.equals("<")) diffCol--;
            
            var curPos = numberPad.get(current);
            var newPos = new Position(curPos.row + diffRow, curPos.col + diffCol);
            System.out.println(name + ": direction: " + direction + ", curPos: " + curPos + ", newPos: " + newPos);
            current = numberPad.entries().filter(e -> e.getValue().equals(newPos)).map(e -> e.getKey()).get(0);
        }
        
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

    class DirectionPad implements Pad {
    
        static FuncMap<String, Position> directionalPad
                = new FuncMapBuilder<String, Position>()
                .with("^", new Position(0, 1))
                .with("A", new Position(0, 2))
                .with("<", new Position(1, 0))
                .with("v", new Position(1, 1))
                .with(">", new Position(1, 2))
                .build();

        private String name;
        private String current;
        
        public DirectionPad(String name, String start) {
            this.name    = name;
            this.current = start;
        }
        
        @Override public String   currentKey()      { return current; }
        @Override public Position currentPosition() { return directionalPad.get(current); }
        
        @Override
        public String walkTo(String next) {
            var curPos = currentPosition();
            var nxtPos = directionalPad.get(next);
            var diffRow = nxtPos.row - curPos.row;
            var diffCol = nxtPos.col - curPos.col;
            
            current = next;
            
            // To avoid walking into the empty space.
            // Go down+left, should go down first
            // Go up+right, should go right right
//            var isDownLeft = (diffRow > 0) && (diffCol < 0);
//            if (isDownLeft) {
//                return repeat("v").limit(abs(diffRow)).join() + repeat("<").limit(abs(diffCol)).join() + "A";
//            }
//            var isUpRight = (diffRow < 0) && (diffCol > 0);
//            if (isUpRight) {
//                return repeat(">").limit(abs(diffCol)).join() + repeat("^").limit(abs(diffRow)).join() + "A";
//            }
            
            return shortestWalk(diffRow, diffCol) + "A";
        }
        
        public void moveTo(String direction, Consumer<String> action) {
            if (direction.equals("A")) {
                action.accept(current);
                return;
            }
            
            int diffRow = 0;
            int diffCol = 0;
            if      (direction.equals("^")) diffRow--;
            else if (direction.equals("v")) diffRow++;
            else if (direction.equals(">")) diffCol++;
            else if (direction.equals("<")) diffCol--;
            
            var curPos = directionalPad.get(current);
            var newPos = new Position(curPos.row + diffRow, curPos.col + diffCol);
            System.out.println(name + ": direction: " + direction + ", curPos: " + curPos + ", newPos: " + newPos);
            current = directionalPad.entries().filter(e -> e.getValue().equals(newPos)).map(e -> e.getKey()).get(0);
        }
        
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
        lines.forEach(this::println);
        return lines.sumToLong(this::calulate);
    }
    
    long calulate(String doorPress) {
        var doorPad   = new NumberPad(   "DoorPad", "A");
        var robot1Pad = new DirectionPad("Rbt1Pad", "A");
        var robot2Pad = new DirectionPad("Rbt2Pad", "A");
        
        var robot1Log = new StringBuilder();
        var robot2Log = new StringBuilder();
        var humanLog  = new StringBuilder();

        FuncList.of(doorPress.split("")).forEach(doorEach -> {
            var robot1Press = doorPad.walkTo(doorEach);
//            System.out.println("  for: " + doorEach + ", robot1Press: " + robot1Press);
            robot1Log.append(robot1Press);
            FuncList.of(robot1Press.split("")).forEach(rb1Each -> {
                var robot2Press = robot1Pad.walkTo(rb1Each);
//                System.out.println("    for: " + rb1Each + ", robot2Press: " + robot2Press);
                robot2Log.append(robot2Press);
                FuncList.of(robot2Press.split("")).forEach(rb2Each -> {
                    var humanPress = robot2Pad.walkTo(rb2Each);
//                    System.out.println("      for: " + rb2Each + ", humanPress: " + humanPress);
                    humanLog.append(humanPress);
                });
            });
        });
//        System.out.println("doorPress:   " + doorPress + "(%d)".formatted(doorPress.length()));
//        System.out.println("robot1Press: " + robot1Log + "(%d)".formatted(robot1Log.length()));
//        System.out.println("robot2Press: " + robot2Log + "(%d)".formatted(robot2Log.length()));
//        System.out.println("humanLog:    " + humanLog  + "(%d)".formatted(humanLog.length()));
//        
        var doorNum    = (long)grab(regex("[0-9]+"), doorPress).map(parseInt).getFirst();
        var humanPress = (long)humanLog.length();
        System.out.println("humanPress: " + humanPress + ", doorNum: " + doorNum);
        return doorNum * humanPress;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("126384", result);
//
//        var doorPad   = new NumberPad(   "    DoorPad", "A");
//        var robot1Pad = new DirectionPad("  Rbt1Pad", "A");
//        var robot2Pad = new DirectionPad("Rbt2Pad", "A");
//        
////        var humanPress = "<v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A";
//        var humanPress = "v<<A>>^AvA^Av<<A>>^AAv<A<A>>^AAvAA^<A>Av<A>^AA<A>Av<A<A>>^AAAvA^<A>A";
//        var step = new AtomicInteger();
//        for (var humanKey : humanPress.split("")) {
//            println("move: " + humanKey);
//            robot2Pad.moveTo(humanKey, key1 -> 
//                robot1Pad.moveTo(key1, keyDoor -> 
//                    doorPad.moveTo(keyDoor, num -> {
//                        println();
//                        println("Step: " + step);
//                        println("Door pressed: " + num);
//                        println();
//                    })));
//            
//            for (int i = 0; i < 10; i++) {
//                System.out.println(robot2Pad.toString(i) + "    " + robot1Pad.toString(i) + "    " + doorPad.toString(i));
//            }
//            step.incrementAndGet();
//        }
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("187808", result);
    }
    
}
