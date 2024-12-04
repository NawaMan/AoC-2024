package day4;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day4Part2Test extends BaseTest {
    
    
    Object calulate(FuncList<String> lines) {
        int total = 0;
        
        int size = lines.size();
        for (int r = 0; r < size; r++) {
            int length = lines.get(r).length();
            for (int c = 0; c < length; c++) {
                var ch = charAt(r, c, lines);
                if (ch == 'A') {
                    total += findX(r, c, lines) ? 1 : 0;
                }
            }
        }
        return total;
    }
    
    boolean findX(int r, int c, FuncList<String> lines) {
        if ((charAt(r - 1, c - 1, lines) == 'M' && charAt(r + 1, c + 1, lines) == 'S')
         || (charAt(r - 1, c - 1, lines) == 'S' && charAt(r + 1, c + 1, lines) == 'M')) {
            return ((charAt(r - 1, c + 1, lines) == 'M' && charAt(r + 1, c - 1, lines) == 'S')
                 || (charAt(r - 1, c + 1, lines) == 'S' && charAt(r + 1, c - 1, lines) == 'M'));
        }
        return false;
    }
    
    char charAt(int r, int c, FuncList<String> lines) {
        if ((r < 0) || (r >= lines.size()))          return '.';
        if ((c < 0) || (c >= lines.get(r).length())) return '.';
        return lines.get(r).charAt(c);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines = readAllLines();
        lines.forEach(this::println);
        println();
        
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("9", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        lines.forEach(this::println);
        println();
        
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("1965", result);
    }
    
}
