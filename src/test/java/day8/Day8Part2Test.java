package day8;

import static functionalj.functions.StrFuncs.*;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import day8.Day8Part1Test.Antenna;
import day8.Day8Part1Test.Position;
import functionalj.list.FuncList;

public class Day8Part2Test extends BaseTest {
    
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
                .flatMap(entry -> totalAntinodes(entry, rowCount, colCount))
                .exclude(p -> p.row < 0 || p.row >= rowCount)
                .exclude(p -> p.col < 0 || p.col >= colCount)
                .appendAll(amtennasPositions)
                .distinct()
                .cache();
        
        return antinodes.size();
    }

    private FuncList<Position> totalAntinodes(FuncList<? super Antenna> antennas, int rowCount, int colCount) {
        return antennas.flatMap(first  -> 
               antennas.flatMap(second -> {
                    if (first.equals(second))
                        return FuncList.<Position>empty();
                    
                    var diffRow = ((Antenna)second).position.row - ((Antenna)first).position.row;
                    var diffCol = ((Antenna)second).position.col - ((Antenna)first).position.col;
                    return FuncList.of(
                            createAntinodes(((Antenna)second).position, diffRow, diffCol,  1, rowCount, colCount),
                            createAntinodes(((Antenna)first).position,  diffRow, diffCol, -1, rowCount, colCount))
                            .flatMap(itself());
               }));
    }
    
    private FuncList<Position> createAntinodes(Position position, int diffRow, int diffCol, int sign, int rowCount, int colCount) {
        var builder = FuncList.<Position>newBuilder();
        int posRow = position.row;
        int posCol = position.col;        
        while (true) {
            posRow = posRow + sign*diffRow;
            posCol = posCol + sign*diffCol;
            builder.add(new Position(posRow, posCol));
            
            if ((posRow < 0) || (posRow >= rowCount))
                break;
            if ((posCol < 0) || (posCol >= colCount))
                break;
        }
        return builder.build();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("34", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("1229", result);
    }
    
}
