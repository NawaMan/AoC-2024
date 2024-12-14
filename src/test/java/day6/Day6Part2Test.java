package day6;

import static day6.Day6Part1Test.OutOfBound;
import static day6.Day6Part1Test.findStartPosition;

import java.util.HashSet;

import org.junit.Test;

import common.BaseTest;
import day6.Day6Part1Test.Direction;
import day6.Day6Part1Test.Grid;
import day6.Day6Part1Test.Position;
import day6.Day6Part1Test.State;
import day6.Day6Part1Test.Walker;
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
    
    int countPosibleLoop(FuncList<String> lines) {
        var startPosition  = findStartPosition(lines);
        var startDirection = Direction.of(lines.get(startPosition.row()).charAt(startPosition.col()));
        
        var total = 0;
        for(int row = 0; row < lines.size(); row++) {
            for(int col = 0; col < lines.get(row).length(); col++) {
                var block  = new Position(row, col);
                var grid   = new Grid(lines, startPosition, block);
                var walker = new Walker(startDirection, startPosition);
                
                var visiteds = new HashSet<State>();
                while (true) {
                    var state = walker.state();
                    if (visiteds.contains(state)) {
                        total++;
                        break;
                    }
                    
                    visiteds.add(state);
                    if (walker.walk(grid) == OutOfBound)
                        break;
                }
            }
        }
        
        return total;
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
