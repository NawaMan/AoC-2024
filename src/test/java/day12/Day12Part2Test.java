package day12;

import static day12.Day12Part2Test.EdgeDirection.Horizontal;
import static day12.Day12Part2Test.EdgeDirection.Vertical;
import static java.util.Comparator.comparingInt;

import java.util.Comparator;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

public class Day12Part2Test extends BaseTest {
    
    record Position(int row, int col) implements Comparable<Position> {
        @Override
        public int compareTo(Position o) {
            return comparingInt((Position p) -> p.row()).thenComparingInt((Position p) -> p.col()).compare(this, o);
        }
    }
    
    record Grid(FuncList<String> lines) {
        int height() { return lines.size(); }
        int width()  { return lines.get(0).length(); }
        char charAt(Position position) {
            return (char)at(position);
        }
        int at(Position position) {
            return at(position.row, position.col);
        }
        char charAt(int row, int col) {
            return (char)at(row, col);
        }
        int at(int row, int col) {
            if (row < 0 || row >= lines.size())            return ' ';
            if (col < 0 || col >= lines.get(row).length()) return ' ';
            return lines.get(row).charAt(col);
        }
    }
    
    enum EdgeDirection {
        Vertical, Horizontal
    }
    
    record Alignment(EdgeDirection direction, int rowOrCol) {
        OptionalInt location(Edge edge) {
            if (!this.equals(edge.alignment()))
                return OptionalInt.empty();
            return (direction == Vertical)
                    ? OptionalInt.of(edge.pos1.row)
                    : OptionalInt.of(edge.pos1.col);
        }
    }
    
    record Edge(Position pos1, Position pos2) implements Comparable<Edge> {
        Position first() {
            return pos1.compareTo(pos2) > 0 ? pos1 : pos2;
        }
        Position second() {
            return pos1.compareTo(pos2) > 0 ? pos2 : pos1;
        }
        EdgeDirection direction() {
            return (pos1.row == pos2.row) ? Vertical : Horizontal;
        }
        Alignment alignment() {
            var direction = direction();
            var rowOrCol
                    = (direction == Vertical)
                    ? pos1.col
                    : pos1.row;
            return new Alignment(direction, rowOrCol);
        }
        boolean isNext(Edge o) {
            return ((pos1.row == o.pos1.row) && (Math.abs(pos1.col - o.pos1.col) == 1))
                != ((pos1.col == o.pos1.col) && (Math.abs(pos1.row - o.pos1.row) == 1));
        }
        @Override
        public int compareTo(Edge o) {
            return Comparator.comparing(Edge::first).thenComparing(Edge::second).compare(this, o);
        }
    }
    
