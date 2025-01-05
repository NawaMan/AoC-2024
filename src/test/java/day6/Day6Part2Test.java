package day6;


import static common.AocCommon.TwoRanges.loop2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

/**
 * --- Part Two ---
 * 
 * While The Historians begin working around the guard's patrol route, you borrow their fancy device and step outside 
 *   the lab. From the safety of a supply closet, you time travel through the last few months and record the nightly 
 *   status of the lab's guard post on the walls of the closet.
 * 
 * Returning after what seems like only a few seconds to The Historians, they explain that the guard's patrol area is 
 *   simply too large for them to safely search the lab without getting caught.
 * 
 * Fortunately, they are pretty sure that adding a single new obstruction won't cause a time paradox. They'd like to 
 *   place the new obstruction in such a way that the guard will get stuck in a loop, making the rest of the lab safe to
 *   search.
 * 
 * To have the lowest chance of creating a time paradox, The Historians would like to know all of the possible positions
 *   for such an obstruction. The new obstruction can't be placed at the guard's starting position - the guard is there
 *   right now and would notice.
 * 
 * In the above example, there are only 6 different positions where a new obstruction would cause the guard to get stuck
 *   in a loop. The diagrams of these six situations use O to mark the new obstruction, | to show a position where the
 *   guard moves up/down, - to show a position where the guard moves left/right, and + to show a position where the 
 *   guard moves both up/down and left/right.
 * 
 * Option one, put a printing press next to the guard's starting position:
 * 
 * ....#.....
 * ....+---+#
 * ....|...|.
 * ..#.|...|.
 * ....|..#|.
 * ....|...|.
 * .#.O^---+.
 * ........#.
 * #.........
 * ......#...
 * 
 * Option two, put a stack of failed suit prototypes in the bottom right quadrant of the mapped area:
 * 
 * ....#.....
 * ....+---+#
 * ....|...|.
 * ..#.|...|.
 * ..+-+-+#|.
 * ..|.|.|.|.
 * .#+-^-+-+.
 * ......O.#.
 * #.........
 * ......#...
 * 
 * Option three, put a crate of chimney-squeeze prototype fabric next to the standing desk in the bottom right quadrant:
 * 
 * ....#.....
 * ....+---+#
 * ....|...|.
 * ..#.|...|.
 * ..+-+-+#|.
 * ..|.|.|.|.
 * .#+-^-+-+.
 * .+----+O#.
 * #+----+...
 * ......#...
 * 
 * Option four, put an alchemical retroencabulator near the bottom left corner:
 * 
 * ....#.....
 * ....+---+#
 * ....|...|.
 * ..#.|...|.
 * ..+-+-+#|.
 * ..|.|.|.|.
 * .#+-^-+-+.
 * ..|...|.#.
 * #O+---+...
 * ......#...
 * 
 * Option five, put the alchemical retroencabulator a bit to the right instead:
 * 
 * ....#.....
 * ....+---+#
 * ....|...|.
 * ..#.|...|.
 * ..+-+-+#|.
 * ..|.|.|.|.
 * .#+-^-+-+.
 * ....|.|.#.
 * #..O+-+...
 * ......#...
 * 
 * Option six, put a tank of sovereign glue right next to the tank of universal solvent:
 * 
 * ....#.....
 * ....+---+#
 * ....|...|.
 * ..#.|...|.
 * ..+-+-+#|.
 * ..|.|.|.|.
 * .#+-^-+-+.
 * .+----++#.
 * #+----++..
 * ......#O..
 * 
 * It doesn't really matter what you choose to use as an obstacle so long as you and The Historians can put it into
 *   position without the guard noticing. The important thing is having enough options that you can find one that
 *   minimizes time paradoxes, and in this example, there are 6 different positions you could choose.
 * 
 * You need to get the guard stuck in a loop by adding a single new obstruction. How many different positions could you
 *   choose for this obstruction?
 * 
 * Your puzzle answer was 1697.
 */
