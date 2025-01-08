package day8;

import static common.AocCommon.TwoLists.nestLoopList2;
import static day8.Antenna.theAntenna;
import static functionalj.stream.intstream.IntStreamPlus.range;

import org.junit.Test;

import functionalj.list.FuncList;

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
public class Day8Part2Test extends Day8Part1Test {
    
    static int MAX = 10000;
    
    int countAntinodes(FuncList<String> lines) {
        var rowCount = lines.size();
        var colCount = lines.get(0).length();
        
        var antennas 
                = lines
                .flatMapWithIndex(this::extractAntennas)
                .toImmutableList ();
        
        return antennas
                .groupingBy(theAntenna.symbol)
                .values    ()
                .map       (values -> values.map(Antenna.class::cast))
                .flatMap   (entry  -> createAntinodes(entry, rowCount, colCount))
                .distinct  ()
                .size      ();
    }
    
    FuncList<Position> createAntinodes(FuncList<Antenna> antennas, int rowCount, int colCount) {
        return nestLoopList2(antennas)
                .filter ((first, second) -> !first.equals(second))
                .flatMap((first, second) -> {
                    return range(1, MAX)
                            .mapToObj   (step     -> createAntinode(first, second, step))
                            .acceptUntil(position -> position.isOutOfBound(rowCount, colCount));
                 })
                .toFuncList();
    }
    
    Position createAntinode(Antenna first, Antenna second, int step) {
        return new Position(
                (step + 1)*second.position().row() - step*first.position().row(),
                (step + 1)*second.position().col() - step*first.position().col());
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = countAntinodes(lines);
        println("result: " + result);
        assertAsString("34", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = countAntinodes(lines);
        println("result: " + result);
        assertAsString("1229", result);
    }
    
}
