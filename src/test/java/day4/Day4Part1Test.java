package day4;

import static functionalj.stream.intstream.IntStreamPlus.range;

import java.util.function.IntBinaryOperator;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

public class Day4Part1Test extends BaseTest {
    
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
        
        int wordCount(String word) {
            var first = word.charAt(0);
            return grid.loop((r, c) -> {
                return (grid.charAt(r, c) == first)
                        ? findWords(word, r, c)
                        : 0;
            }).sum();
        }
        
        private int findWords(String word, int r, int c) {
            int total = 0;
            total += findWord(r, c,  0,  1, word) ? 1 : 0;
            total += findWord(r, c,  0,  0, word) ? 1 : 0;
            total += findWord(r, c,  0, -1, word) ? 1 : 0;
            total += findWord(r, c,  1,  1, word) ? 1 : 0;
            total += findWord(r, c,  1,  0, word) ? 1 : 0;
            total += findWord(r, c,  1, -1, word) ? 1 : 0;
            total += findWord(r, c, -1,  1, word) ? 1 : 0;
            total += findWord(r, c, -1,  0, word) ? 1 : 0;
            total += findWord(r, c, -1, -1, word) ? 1 : 0;
            return total;
        }
        private boolean findWord(int r, int c, int directionR, int directionC, String word) {
            return findWord(r, c, directionR, directionC, 1, word);
        }
        private boolean findWord(int r, int c, int directionR, int directionC, int at, String word) {
            if (at >= word.length())
                return true;
            
            r += directionR;
            c += directionC;
            return grid.charAt(r, c) == word.charAt(at)
                && findWord(r, c, directionR, directionC, at + 1, word);
        }
    }
    
    Object calulate(FuncList<String> lines) {
        var wordGrid  = new WordGrid(new Grid(lines));
        return wordGrid.wordCount("XMAS");
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