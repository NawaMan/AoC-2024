package day6;

import static day6.State.theState;
import static functionalj.stream.StreamPlus.generate;
import static functionalj.stream.intstream.IntStreamPlus.range;
import static java.util.regex.Pattern.compile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.stream.StreamPlus;
import functionalj.types.Struct;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * --- Day 6: Guard Gallivant ---
 * 
 * The Historians use their fancy device again, this time to whisk you all away to the North Pole prototype suit 
 *   manufacturing lab... in the year 1518! It turns out that having direct access to history is very convenient for 
 *   a group of historians.
 * 
 * You still have to be careful of time paradoxes, and so it will be important to avoid anyone from 1518 while 
 *   The Historians search for the Chief. Unfortunately, a single guard is patrolling this part of the lab.
 * 
 * Maybe you can work out where the guard will go ahead of time so that The Historians can search safely?
 * 
 * You start by making a map (your puzzle input) of the situation. For example:
 * 
 * ....#.....
 * .........#
 * ..........
 * ..#.......
 * .......#..
 * ..........
 * .#..^.....
 * ........#.
 * #.........
 * ......#...
 * 
 * The map shows the current position of the guard with ^ (to indicate the guard is currently facing up from the 
 *   perspective of the map). Any obstructions - crates, desks, alchemical reactors, etc. - are shown as #.
 * 
 * Lab guards in 1518 follow a very strict patrol protocol which involves repeatedly following these steps:
 * 
 *     If there is something directly in front of you, turn right 90 degrees.
 *     Otherwise, take a step forward.
 * 
 * Following the above protocol, the guard moves up several times until she reaches an obstacle (in this case, a pile 
 *   of failed suit prototypes):
 * 
 * ....#.....
 * ....^....#
 * ..........
 * ..#.......
 * .......#..
 * ..........
 * .#........
 * ........#.
 * #.........
 * ......#...
 * 
 * Because there is now an obstacle in front of the guard, she turns right before continuing straight in her new facing 
 *   direction:
 * 
 * ....#.....
 * ........>#
 * ..........
 * ..#.......
 * .......#..
 * ..........
 * .#........
 * ........#.
 * #.........
 * ......#...
 * 
 * Reaching another obstacle (a spool of several very long polymers), she turns right again and continues downward:
 * 
 * ....#.....
 * .........#
 * ..........
 * ..#.......
 * .......#..
 * ..........
 * .#......v.
 * ........#.
 * #.........
 * ......#...
 * 
 * This process continues for a while, but the guard eventually leaves the mapped area (after walking past a tank of 
 *   universal solvent):
 * 
 * ....#.....
 * .........#
 * ..........
 * ..#.......
 * .......#..
 * ..........
 * .#........
 * ........#.
 * #.........
 * ......#v..
 * 
 * By predicting the guard's route, you can determine which specific positions in the lab will be in the patrol path. 
 * Including the guard's starting position, the positions visited by the guard before leaving the area are marked with 
 *   an X:
 * 
 * ....#.....
 * ....XXXXX#
 * ....X...X.
 * ..#.X...X.
 * ..XXXXX#X.
 * ..X.X.X.X.
 * .#XXXXXXX.
 * .XXXXXXX#.
 * #XXXXXXX..
 * ......#X..
 * 
 * In this example, the guard will visit 41 distinct positions on your map.
 * 
 * Predict the path of the guard. How many distinct positions will the guard visit before leaving the mapped area?
 * 
 * Your puzzle answer was 4988.
 */
public class Day6Part1Test extends BaseTest {
    
    static final char Obstacle   = '#';
    static final char Ground     = '.';
    static final char OutOfBound = 'X';
    
    private static final Map<Character, Direction> directionsBySymbols = new HashMap<>();
    
    static enum Direction {
        North('^', -1,  0),
        East ('>',  0,  1),
        South('v',  1,  0),
        West ('<',  0, -1);
        
        final char symbol;
        final int  nextRow;
        final int  nextCol;
        
        private Direction(char symbol, int nextRow, int nextCol) {
            this.symbol  = symbol;
            this.nextRow = nextRow;
            this.nextCol = nextCol;
            directionsBySymbols.put(symbol, this);
        }
        
        static Direction of(char symbol) {
            return directionsBySymbols.get(symbol);
        }
        
        Direction turnRight() {
            return values()[(ordinal() + 1) % 4];
        }
    }
    
    static record Position(int row, int col) {
        boolean  isAt(int row, int col) {
            return (this.row == row) && (this.col == col);
        }
        Position move(Direction dir) {
            return new Position(row + dir.nextRow, col + dir.nextCol);
        }
    }
    
