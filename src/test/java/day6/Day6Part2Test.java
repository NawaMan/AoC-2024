package day6;

import static day6.Day6Part1Test.countVisit;
import static day6.Day6Part1Test.findStartPosition;

import org.junit.Test;

import common.BaseTest;
import day6.Day6Part1Test.RC;
import functionalj.list.FuncList;

public class Day6Part2Test extends BaseTest {
    
    Object calulate(FuncList<String> lines) {
        var grid      = new int[lines.size()][lines.get(0).length()];
        var height    = grid.length;
        var width     = grid[0].length;
        var posistion = findStartPosition(lines, grid, height, width);
        var dir       = (char)grid[posistion.r][posistion.c];
        return countLoop(height, width, grid, dir, posistion);
    }
    
    int countLoop(int height, int width, int[][] grid, char dir, RC posistion) {
        int count = 0;
        for (var r = 0; r < height; r++) {
            for (var c = 0; c < width; c++) {
                var ch = grid[r][c];
                if (ch == '.') {
                    var hasLoop = hasLoop(grid, dir, posistion, r, c);
                    count += hasLoop ? 1 : 0;
                }
            }
        }
        return count;
    }

    boolean hasLoop(int[][] orgGrid, char dir, RC posistion, int blockR, int blockC) {
        var grid = deepClone2DArray(orgGrid);
        grid[blockR][blockC] = '#';

        var height = grid.length;
        var width  = grid[0].length;
        var count  = countVisit(grid, height, width, dir, new RC(posistion.r, posistion.c));
        return count == -1;
    }
    
    int[][] deepClone2DArray(int[][] original) {
        int[][] clonedArray = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            clonedArray[i] = original[i].clone(); // Clone each sub-array
            for (int j = 0; j < clonedArray[i].length; j++) {
                if ((clonedArray[i][j] != '.') && (clonedArray[i][j] != '#'))
                    clonedArray[i][j] = '.';
            }
        }
        return clonedArray;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("6", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("1697", result);
    }
    
}
