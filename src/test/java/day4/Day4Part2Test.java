package day4;

import static functionalj.stream.intstream.IntStreamPlus.range;

import java.util.function.IntBinaryOperator;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

public class Day4Part2Test extends BaseTest {
    
    record Grid(FuncList<String> lines) {
        char charAt(int r, int c) {
            if ((r < 0) || (r >= lines.size()))          return '.';
            if ((c < 0) || (c >= lines.get(r).length())) return '.';
            return lines.get(r).charAt(c);
        }
        IntFuncList loop(IntBinaryOperator operator) {
            var rows = lines.size();
            var cols = lines.get(0).length();
            return range(0, rows).flatMapToInt(r -> {
                        return range(0, cols).mapToInt(c -> {
                            return operator.applyAsInt(r, c);
                        });
                    })
                    .toFuncList();
        }
    }
    record WordGrid(Grid grid) {
        int wordX(String word) {
            if (word.length() != 3)
                throw new IllegalArgumentException("Only 3-character-long word is supported.");
            
            var mid    = word.charAt(1);
            var before = word.charAt(0);
            var after  = word.charAt(2);
            return grid.loop((r, c) -> {
                return (grid.charAt(r, c) != mid)
                        ? 0
                        : checkX(r, c, before, after) ? 1 : 0;
            }).sum();
        }
        boolean checkX(int r, int c, char before, char after) {
            if ((grid.charAt(r - 1, c - 1) == before && grid.charAt(r + 1, c + 1) == after)
             || (grid.charAt(r - 1, c - 1) == after  && grid.charAt(r + 1, c + 1) == before)) {
                return ((grid.charAt(r - 1, c + 1) == before && grid.charAt(r + 1, c - 1) == after)
                     || (grid.charAt(r - 1, c + 1) == after  && grid.charAt(r + 1, c - 1) == before));
            }
            return false;
        }
    }
    
    Object calulate(FuncList<String> lines) {
        var wordGrid = new WordGrid(new Grid(lines));
        return wordGrid.wordX("MAS");
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
