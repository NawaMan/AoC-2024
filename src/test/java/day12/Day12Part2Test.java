package day12;

import static day12.Day12Part2Test.EdgeDirection.Horizontal;
import static day12.Day12Part2Test.EdgeDirection.Vertical;
import static functionalj.stream.intstream.IntStreamPlus.range;
import static java.util.Comparator.comparingInt;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.stream.StreamPlus;

/**
 * --- Part Two ---
 * 
 * Fortunately, the Elves are trying to order so much fence that they qualify for a bulk discount!
 * 
 * Under the bulk discount, instead of using the perimeter to calculate the price, you need to use the number of sides 
 *   each region has. Each straight section of fence counts as a side, regardless of how long it is.
 * 
 * Consider this example again:
 * 
 * AAAA
 * BBCD
 * BBCC
 * EEEC
 * 
 * The region containing type A plants has 4 sides, as does each of the regions containing plants of type B, D, and E. 
 *   However, the more complex region containing the plants of type C has 8 sides!
 * 
 * Using the new method of calculating the per-region price by multiplying the region's area by its number of sides, 
 *   regions A through E have prices 16, 16, 32, 4, and 12, respectively, for a total price of 80.
 * 
 * The second example above (full of type X and O plants) would have a total price of 436.
 * 
 * Here's a map that includes an E-shaped region full of type E plants:
 * 
 * EEEEE
 * EXXXX
 * EEEEE
 * EXXXX
 * EEEEE
 * 
 * The E-shaped region has an area of 17 and 12 sides for a price of 204. Including the two regions full of type X plants,
 *   this map has a total price of 236.
 * 
 * This map has a total price of 368:
 * 
 * AAAAAA
 * AAABBA
 * AAABBA
 * ABBAAA
 * ABBAAA
 * AAAAAA
 * 
 * It includes two regions full of type B plants (each with 4 sides) and a single region full of type A plants (with 
 *   4 sides on the outside and 8 more sides on the inside, a total of 12 sides). Be especially careful when counting 
 *   the fence around regions like the one full of type A plants; in particular, each section of fence has an in-side 
 *   and an out-side, so the fence does not connect across the middle of the region (where the two B regions touch 
 *   diagonally). (The Elves would have used the Möbius Fencing Company instead, but their contract terms were too 
 *   one-sided.)
 * 
 * The larger example from before now has the following updated prices:
 * 
 *     A region of R plants with price 12 * 10 = 120.
 *     A region of I plants with price 4 * 4 = 16.
 *     A region of C plants with price 14 * 22 = 308.
 *     A region of F plants with price 10 * 12 = 120.
 *     A region of V plants with price 13 * 10 = 130.
 *     A region of J plants with price 11 * 12 = 132.
 *     A region of C plants with price 1 * 4 = 4.
 *     A region of E plants with price 13 * 8 = 104.
 *     A region of I plants with price 14 * 16 = 224.
 *     A region of M plants with price 5 * 6 = 30.
 *     A region of S plants with price 3 * 6 = 18.
 * 
 * Adding these together produces its new total price of 1206.
 * 
 * What is the new total price of fencing all regions on your map?
 * 
 * Your puzzle answer was 851994.
 */
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
            return comparingInt      (Position::row)
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
            positions().forEach(position -> walk(position, visiteds, groups));
            return FuncList.from(groups);
        }
        private void walk(Position position, Set<Position> visiteds, Set<Group> groups) {
            if (visiteds.contains(position))
                return;
            
            var forChar = charAt(position);
            var group   = walk(forChar, position, visiteds, groups).sorted().distinct().toFuncList();
            groups.add(new Group(Grid.this, group));
        }
        private StreamPlus<Position> walk(char forChar, Position position, Set<Position> visiteds, Set<Group> groups) {
            if (visiteds.contains(position) || (forChar != charAt(position)))
                return StreamPlus.empty();
            
            visiteds.add(position);
            return position
                    .neighbours()
                    .streamPlus()
                    .flatMap   (neighbour -> walk(forChar, neighbour, visiteds, groups))
                    .appendWith(StreamPlus.of(position));
        }
    }
    
    enum EdgeDirection { Vertical, Horizontal }
    
    record Alignment(EdgeDirection direction, int rowOrCol) {
        int location(Edge edge) {
            return (direction == Vertical) ? edge.pos1.row : edge.pos1.col;
        }
    }
    
    record Edge(Position pos1, Position pos2) {
        Alignment alignment() {
            var direction = (pos1.row  == pos2.row) ? Vertical : Horizontal;
            var rowOrCol  = (direction == Vertical) ? pos1.col : pos1.row;
            return new Alignment(direction, rowOrCol);
        }
        String identityFor(Grid grid, char ch) {
            // Identity is a string indicating which side the character ch is on.
            // For example, for the edge between (A,B), its identity for A will be (A, ) and for B will be ( ,B). 
            return "(%s,%s)".formatted(
                    (grid.charAt(pos1) == ch ? ch : ' '),
                    (grid.charAt(pos2) == ch ? ch : ' '));
        }
    }
    
    record SideKey(Alignment alignment, String identity) {}
    
    record Group(Grid grid, FuncList<Position> positions) implements Comparable<Group> {
        Position first() {
            return positions.stream().findFirst().get();
        }
        int fencePrice() {
            var area  = positions.size();
            var sides = sides();
            return area*sides;
        }
        int sides() {
            var groupChar = grid.charAt(positions.getFirst());
            return positions
                    .flatMap   (this::findEdges)
                    .groupingBy(edge -> new SideKey(edge.alignment(), edge.identityFor(grid, groupChar)))
                    .entries   ()
                    .sumToInt  (entry -> {
                        var alignment = entry.getKey  ().alignment();
                        var edges     = entry.getValue();
                        return continousSidesOnSameAlignment(alignment, edges);
                    });
        }
        private FuncList<Edge> findEdges(Position position) {
            return position
                    .neighbours()
                    .filter(this::isPerimeter)
                    .map   (position::edgeWith);
        }
        private boolean isPerimeter(Position another) {
            return !positions.contains(another);
        }
        private int continousSidesOnSameAlignment(Alignment alignment, FuncList<? super Edge> edges) {
            // Edges that are on the same alignment but disconnected are considered a separated sides.
            // +--+  +--+  <-- These four edges are on the same alignment but not all connected.
            // |AA+--+AA|
            // |AAAAAAAA|
            //    ....
            var diffs = edges.mapToInt(e -> alignment.location((Edge)e)).mapTwo((a, b) -> b - a);                
            return 1 + diffs.filter(diff -> diff != 1).size();
        }
        @Override
        public int compareTo(Group o) {
            return Comparator.comparing(Group::first).compare(this, o);
        }
    }
    
    Object calculate(FuncList<String> lines) {
        var grid = new Grid(lines);
        return grid.groups().sumToInt(Group::fencePrice);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("1206", result);
    }
    
    @Ignore
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("851994", result);
    }
    
}
