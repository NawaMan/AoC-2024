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

public class Day8Part2Test extends BaseTest {
    
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
                .mapWithIndex((row, line) -> matches(line, regex("[^\\.]")).map(extractAntenna(row)))
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

    Func1<RegExMatchResult, Antenna> extractAntenna(int row) {
        return result -> {
            return new Antenna(new Position(row, result.start()), result.group().charAt(0));
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
