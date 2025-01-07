package day8;

import static functionalj.functions.StrFuncs.matches;
import static functionalj.stream.intstream.IntStreamPlus.infinite;

import java.util.function.IntFunction;

import org.junit.Test;

import common.BaseTest;
import functionalj.function.Func1;
import functionalj.functions.RegExMatchResult;
import functionalj.list.FuncList;
import functionalj.stream.StreamPlus;

/**
 * --- Part Two ---
 * 
 * Watching over your shoulder as you work, one of The Historians asks if you took the effects of resonant harmonics 
 *   into your calculations.
 * 
 * Whoops!
 * 
 * After updating your model, it turns out that an antinode occurs at any grid position exactly in line with at least 
 *   two antennas of the same frequency, regardless of distance. This means that some of the new antinodes will occur 
 *   at the position of each antenna (unless that antenna is the only one of its frequency).
 * 
 * So, these three T-frequency antennas now create many antinodes:
 * 
 * T....#....
 * ...T......
 * .T....#...
 * .........#
 * ..#.......
 * ..........
 * ...#......
 * ..........
 * ....#.....
 * ..........
 * 
 * In fact, the three T-frequency antennas are all exactly in line with two antennas, so they are all also antinodes! 
 *   This brings the total number of antinodes in the above example to 9.
 * 
 * The original example now has 34 antinodes, including the antinodes that appear on every antenna:
 * 
 * ##....#....#
 * .#.#....0...
 * ..#.#0....#.
 * ..##...0....
 * ....0....#..
 * .#...#A....#
 * ...#..#.....
 * #....#.#....
 * ..#.....A...
 * ....#....A..
 * .#........#.
 * ...#......##
 * 
 * Calculate the impact of the signal using this updated model. How many unique locations within the bounds of the map 
 *   contain an antinode?
 * 
 * Your puzzle answer was 1229.
 * 
 */
public class Day8Part2Test extends BaseTest {
    
    record Position(int row, int col) {
        boolean isOutOfBound(int rowCount, int colCount) {
            return (row < 0 || row >= rowCount)
                || (col < 0 || col >= colCount);
        }
    }
    
    record Antenna(Position position, char symbol) {}
    
    int countAllNodes(FuncList<String> lines) {
        var rowCount = lines.size();
        var colCount = lines.get(0).length();
        
        var antennas 
                = lines
                .mapWithIndex(this::extractAntennas)
                .flatMap     (StreamPlus::toFuncList)
                .cache       ();
        
        return antennas
                .groupingBy(Antenna::symbol)
                .values    ()
                .map       (values -> values.map(Antenna.class::cast))
                .flatMap   (entry  -> totalAntinodes(entry, rowCount, colCount))
                .appendAll (antennas.map(Antenna::position).toSet())
                .distinct  ()
                .size      ();
    }
    
    StreamPlus<Antenna> extractAntennas(int row, String line) {
        return matches(line, regex("[^\\.]")).map(extractAntenna(row));
    }
    
    Func1<RegExMatchResult, Antenna> extractAntenna(int row) {
        return result -> {
            var col      = result.start();
            var position = new Position(row, col);
            var symbol   = result.group().charAt(0);
            return new Antenna(position, symbol);
        };
    }
    
    FuncList<Position> totalAntinodes(FuncList<Antenna> antennas, int rowCount, int colCount) {
        return antennas.flatMap(first -> {
            return antennas
                    .filter (second -> !first.equals(second))
                    .flatMap(second -> {
                            return infinite()
                                    .mapToObj   (createAntinode(first, second))
                                    .acceptUntil(position -> position.isOutOfBound(rowCount, colCount))
                                    .toFuncList();
                        }
                    );
        });
    }
    
    IntFunction<Position> createAntinode(Antenna first, Antenna second) {
        return i -> new Position(
                (i + 1)*second.position.row - i*first.position.row,
                (i + 1)*second.position.col - i*first.position.col);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = countAllNodes(lines);
        println("result: " + result);
        assertAsString("34", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = countAllNodes(lines);
        println("result: " + result);
        assertAsString("1229", result);
    }
    
}
