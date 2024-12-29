package day8;

import static functionalj.functions.StrFuncs.matches;

import org.junit.Test;

import common.BaseTest;
import functionalj.functions.RegExMatchResult;
import functionalj.list.FuncList;
import functionalj.stream.StreamPlus;

/**
 * --- Day 8: Resonant Collinearity ---
 * 
 * You find yourselves on the roof of a top-secret Easter Bunny installation.
 * 
 * While The Historians do their thing, you take a look at the familiar huge antenna. Much to your surprise, it seems to
 *   have been reconfigured to emit a signal that makes people 0.1% more likely to buy Easter Bunny brand Imitation 
 *   Mediocre Chocolate as a Christmas gift! Unthinkable!
 * 
 * Scanning across the city, you find that there are actually many such antennas. Each antenna is tuned to a specific 
 *   frequency indicated by a single lowercase letter, uppercase letter, or digit. You create a map (your puzzle input)
 *   of these antennas. For example:
 * 
 * ............
 * ........0...
 * .....0......
 * .......0....
 * ....0.......
 * ......A.....
 * ............
 * ............
 * ........A...
 * .........A..
 * ............
 * ............
 * 
 * The signal only applies its nefarious effect at specific antinodes based on the resonant frequencies of the antennas.
 *   In particular, an antinode occurs at any point that is perfectly in line with two antennas of the same frequency - 
 *   but only when one of the antennas is twice as far away as the other. This means that for any pair of antennas with 
 *   the same frequency, there are two antinodes, one on either side of them.
 * 
 * So, for these two antennas with frequency a, they create the two antinodes marked with #:
 * 
 * ..........
 * ...#......
 * ..........
 * ....a.....
 * ..........
 * .....a....
 * ..........
 * ......#...
 * ..........
 * ..........
 * 
 * Adding a third antenna with the same frequency creates several more antinodes. It would ideally add four antinodes, 
 *   but two are off the right side of the map, so instead it adds only two:
 * 
 * ..........
 * ...#......
 * #.........
 * ....a.....
 * ........a.
 * .....a....
 * ..#.......
 * ......#...
 * ..........
 * ..........
 * 
 * Antennas with different frequencies don't create antinodes; A and a count as different frequencies. However, 
 *   antinodes can occur at locations that contain antennas. In this diagram, the lone antenna with frequency capital 
 *   A creates no antinodes but has a lowercase-a-frequency antinode at its location:
 * 
 * ..........
 * ...#......
 * #.........
 * ....a.....
 * ........a.
 * .....a....
 * ..#.......
 * ......A...
 * ..........
 * ..........
 * 
 * The first example has antennas with two different frequencies, so the antinodes they create look like this, plus an 
 *   antinode overlapping the topmost A-frequency antenna:
 * 
 * ......#....#
 * ...#....0...
 * ....#0....#.
 * ..#....0....
 * ....0....#..
 * .#....A.....
 * ...#........
 * #......#....
 * ........A...
 * .........A..
 * ..........#.
 * ..........#.
 * 
 * Because the topmost A-frequency antenna overlaps with a 0-frequency antinode, there are 14 total unique locations 
 *   that contain an antinode within the bounds of the map.
 * 
 * Calculate the impact of the signal. How many unique locations within the bounds of the map contain an antinode?
 * 
 * Your puzzle answer was 371.
 */
public class Day8Part1Test extends BaseTest {
    
    record Position(int row, int col) {
        boolean isOutOfBound(int rowCount, int colCount) {
            return (row < 0 || row >= rowCount)
                || (col < 0 || col >= colCount);
        }
    }
    record Antenna(Position position, char symbol) {}
    
    int countAntinodes(FuncList<String> lines) {
        var rowCount = lines.size();
        var colCount = lines.get(0).length();
        
        var antennas 
                = lines
                .mapWithIndex((row, line) -> matches(line, regex("[^\\.]")).map(result -> extractAntenna(row, result)))
                .flatMap     (StreamPlus::toFuncList)
                .cache       ();
        
        return antennas
                .groupingBy(Antenna::symbol)
                .values    ()
                .map       (values   -> values.map(Antenna.class::cast))
                .flatMap   (entry    -> totalAntinodes(entry))
                .exclude   (position -> position.isOutOfBound(rowCount, colCount))
                .excludeIn (antennas.map(Antenna::position).toSet())
                .size      ();
    }
    
    Antenna extractAntenna(int row, RegExMatchResult result) {
        return new Antenna(new Position(row, result.start()), result.group().charAt(0));
    }
    
    FuncList<Position> totalAntinodes(FuncList<Antenna> antennas) {
        return antennas.flatMap(first -> 
               antennas
               .filter(second -> !first.equals(second))
               .map   (second -> new Position(
                                       2*second.position.row - first.position.row,
                                       2*second.position.col - first.position.col)));
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = countAntinodes(lines);
        println("result: " + result);
        assertAsString("14", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = countAntinodes(lines);
        println("result: " + result);
        assertAsString("371", result);
    }
    
}