public class Day6Part2Test extends BaseTest {
    
    static final char Obstacle   = '#';
    static final char Ground     = '.';
    static final char OutOfBound = 'X';
    
    private static final Map<Character, Direction> directBySymbols = new HashMap<>();
    
    static enum Direction {
        North('^', -1,  0),
        East ('>',  0,  1),
        South('v',  1,  0),
        West ('>',  0, -1);
        
        final char symbol;
        final int  nextRow;
        final int  nextCol;
        
        private Direction(char symbol, int nextRow, int nextCol) {
            this.symbol  = symbol;
            this.nextRow = nextRow;
            this.nextCol = nextCol;
            directBySymbols.put(symbol, this);
        }
        
        static Direction of(char symbol) {
            return directBySymbols.get(symbol);
        }
        
        Direction turnRight() {
            return values()[(ordinal() + 1) % 4];
        }
    }
    
    static record Position(int row, int col) {
        boolean  isAt(int row, int col) { return (this.row == row) && (this.col == col);             }
        Position move(Direction dir)    { return new Position(row + dir.nextRow, col + dir.nextCol); }
    }
    
    static record Grid(FuncList<String> lines, Position startPosition, Position obstacle) {
        char charAt(Position position) {
            return charAt(position.row, position.col);
        }
        char charAt(int row, int col) {
            if ((row < 0) || (row >= lines.size()))            return OutOfBound;
            if ((col < 0) || (col >= lines.get(row).length())) return OutOfBound;
            if (startPosition.isAt(row, col))                  return Ground;
            if ((obstacle != null) && obstacle.isAt(row, col)) return Obstacle;
            return lines.get(row).charAt(col);
        }
    }
    
    static record State(Position position, Direction direction) {}
    
    static class Walker {
        Grid      grid;
        Position  position;
        Direction direction;
        Walker(Grid grid, Position position, Direction direction) {
            this.grid      = grid;
            this.direction = direction;
            this.position  = position;
        }
        State state() {
            return new State(position, direction);
        }
        char walk() {
            var currentSymbol = grid.charAt(position.row, position.col);
            if (currentSymbol == OutOfBound) return currentSymbol;
            
            var nextPosition = position.move(direction);
            var nextSymbol   = grid.charAt(nextPosition);
            if (nextSymbol == Obstacle) {
                this.direction = direction.turnRight();
                return currentSymbol;
            }
            
            position = nextPosition;
            return nextSymbol;
        }
    }
    
    static Position findStartPosition(FuncList<String> lines) {
        return lines
                .map  (line    -> Pattern.compile("[><\\^v]").matcher(line))
                .query(matcher -> matcher.find())
                .map  (result  -> new Position(result.index(), result.getValue().start()))
                .first()
                .get();
    }
    
    int countPosibleLoop(FuncList<String> lines) {
        var startPosition  = findStartPosition(lines);
        var startDirection = Direction.of(lines.get(startPosition.row()).charAt(startPosition.col()));
        int gridHeight = lines.size();
        int gridWidth  = lines.get(0).length();
        return loop2   (gridHeight, gridWidth)
                .map   ((row, col) -> new Position(row, col))
                .map   (position   -> new Grid  (lines, startPosition, position))
                .map   (grid       -> new Walker(grid, startPosition, startDirection))
                .filter(walker     -> checkIfStuckInLoop(walker))
                .size();
    }
    
    private boolean checkIfStuckInLoop(Walker walker) {
        var visiteds = new HashSet<State>();
        while (true) {
            var state = walker.state();
            if (!visiteds.add(state))
                return true;
            
            if (walker.walk() == OutOfBound)
                return false;
        }
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = countPosibleLoop(lines);
        println("result: " + result);
        assertAsString("6", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = countPosibleLoop(lines);
        println("result: " + result);
        assertAsString("1697", result);
    }
    
}
