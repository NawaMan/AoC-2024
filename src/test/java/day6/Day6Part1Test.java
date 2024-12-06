package day6;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day6Part1Test extends BaseTest {
    
    static boolean show = false;
    
    static class RC {
        int r; int c;
        RC(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }
    
    Object calulate(FuncList<String> lines) {
        var grid      = new int[lines.size()][lines.get(0).length()];
        var height    = grid.length;
        var width     = grid[0].length;
        var posistion = findStartPosition(lines, grid, height, width);
        var dir       = (char)grid[posistion.r][posistion.c];
        return countVisit(grid, height, width, dir, posistion);
    }
    
    static RC findStartPosition(FuncList<String> lines, int[][] grid, int height, int width) {
        var posistion = new RC(0, 0);
        for (var r = 0; r < height; r++) {
            for (var c = 0; c < width; c++) {
                var ch = lines.get(r).charAt(c);
                grid[r][c] = ch;
                if ((ch == '^') || (ch == 'v') || (ch == '<') || (ch == '>')) {
                    posistion.r = r;
                    posistion.c = c;
                }
            }
        }
        return posistion;
    }

    static int countVisit(int[][] grid, int height, int width, char dir, RC posistion) {
        var direction = adjustDirection(dir);
        while (true) {
            move(posistion, direction, +1);
            
            if (!(posistion.r >= 0 && posistion.r < height)
             || !(posistion.c >= 0 && posistion.c < width)) 
                break;
            
            var ch = (char)grid[posistion.r][posistion.c];
            if (ch == dir) {
                return -1;
            }
            if (ch == '#') {
                dir = turn(dir);
                move(posistion, direction, -1);
                direction = adjustDirection(dir);
                continue;
            }

            grid[posistion.r][posistion.c] = dir;
        }

        showGrid(height, width, grid);
        return countVisit(height, width, grid);
    }

    static void move(RC posistion, RC direction, int sign) {
        posistion.r += sign*direction.r;
        posistion.c += sign*direction.c;
    }

    static char turn(char dir) {
        if      (dir == '^') dir = '>';
        else if (dir == 'v') dir = '<';
        else if (dir == '<') dir = '^';
        else if (dir == '>') dir = 'v';
        return dir;
    }

    static RC adjustDirection(char dir) {
        if (dir == '^') { return new RC(-1,  0); }
        if (dir == 'v') { return new RC( 1,  0); }
        if (dir == '<') { return new RC( 0, -1); }
        if (dir == '>') { return new RC( 0,  1); }
        return null;
    }
    
    static void showGrid(int height, int width, int[][] grid) {
        if (!show)
            return;
        
        System.out.println();
        for (var r = 0; r < height; r++) {
            for (var c = 0; c < width; c++) {
                var ch = (char)grid[r][c];
                System.out.print(ch);
            }
            System.out.println();
        }
        System.out.println();
    }

    static int countVisit(int height, int width, int[][] grid) {
        var count = 0;
        for (var r = 0; r < height; r++) {
            for (var c = 0; c < width; c++) {
                var ch = (char)grid[r][c];
                if ((ch != '.') && (ch != '#'))
                    count++;
            }
        }
        return count;
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
