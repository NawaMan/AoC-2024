package day4;

import static functionalj.stream.intstream.IntStreamPlus.range;

import java.util.function.IntBinaryOperator;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.stream.intstream.IntStreamPlus;

public class Day4Part1Test extends BaseTest {
    
    static FuncList<RC> allDirections = FuncList.of(
            new RC(-1,  1), new RC(-1,  0), new RC(-1, -1),
            new RC( 0,  1),                 new RC( 0, -1), 
            new RC( 1,  1), new RC( 1,  0), new RC( 1, -1));
    
    static record RC(int r, int c) {
    }
    
    static record Grid(FuncList<String> lines) {
        char charAt(int r, int c) {
            if ((r < 0) || (r >= lines.size()))          return '.';
            if ((c < 0) || (c >= lines.get(r).length())) return '.';
            return lines.get(r).charAt(c);
        }
        IntStreamPlus visitAll(IntBinaryOperator operator) {
            var rows = lines.size();
            var cols = lines.get(0).length();
            return range(0, rows).flatMapToInt(r -> {
                        return range(0, cols).mapToInt(c -> {
                            return operator.applyAsInt(r, c);
                        });
                    });
        }
    }
    
    Object calulate(FuncList<String> lines) {
        var grid = new Grid(lines);
        return grid.visitAll((r, c) -> {
            return (grid.charAt(r, c) == 'X')
                    ? allDirections.filter(dir -> findWord(grid, r, c, dir, 1, "XMAS")).size()
                    : 0;
        }).sum();
    }
    
    boolean findWord(Grid grid, int r, int c, RC dir, int at, String word) {
        if (at >= word.length())
            return true;
        
        r += dir.r;
        c += dir.c;
        return grid.charAt(r, c) == word.charAt(at)
            && findWord(grid, r, c, dir, at + 1, word);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("18", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("2603", result);
    }
    
}