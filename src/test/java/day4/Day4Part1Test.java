package day4;

import static functionalj.stream.intstream.IntStreamPlus.range;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

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
    
    static record Direction(int row, int col) {}
    
    static FuncList<Direction> allDirections = FuncList.of(
            new Direction(-1, -1), new Direction(-1,  0), new Direction(-1,  1),
            new Direction( 0, -1),                        new Direction( 0,  1), 
            new Direction( 1, -1), new Direction( 1,  0), new Direction( 1,  1));
    
    static record WordSearch(FuncList<String> grid, FuncList<Direction> searchDirections) {
        char charAt(int row, int col) {
            if ((row < 0) || (row >= grid.size()))            return '.';
            if ((col < 0) || (col >= grid.get(row).length())) return '.';
            return grid.get(row).charAt(col);
        }
        int countWordAt(int row, int col, String word) {
            return searchDirections
                    .filter(dir -> findWord(row, col, dir, word))
                    .size();
        }
        private boolean findWord(int row, int col, Direction dir, String word) {
            return findWord(row, col, dir, 0, word);
        }
        private boolean findWord(int row, int col, Direction dir, int index, String word) {
            return (index >= word.length())
                || ((charAt(row, col) == word.charAt(index)) 
                        && findWord(row + dir.row(), col + dir.col(), dir, index + 1, word));
        }
    }
    
    int countXMas(FuncList<String> lines) {
        var grid = new WordSearch(lines, allDirections);
        var rows = lines.size();
        var cols = lines.get(0).length();
        return range(0, rows).sum(row -> {
            return range(0, cols)
                    .sum(col -> grid.countWordAt(row, col, "XMAS"));
        });
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = countXMas(lines);
        println("result: " + result);
        assertAsString("18", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = countXMas(lines);
        println("result: " + result);
        assertAsString("2603", result);
    }
    
}