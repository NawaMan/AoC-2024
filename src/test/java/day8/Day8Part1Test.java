package day8;

import static functionalj.functions.StrFuncs.matches;

import org.junit.Test;

import common.BaseTest;
import functionalj.functions.RegExMatchResult;
import functionalj.list.FuncList;
import functionalj.stream.StreamPlus;

public class Day8Part1Test extends BaseTest {
    
    record Position(int row, int col) {
        boolean isOutOfBound(int rowCount, int colCount) {
            return (row < 0 || row >= rowCount)
                || (col < 0 || col >= colCount);
        }
    }
    record Antenna(Position position, char symbol) {}
    
    Object calulate(FuncList<String> lines) {
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
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("14", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("371", result);
    }
    
}
