package day4;

import static functionalj.stream.intstream.IntStreamPlus.range;

import java.util.function.IntBinaryOperator;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.stream.intstream.IntStreamPlus;

/**
 * --- Day 4: Ceres Search ---
 * 
 * "Looks like the Chief's not here. Next!" One of The Historians pulls out a device and pushes the only button on it.
 *   After a brief flash, you recognize the interior of the Ceres monitoring station!
 * 
 * As the search for the Chief continues, a small Elf who lives on the station tugs on your shirt; she'd like to know
 *   if you could help her with her word search (your puzzle input). She only has to find one word: XMAS.
 * 
 * This word search allows words to be horizontal, vertical, diagonal, written backwards, or even overlapping other
 *   words. It's a little unusual, though, as you don't merely need to find one instance of XMAS - you need to find all
 *   of them. Here are a few ways XMAS might appear, where irrelevant characters have been replaced with .:
 * 
 * ..X...
 * .SAMX.
 * .A..A.
 * XMAS.S
 * .X....
 * 
 * The actual word search will be full of letters instead. For example:
 * 
 * MMMSXXMASM
 * MSAMXMSMSA
 * AMXSXMAAMM
 * MSAMASMSMX
 * XMASAMXAMM
 * XXAMMXXAMA
 * SMSMSASXSS
 * SAXAMASAAA
 * MAMMMXMMMM
 * MXMXAXMASX
 * 
 * In this word search, XMAS occurs a total of 18 times; here's the same word search again, but where letters not
 *   involved in any XMAS have been replaced with .:
 * 
 * ....XXMAS.
 * .SAMXMS...
 * ...S..A...
 * ..A.A.MS.X
 * XMASAMX.MM
 * X.....XA.A
 * S.S.S.S.SS
 * .A.A.A.A.A
 * ..M.M.M.MM
 * .X.X.XMASX
 * 
 * Take a look at the little Elf's word search. How many times does XMAS appear?
 * 
 * Your puzzle answer was 2603.
 */
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