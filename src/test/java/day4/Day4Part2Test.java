package day4;

import static functionalj.stream.intstream.IntStreamPlus.range;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

/**
 * --- Part Two ---
 * 
 * The Elf looks quizzically at you. Did you misunderstand the assignment?
 * 
 * Looking for the instructions, you flip over the word search to find that this isn't actually an XMAS puzzle; it's an
 *   X-MAS puzzle in which you're supposed to find two MAS in the shape of an X. One way to achieve that is like this:
 * 
 * M.S
 * .A.
 * M.S
 * 
 * Irrelevant characters have again been replaced with . in the above diagram. Within the X, each MAS can be written
 *   forwards or backwards.
 * 
 * Here's the same example from before, but this time all of the X-MASes have been kept instead:
 * 
 * .M.S......
 * ..A..MSMS.
 * .M.S.MAA..
 * ..A.ASMSM.
 * .M.S.M....
 * ..........
 * S.S.S.S.S.
 * .A.A.A.A..
 * M.M.M.M.M.
 * ..........
 * 
 * In this example, an X-MAS appears 9 times.
 * 
 * Flip the word search from the instructions back over to the word search side and try again. How many times does an
 *   X-MAS appear?
 * 
 * Your puzzle answer was 1965.
 */
public class Day4Part2Test extends BaseTest {
    
    record Grid(FuncList<String> lines) {
        char charAt(int r, int c) {
            if ((r < 0) || (r >= lines.size()))          return '.';
            if ((c < 0) || (c >= lines.get(r).length())) return '.';
            return lines.get(r).charAt(c);
        }
    }
    
    int countXMas(FuncList<String> lines) {
        var grid = new Grid(lines);
        var rows = lines.size();
        var cols = lines.get(0).length();
        return range(0, rows).sum(row -> {
                    return range(0, cols).filter(col -> {
                        return checkX(grid, row, col, 'M', 'A', 'S');
                    }).size();
                });
    }
    boolean checkX(Grid grid, int row, int col, char before, char mid, char after) {
        return (grid.charAt(row, col) == mid)
            && checkHaftX(grid, row, col, before, after, 1,  1)
            && checkHaftX(grid, row, col, before, after, 1, -1);
    }
    private boolean checkHaftX(Grid grid, int row, int col, char before, char after, int diffRow, int diffCol) {
        var forward  = grid.charAt(row + diffRow, col + diffCol);
        var backward = grid.charAt(row - diffRow, col - diffCol);
        return (backward == before && forward == after)
            || (backward == after  && forward == before);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = countXMas(lines);
        println("result: " + result);
        println();
        assertAsString("9", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = countXMas(lines);
        println("result: " + result);
        assertAsString("1965", result);
    }
    
}
