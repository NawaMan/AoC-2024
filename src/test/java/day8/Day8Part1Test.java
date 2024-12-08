package day8;

import static functionalj.functions.StrFuncs.matches;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day8Part1Test extends BaseTest {
    
    record Position(int row, int col) {}
    record Antenna(Position position, char symbol) {}
    
    Object calulate(FuncList<String> lines) {
        var rowCount = lines.size();
        var colCount = lines.get(0).length();
        
        var antennaPattern = regex("[^\\.]");
        var antennas = lines
                .mapWithIndex((row, line) -> {
                    return matches(line, antennaPattern)
                            .map(result -> new Antenna(new Position(row, result.start()), result.group().charAt(0)))
                            .toFuncList();
                })
                .exclude(FuncList::isEmpty)
                .flatMap(itself())
                .cache();
        
        var amtennasPositions
            = antennas
            .map(Antenna::position)
            .toSet();
        
        var antinodes = antennas
                .groupingBy(Antenna::symbol)
                .values()
                .flatMap(entry -> totalAntinodes(entry))
                .exclude(p -> p.row < 0 || p.row >= rowCount)
                .exclude(p -> p.col < 0 || p.col >= colCount)
                .excludeIn(amtennasPositions)
                .cache();
        
        return antinodes.size();
    }

    private FuncList<Position> totalAntinodes(FuncList<? super Antenna> antennas) {
        return antennas.flatMap(first  -> 
               antennas.flatMap(second -> {
                    if (first.equals(second))
                        return FuncList.<Position>empty();
                    
                    var diffRow = ((Antenna)second).position.row - ((Antenna)first).position.row;
                    var diffCol = ((Antenna)second).position.col - ((Antenna)first).position.col;
                    
                    var antinode1Row   = ((Antenna)second).position.row + diffRow;
                    var antinode1Col   = ((Antenna)second).position.col + diffCol;
                    
                    var antinode2Row   = ((Antenna)first).position.row - diffRow;
                    var antinode2Col   = ((Antenna)first).position.col - diffCol;
                    return FuncList.of(new Position(antinode1Row, antinode1Col), new Position(antinode2Row, antinode2Col));
               }))
               .distinct();
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
