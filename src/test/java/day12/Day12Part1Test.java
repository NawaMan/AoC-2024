package day12;

import static functionalj.stream.intstream.IntStreamPlus.range;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.stream.StreamPlus;

public class Day12Part1Test extends BaseTest {
    
    record Position(int row, int col) implements Comparable<Position> {
        FuncList<Position> neighbours() {
            return FuncList.of(
                        new Position(row + 1, col()    ),
                        new Position(row - 1, col()    ),
                        new Position(row    , col() + 1),
                        new Position(row    , col() - 1));
        }
        @Override
        public int compareTo(Position o) {
            return comparingInt(Position::row)
                    .thenComparingInt(Position::col)
                    .compare(this, o);
        }
    }
    
    record Grid(FuncList<String> lines) {
        char charAt(Position position) {
            if (position.row < 0 || position.row >= lines.size())                     return ' ';
            if (position.col < 0 || position.col >= lines.get(position.row).length()) return ' ';
            return lines.get(position.row).charAt(position.col);
        }
        StreamPlus<Position> positions() {
            return range(0, lines.size()).flatMapToObj(row -> {
                return range(0, lines.get(row).length()).mapToObj(col -> {
                    return new Position(row, col);
                });
            });
        }

        FuncList<Group> groups() {
            var visiteds = new TreeSet<Position>();
            var groups   = new TreeSet<Group>();
            positions()
            .exclude(position -> visiteds.contains(position))
            .forEach(position -> walk(position, visiteds, groups));
            return FuncList.from(groups);
        }

        private void walk(Position position, Set<Position> visiteds, Set<Group> groups) {
            var forChar = charAt(position);
            var group   = walk(forChar, position, visiteds, groups).toFuncList();
            groups.add(new Group(Grid.this, group));
        }
        
        @SuppressWarnings("unchecked")
        private StreamPlus<Position> walk(char forChar, Position position, Set<Position> visiteds, Set<Group> groups) {
            if (visiteds.contains(position) || (forChar != charAt(position)))
                return StreamPlus.empty();

            visiteds.add(position);
            
            return position
                    .neighbours()
                    .map       (neighbour -> walk(forChar, neighbour, visiteds, groups))
                    .streamPlus()
                    .flatMap   (StreamPlus.class::cast)
                    .appendWith(StreamPlus.of(position));
        }
    }
    
    record Group(Grid grid, FuncList<Position> positions) implements Comparable<Group> {
        Position first() {
            return positions.stream().findFirst().get();
        }
        int fencePrice() {
            var area      = positions.size();
            var perimeter = positions.sumToInt(this::perimetersAt);
            return area*perimeter;
        }
        private int perimetersAt(Position position) {
            return position
                    .neighbours()
                    .filter(this::isPerimeter)
                    .size();
        }
        boolean isPerimeter(Position another) {
            return !positions.contains(another);
        }
        @Override
        public int compareTo(Group o) {
            return comparing(Group::first)
                    .compare(this, o);
        }
    }
    
    Object calulate(FuncList<String> lines) {
        return new Grid(lines)
                .groups()
                .sumToInt(Group::fencePrice);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("1930", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("1400386", result);
    }
    
}
