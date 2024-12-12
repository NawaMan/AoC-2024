package day12;

import static java.util.Comparator.comparingInt;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day12Part1Test extends BaseTest {
    
    record Position(int row, int col) implements Comparable<Position> {
        @Override
        public int compareTo(Position o) {
            return comparingInt((Position p) -> p.row()).thenComparingInt((Position p) -> p.col()).compare(this, o);
        }
    }
    
    record Grid(FuncList<String> lines) {
        int height() { return lines.size(); }
        int width()  { return lines.get(0).length(); }
        int at(Position position) {
            return at(position.row, position.col);
        }
        int at(int row, int col) {
            if (row < 0 || row >= lines.size())            return ' ';
            if (col < 0 || col >= lines.get(row).length()) return ' ';
            return lines.get(row).charAt(col);
        }
    }
    
    record Group(TreeSet<Position> positions) implements Comparable<Group> {
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
        private int perimeterAt(Position position) {
            return (isPerimeter(new Position(position.row() + 1, position.col() + 0)) ? 1 : 0)
                 + (isPerimeter(new Position(position.row() - 1, position.col() - 0)) ? 1 : 0)
                 + (isPerimeter(new Position(position.row() + 0, position.col() + 1)) ? 1 : 0)
                 + (isPerimeter(new Position(position.row() - 0, position.col() - 1)) ? 1 : 0);
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
            println("area: " + group.area() + ", perimeter: " + group.perimeter());
            var cost = group.area()*group.perimeter();
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
        
        groups.add(new Group(group));
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
