package day4;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day4Part1Test extends BaseTest {
    
    char[] expected = "XMAS".toCharArray();
    
    Object calulate(FuncList<String> lines) {
        int total = 0;
        
        int size   = lines.size();
        int length = lines.get(0).length();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < length; c++) {
                var ch = charAt(r, c, lines);
                if (ch == 'X') {
                    total += findWord(r, c, lines, 1, expected,  0,  1) ? 1 : 0;
                    total += findWord(r, c, lines, 1, expected,  0,  0) ? 1 : 0;
                    total += findWord(r, c, lines, 1, expected,  0, -1) ? 1 : 0;
                    total += findWord(r, c, lines, 1, expected,  1,  1) ? 1 : 0;
                    total += findWord(r, c, lines, 1, expected,  1,  0) ? 1 : 0;
                    total += findWord(r, c, lines, 1, expected,  1, -1) ? 1 : 0;
                    total += findWord(r, c, lines, 1, expected, -1,  1) ? 1 : 0;
                    total += findWord(r, c, lines, 1, expected, -1,  0) ? 1 : 0;
                    total += findWord(r, c, lines, 1, expected, -1, -1) ? 1 : 0;
                }
            }
        }
        return total;
    }
    
    boolean findWord(int r, int c, FuncList<String> lines, int at, char[] expected, int directionR, int directionC) {
        if (at >= expected.length)
            return true;
        
        r += directionR;
        c += directionC;
        
        var ch = charAt(r, c, lines);
        if (ch != expected[at])
            return false;
        
        return findWord(r, c, lines, at + 1, expected, directionR, directionC);
    }
    
    
    char charAt(int r, int c, FuncList<String> lines) {
        if ((r < 0) || (r >= lines.size()))
            return '.';
        if ((c < 0) || (c >= lines.get(r).length()))
            return '.';
        return lines.get(r).charAt(c);
    }
//    
//    Object calulate(FuncList<String> lines) {
//        var line0   = lines;
//        var line45  = rotate45(line0);
//        var line90  = rotate90(lines);
//        var line135 = rotate45(line90);
//        var line180 = rotate90(line90);
//        var line225 = rotate45(line180);
//        var line270 = rotate90(line180);
//        var line315 = rotate45(line270);
//        
//        return FuncList.of(line0, line45, line90, line135, line180, line225, line270, line315)
//                .mapToInt(this::countWord).sum();
//    }
//    
//    int countWord(FuncList<String> lines) {
//        return countWord(lines, "XMAS");
//    }
//    
//    int countWord(FuncList<String> lines, String word) {
//        return lines.mapToInt(line -> StrFuncs.grab(line, word).size()).sum();
//    }
//    
//    FuncList<String> rotate90(FuncList<String> lines) {
//        var size = lines.size();
//        return range(0, lines.get(0).length())
//                .mapToObj(r -> range(0, size).mapToObj(c -> lines.get(c).charAt(r)).join())
//                .toFuncList();
//    }
//    
//    char charAt(int r, int c, FuncList<String> lines) {
//        if ((r < 0) || (r >= lines.size()))
//            return '.';
//        if ((c < 0) || (c >= lines.get(r).length()))
//            return '.';
//        return lines.get(r).charAt(c);
//    }
//    
//    FuncList<String> rotate45(FuncList<String> lines) {
//        int size   = lines.size();
//        int length = lines.get(0).length();
//        int width   = size + length;
//        var grid = new int[width + 1][width + 1];
//        
//        for (int y = 0; y < grid.length; y++) {
//            for (int x = 0; x < grid[y].length; x++) {
//                grid[y][x] = '.';
//            }
//        }
//        
//        for (int r = 0; r < size; r++) {
//            for (int c = 0; c < length; c++) {
//                int x = +c + -r + size;
//                int y = -c + -r + size + length;
//                grid[y][x] = charAt(r, c, lines);
//            }
//        }
//        return FuncList.of(grid).map(row -> IntFuncList.of(row).mapToObj(ch -> (char)ch).join().replaceAll("(.)\\.", "$1"));
//    }
//    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("18", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("2603", result);
    }
    
}