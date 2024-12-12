package day12;

import static day12.Day12Part2Test.EdgeDirection.Horizontal;
import static day12.Day12Part2Test.EdgeDirection.Vertical;
import static functionalj.stream.intstream.IntStreamPlus.range;
import static java.util.Comparator.comparingInt;

import java.util.Comparator;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import common.BaseTest;
import functionalj.function.Func1;
import functionalj.list.FuncList;
import functionalj.stream.StreamPlus;

public class Day12Part2Test extends BaseTest {
    
    record Position(int row, int col) implements Comparable<Position> {
        FuncList<Position> neighbours() {
            return FuncList.of(
                        new Position(row + 1, col()    ),
                        new Position(row - 1, col()    ),
                        new Position(row    , col() + 1),
                        new Position(row    , col() - 1));
        }
        Edge edgeWith(Position position) {
            var sign = (position.row - this.row)
                     + (position.col - this.col);
             return (sign == 1)
                     ? new Edge(this, position)
                     : new Edge(position, this);
        }
        @Override
        public int compareTo(Position o) {
            return comparingInt((Position p) -> p.row()).thenComparingInt((Position p) -> p.col()).compare(this, o);
        }
    }
    
    record Grid(FuncList<String> lines) {
        int height() { return lines.size(); }
        int width()  { return lines.get(0).length(); }
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
            positions().forEach(position -> walk(position, visiteds, groups));
            return FuncList.from(groups);
        }

        private void walk(Position position, Set<Position> visiteds, Set<Group> groups) {
            if (visiteds.contains(position))
                return;
            
            var forChar = charAt(position);
            var group   = walk(forChar, position, visiteds, groups)
                            .sorted().distinct().toFuncList();
            groups.add(new Group(Grid.this, group));
        }
        
        @SuppressWarnings("unchecked")
        private StreamPlus<Position> walk(char forChar, Position position, Set<Position> visiteds, Set<Group> groups) {
            if (visiteds.contains(position) || (forChar != charAt(position)))
                return StreamPlus.empty();

            visiteds.add(position);
            
            return position
                    .neighbours()
                    .map(neighbour -> walk(forChar, neighbour, visiteds, groups))
                    .streamPlus().flatMap(StreamPlus.class::cast)
                    .appendWith(StreamPlus.of(position));
        }
    }
    
    enum EdgeDirection { Vertical, Horizontal }
    
    record Alignment(EdgeDirection direction, int rowOrCol) {
        OptionalInt location(Edge edge) {
            return this.equals(edge.alignment())
                    ? OptionalInt.of((direction == Vertical) ? edge.pos1.row : edge.pos1.col)
                    : OptionalInt.empty();
        }
    }
    
    record Edge(Position pos1, Position pos2) implements Comparable<Edge> {
        Alignment alignment() {
            var direction = (pos1.row  == pos2.row) ? Vertical : Horizontal;
            var rowOrCol  = (direction == Vertical) ? pos1.col : pos1.row;
            return new Alignment(direction, rowOrCol);
        }
        String identityFor(Grid grid, char ch) {
            return "(%s,%s)".formatted((grid.charAt(pos1) == ch ? ch : ' '), (grid.charAt(pos2) == ch ? ch : ' '));
        }
        @Override
        public int compareTo(Edge o) {
            return Comparator.comparing(Edge::pos1).thenComparing(Edge::pos2).compare(this, o);
        }
    }
    
    record Group(Grid grid, FuncList<Position> positions) implements Comparable<Group> {
        Position first() {
            return positions.stream().findFirst().get();
        }
        int area() {
            return positions.size();
        }
        @SuppressWarnings("unchecked")
        int sides() {
            var ch = grid.charAt(positions.getFirst());
            return FuncList.from(positions)
                    .flatMap(this::findEdges)
                    .groupingBy(Edge::alignment).entries()
                    .flatMapToInt(entry -> {
                        var alignment = entry.getKey();
                        return entry.getValue().map(Edge.class::cast)
                            .groupingBy(sideWith(ch))
                            .values().mapToInt(es -> {
                                var diffs = ((FuncList<Edge>)es)
                                    .mapToInt(e -> alignment.location(e).getAsInt())
                                    .mapTwo((a, b) -> b - a);                
                                return diffs.filter(diff -> diff != 1).size() + 1;
                            });
                    })
                    .sum();
        }
        FuncList<Edge> findEdges(Position position) {
            return position.neighbours().filter(this::isPerimeter).map(position::edgeWith);
        }
        boolean isPerimeter(Position another) {
            return !positions.contains(another);
        }
        Func1<Edge, String> sideWith(char ch) {
            return edge -> edge.identityFor(grid, ch);
        }
        @Override
        public int compareTo(Group o) {
            return Comparator.comparing(Group::first).compare(this, o);
        }
    }
    
    Object calulate(FuncList<String> lines) {
        var grid = new Grid(lines);
        return grid.groups().mapToInt(group -> group.area()*group.sides()).sum();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("1206", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("851994", result);
    }
    
}