    static class Grid {
        final FuncList<String> lines;
        final Position         startPosition;
        final Direction        startDirection;
        Grid(FuncList<String> lines) {
            this.lines          = lines;
            this.startPosition  = findStartPosition();
            this.startDirection = Direction.of(lines.get(startPosition.row()).charAt(startPosition.col()));
        }
        char charAt(Position position) {
            return charAt(position.row, position.col);
        }
        char charAt(int row, int col) {
            if ((row < 0) || (row >= lines.size()))            return OutOfBound;
            if ((col < 0) || (col >= lines.get(row).length())) return OutOfBound;
            
            // Mask the staring position as a ground because once the walk start this will be seen as a ground position.
            if (startPosition.isAt(row, col))
                return Ground;
            
            return lines.get(row).charAt(col);
        }
        private Position findStartPosition() {
            return lines
                    .map  (line    -> compile("[><\\^v]").matcher(line))
                    .query(matcher -> matcher.find())
                    .map  (result  -> new Position(result.index(), result.getValue().start()))
                    .first()
                    .get();
        }
    }
    
    @Getter
    @Accessors(fluent = true)
    static class GridWalker {
        
        @Struct
        static void State(Position position, Direction direction, boolean isOutOfBound) {}
        
        final   Grid      grid;
        private Position  position;
        private Direction direction;
        
        private Set<State> visiteds = new HashSet<State>();
        
        GridWalker(Grid grid) {
            this.grid      = grid;
            this.position  = grid.startPosition;
            this.direction = grid.startDirection;
            visiteds.add(new State(position, direction, false));
        }
        StreamPlus<State> steps() {
            return generate     (()    -> step())
                    .acceptWhile(state -> state != null)        // Loop
                    .dropAfter  (state -> state.isOutOfBound()) // Out of bound.
                    .prependWith(Stream.of(new State(grid.startPosition, grid.startDirection, false)));
        }
        private State step() {
            var state = walkStep();
            if (visiteds.contains(state))   // In case of a loop.
                return null;
            
            visiteds.add(state);
            return state;
        }
        private State walkStep() {
            var currentSymbol = grid.charAt(position.row, position.col);
            if (currentSymbol == OutOfBound) 
                return new State(position, direction, true);
            
            var nextPosition = position.move(direction);
            var nextSymbol   = grid.charAt(nextPosition); 
            if (nextSymbol == Obstacle) {
                this.direction = direction.turnRight();
                return new State(position, direction, false);
            }
            
            position = nextPosition;
            return new State(position, direction, nextSymbol == OutOfBound);
        }
    }
    
    int countVisitedBlocks(FuncList<String> lines) {
        var grid   = new Grid(lines);
        var walker = new GridWalker(grid);
        return walker
                .steps   ()
                .exclude (theState.isOutOfBound)
                .map     (State::position)
                .distinct()
                .size    ();
    }
    
    //== Display for debug ==
    
    public static final String BOLD  = "\033[1m";
    public static final String BLUE  = "\033[94m";
    public static final String DARK  = "\033[34m";
    public static final String RESET = "\033[0m"; // Reset to default color
    
    static void drawGridWalker(String indent, GridWalker walker, Map<Position, Direction> visiteds) throws InterruptedException {
        System.out.println();
        for (int row = 0; row < walker.grid.lines.size(); row++) {
            System.out.print(indent);
            for (int col = 0; col < walker.grid.lines.get(0).length(); col++) {
                if (walker.position.isAt(row, col)) {
                    System.out.print(BOLD + BLUE + walker.direction.symbol + RESET);
                } else if (visiteds.containsKey(new Position(row, col))) {
                    System.out.print(BOLD + DARK + visiteds.get(new Position(row, col)).symbol + RESET);
                } else {
                    System.out.print(walker.grid.charAt(row, col));
                }
            }
            System.out.println();
        }
    }
    
    //== Test ==
    
    public static void main(String ... args) throws InterruptedException, IOException {
        System.out.print("\033[H\033[2J");
        System.out.flush();
      
        var lines    = FuncList.from(Files.readAllLines(Path.of("/home/nawa/dev/git/AoC-2024/data", "day6", "day6-part1-example.txt")));
        var grid     = new Grid(lines);
        var walker   = new GridWalker(grid);
        var visiteds = new HashMap<Position, Direction>();
        
        drawGridWalker("                         ", walker, visiteds);
        range(0, 5).forEach(__ -> System.out.println());
        Thread.sleep(2000);
        
        do {
            System.out.print("\033[1;1H");
            System.out.flush();
            
            drawGridWalker("                         ", walker, visiteds);
            range(0, 5).forEach(__ -> System.out.println());
            
            visiteds.put(walker.position, walker.direction);
            walker.step();
            
            if ((walker.position.row < 0 || walker.position.row >= walker.grid.lines.size())
             || (walker.position.col < 0 || walker.position.col >= walker.grid.lines.get(0).length()))
                break;
            Thread.sleep(200);
            
            // if (!Console.readln().trim().equals("exit")) break;
        } while (true);
        System.exit(0);
    }
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = countVisitedBlocks(lines);
        println("result: " + result);
        assertAsString("41", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = countVisitedBlocks(lines);
        println("result: " + result);
        assertAsString("4988", result);
    }
    
}
