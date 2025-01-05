package day6;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

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
    
    static record Grid(FuncList<String> lines, Position startPosition) {
        char charAt(Position pos) {
            return charAt(pos.row, pos.col);
        }
        char charAt(int row, int col) {
            if ((row < 0) || (row >= lines.size()))            return OutOfBound;
            if ((col < 0) || (col >= lines.get(row).length())) return OutOfBound;
            if (startPosition.isAt(row, col))                  return Ground;
            return lines.get(row).charAt(col);
        }
    }
    
    static class Walker {
        Position  position;
        Direction direction;
        Walker(Position position, Direction direction) {
            this.direction = direction;
            this.position  = position;
        }
        char walk(Grid grid) {
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
    
    int countVisitedBlocks(FuncList<String> lines) {
        var start     = findStartPosition(lines);
        var direction = Direction.of(lines.get(start.row).charAt(start.col));
        var grid      = new Grid(lines, start);
        var walker    = new Walker(start, direction);
        var visiteds  = new HashSet<Position>();
        var isDone    = false;
        while (!isDone) {
            visiteds.add(walker.position);
            isDone = (walker.walk(grid) == OutOfBound);
        }
        return visiteds.size();
    }
    
    //== Test ==
    
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