    record Group(Grid grid, TreeSet<Position> positions) implements Comparable<Group> {
        Position first() {
            return positions.stream().findFirst().get();
        }
        int area() {
            return positions.size();
        }
        int perimeter() {
            var total = 0;
            for (var position : positions) {
                total += perimeterAt(position);
            }
            return total;
        }
        int sides() {
            var ch = grid.charAt(positions.first());
            
            var allEdges = new TreeSet<Edge>();
            for (var position : positions) {
                gatherEdges(position, allEdges);
            }
            var edges = FuncList.from(allEdges);
            var byAlignments = edges.groupingBy(e -> e.alignment()).mapValue(v -> (FuncList<Edge>)v).toImmutableMap();
            return byAlignments
            .entries()
//            .mapToInt(entry -> {
            .flatMapToInt(entry -> {
                var alignment = entry.getKey();
                var theEdges  = entry.getValue()
                        .map(Edge.class::cast)
                        .groupingBy(e -> "(%s,%s)".formatted(
                                        grid.charAt(e.pos1) == ch ? ch : ' ', 
                                        grid.charAt(e.pos2) == ch ? ch : ' '))
                        .mapValue(es -> (FuncList<Edge>)es)
                        ;
                return theEdges
                        .values()
                        .mapToInt(es -> {
                    System.out.println(alignment);
                    es
//                    theEdges
                    .forEach(e -> System.out.println("  " + e + "(%s,%s)".formatted(
                            grid.charAt(e.pos1) == ch ? ch : ' ', 
                            grid.charAt(e.pos2) == ch ? ch : ' ')));
                    
                    var locations 
//                        = theEdges
                        = es
                        .map(e -> alignment.location(e))
                        .filter(OptionalInt::isPresent)
                        .mapToInt(OptionalInt::getAsInt)
                        .sorted();
                    
                    var diffs = (locations.size() == 1) ? IntFuncList.of(1) : locations.mapTwo((a, b) -> b - a);                
                    return diffs.filter(diff -> diff != 1).size() + 1;
                });
            })
            .sum();
        }
        private int perimeterAt(Position position) {
            return (isPerimeter(new Position(position.row() + 1, position.col()    )) ? 1 : 0)
                 + (isPerimeter(new Position(position.row() - 1, position.col()    )) ? 1 : 0)
                 + (isPerimeter(new Position(position.row()    , position.col() + 1)) ? 1 : 0)
                 + (isPerimeter(new Position(position.row()    , position.col() - 1)) ? 1 : 0);
        }
        private void gatherEdges(Position position, Set<Edge> edges) {
            gatherEdge(position, new Position(position.row() + 1, position.col()    ),  1, edges);
            gatherEdge(position, new Position(position.row() - 1, position.col()    ), -1, edges);
            gatherEdge(position, new Position(position.row()    , position.col() + 1),  1, edges);
            gatherEdge(position, new Position(position.row()    , position.col() - 1), -1, edges);
        }
        void gatherEdge(Position position, Position anotherPosition, int sign, Set<Edge> edges) {
            if (isPerimeter(anotherPosition)) {
//                var edge = FuncList.of(position, anotherPosition).sorted().pipe(ps -> new Edge(ps.get(0), ps.get(1)));
                if (sign == 1)
                     edges.add(new Edge(position, anotherPosition));
                else edges.add(new Edge(anotherPosition, position));
            }
        }
        private boolean isPerimeter(Position another) {
            return !positions.contains(another);
        }
        @Override
        public int compareTo(Group o) {
            return Comparator.comparing(Group::first).compare(this, o);
        }
    }
    record State(int area, int perimeter) {}
    
    Set<Position> visiteds = new TreeSet<>();
    Set<Group>    groups   = new TreeSet<>();
    
    
    Object calulate(FuncList<String> lines) {
        lines.forEach(this::println);
        println();
        
        var grid = new Grid(lines);
        for (int r = 0; r < grid.height(); r++) {
            for (int c = 0; c < grid.width(); c++) {
                walk(grid, new Position(r, c));
            }
        }
        
        var totalCost = 0;
        for (var group : groups) {
            println("==" + group.first() + " : " + (char)grid.at(group.first()) + " ==");
            int area  = group.area();
            int sides = group.sides();
            println("area: " + area + ", side: " + sides);
            var cost = area*sides;
//            FuncList.from(group.positions).forEach(println);
            println();
            totalCost += cost;
        }
        
        return totalCost;
    }
    
    void walk(Grid grid, Position position) {
        if (visiteds.contains(position))
            return;
        
        var group = new TreeSet<Position>();
        var ch    = grid.at(position);
        walk(grid, ch, position, group);
        
        groups.add(new Group(grid, group));
    }
    
    void walk(Grid grid, int expected, Position position, Set<Position> group) {
        if (visiteds.contains(position))
            return;
        
        var ch = grid.at(position);
        if (expected != ch)
            return;

        visiteds.add(position);
        group.add(position);
        
        walk(grid, ch, new Position(position.row + 1, position.col    ), group);
        walk(grid, ch, new Position(position.row - 1, position.col    ), group);
        walk(grid, ch, new Position(position.row    , position.col + 1), group);
        walk(grid, ch, new Position(position.row    , position.col - 1), group);
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
