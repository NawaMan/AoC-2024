package day6;

import static functionalj.stream.StreamPlus.streamOf;

import java.util.HashSet;
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
    
    static record Grid(FuncList<String> lines, Position startPosition, Position obstaclePosition) {
        char charAt(Position position) {
            return charAt(position.row, position.col);
        }
        char charAt(int r, int c) {
            if ((r < 0) || (r >= lines.size()))          return OutOfBound;
            if ((c < 0) || (c >= lines.get(r).length())) return OutOfBound;
            if ((r == startPosition.row)
             && (c == startPosition.col))
                return Ground;
            if (obstaclePosition != null) {
                if ((r == obstaclePosition.row)
                 && (c == obstaclePosition.col))
                   return Obstacle;
            }
            
            return lines.get(r).charAt(c);
        }
    }
    
    static record Position(int row, int col) {}
    
    static enum Direction {
        North(0, '^', -1,  0),
        East (1, '>',  0,  1),
        South(2, 'v',  1,  0),
        West (3, '>',  0, -1);
        
        final int  index;
        final char symbol;
        final int  nextRow;
        final int  nextCol;
        
        private Direction(int index, char symbol, int nextRow, int nextCol) {
            this.index   = index;
            this.symbol  = symbol;
            this.nextRow = nextRow;
            this.nextCol = nextCol;
        }
        
        static Direction of(char symbol) {
            return streamOf   (values())
                    .findFirst(value -> value.symbol == symbol)
                    .get      ();
        }
        
        Direction turnRight() {
            var newIndex = (index + 1) % 4;
            return streamOf   (values())
                    .findFirst(value -> value.index == newIndex)
                    .get      ();
        }
    }
    
    static record State(Direction direction, Position position) {}
    
    static class Walker {
        private Direction direction;
        private Position  position;
        Walker(Direction direction, Position position) {
            this.direction = direction;
            this.position  = position;
        }
        Direction direction() {
            return direction;
        }
        Position position() {
            return position;
        }
        State state() {
            return new State(direction, position);
        }
        char walk(Grid grid) {
            var currentSymbol = grid.charAt(position.row, position.col);
            if (currentSymbol == OutOfBound) {
                return currentSymbol;
            }
            
            var nextRow    = position.row + direction.nextRow;
            var nextCol    = position.col + direction.nextCol;
            var nextSymbol = grid.charAt(nextRow, nextCol);
            if (nextSymbol == Obstacle) {
                this.direction = direction.turnRight();
                return currentSymbol;
            }
            
            position = new Position(nextRow, nextCol);
            return nextSymbol;
        }
    }
    
    Object calulate(FuncList<String> lines) {
        var start  = findStartPosition(lines);
        var grid   = new Grid(lines, start, null);
        var walker = new Walker(Direction.of(lines.get(start.row).charAt(start.col)), start);
        
        var visiteds = new HashSet<Position>();
        var isDone   = false;
        while (!isDone) {
            visiteds.add(walker.position);
            isDone = (walker.walk(grid) == OutOfBound);
        }
        
        return visiteds.size();
    }
    
    static Position findStartPosition(FuncList<String> lines) {
        return lines.mapWithIndex((row, line) -> {
            var matcher = Pattern.compile("[><\\^v]").matcher(line);
            return matcher.find()
                    ? new Position(row, matcher.start())
                    : null;
        })
        .excludeNull()
        .findFirst()
        .get();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("41", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("4988", result);
    }
    
}
