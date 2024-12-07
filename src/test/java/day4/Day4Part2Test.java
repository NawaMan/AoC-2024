package day4;

import static functionalj.stream.intstream.IntStreamPlus.range;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day4Part2Test extends BaseTest {
    
    record Grid(FuncList<String> lines) {
        char charAt(int r, int c) {
            if ((r < 0) || (r >= lines.size()))          return '.';
            if ((c < 0) || (c >= lines.get(r).length())) return '.';
            return lines.get(r).charAt(c);
        }
    }
    
    Object calulate(FuncList<String> lines) {
        var grid = new Grid(lines);
        var rows = lines.size();
        var cols = lines.get(0).length();
        return range(0, rows).mapToInt(row -> {
                    return range(0, cols).filter(col -> {
                        return checkX(grid, row, col, 'M', 'A', 'S');
                    }).size();
                }).sum();
    }
    boolean checkX(Grid grid, int row, int col, char before, char mid, char after) {
        if (grid.charAt(row, col) != mid)
            return false;
        
        if ((grid.charAt(row - 1, col - 1) == before && grid.charAt(row + 1, col + 1) == after)
         || (grid.charAt(row - 1, col - 1) == after  && grid.charAt(row + 1, col + 1) == before)) {
            return ((grid.charAt(row - 1, col + 1) == before && grid.charAt(row + 1, col - 1) == after)
                 || (grid.charAt(row - 1, col + 1) == after  && grid.charAt(row + 1, col - 1) == before));
        }
        return false;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("9", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("1965", result);
    }
    
}
