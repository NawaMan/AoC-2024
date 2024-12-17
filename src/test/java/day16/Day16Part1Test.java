package day16;

import static functionalj.list.intlist.IntFuncList.range;
import static java.lang.Math.signum;
import static java.util.Comparator.comparing;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntPredicate;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.FuncListBuilder;
import functionalj.map.FuncMap;
import functionalj.tuple.Tuple2;

public class Day16Part1Test extends BaseTest {
    
    record Position(int row, int col) {
        FuncList<Position> neighbours() {
            return FuncList.of(
                        new Position(row + 1, col()    ),
                        new Position(row    , col() + 1),
                        new Position(row - 1, col()    ),
                        new Position(row    , col() - 1));
        }
    }
    
    record Dot(int row, int col, String neighbours) implements Comparable<Dot> {
        @Override
        public int compareTo(Dot o) {
            return comparing(Dot::row)
                    .thenComparing(Dot::col)
                    .compare(this, o);
        }
    }

    record Grid(FuncList<String> lines) {
        char charAt(Position position) {
            return charAt(position.row, position.col);
        }
        char charAt(int row, int col) {
            if (row < 0 || row >= lines.size())            return '#';
            if (col < 0 || col >= lines.get(row).length()) return '#';
            return lines.get(row).charAt(col);
        }
        FuncList<Dot> selectDots() {
            return range(0, lines.size()).toCache().flatMapToObj(row -> {
                return range(0, lines.get(0).length())
                        .toCache ()
                        .mapToObj(col -> new Position(row, col))
                        .filter  (pos -> charAt(pos) != '#')
                        .map     (pos -> new Dot(pos.row, pos.col, pos.neighbours().map(n -> charAt(n) != '#' ? '.' : ' ').join() + pos.neighbours().map(n -> charAt(n) != '#' ? '.' : ' ').join()))
                        ;
            });
        }
        FuncList<Position> select(IntPredicate charSelector) {
            return range(0, lines.size()).toCache().flatMapToObj(row -> {
                return range(0, lines.get(0).length())
                        .toCache ()
                        .filter  (col -> charSelector.test((int)charAt(row, col)))
                        .mapToObj(col -> new Position(row, col))
                        ;
            });
        }
    }
    
    record Edge(Dot start, Dot end, int distance) {
        Dot to(Dot from) {
            return from.equals(start) ? end   :
                   from.equals(end)   ? start : null; 
        }
        @Override
        public String toString() {
            return "Edge[(%d,%d)->(%d,%d) : %d]"
                    .formatted(start.row, start.col, end.row, end.col, distance);
        }
    }
    
    record Graph(Grid grid, Dot start, Dot end, FuncList<Dot> dots, FuncMap<Dot, FuncList<Edge>> graphMap) {
        Tuple2<Long, FuncList<Dot>> shortestPath() {
            var seen = new HashSet<Dot>();

            var current  = start;
            var previous = current;
            return shortestPath(previous, current, seen);
        }
        
        Tuple2<Long, FuncList<Dot>> shortestPath(Dot previous, Dot current, HashSet<Dot> seen) {
            if (current.equals(end)) {
                var path = FuncList.of(current);
                return Tuple2.of(0L, path);
            }
            
            seen.add(current);
            var choices = choicesFor(current);
            var queue   = new PriorityQueue<Tuple2<Edge, Long>>(comparing(Tuple2::_2));;
            for (var choice : choices) {
                var to = choice.to(current);
                if (seen.contains(to))
                    continue;
                
                var distance = distance(previous, current, choice);
                var pair     = Tuple2.of(choice, distance);
                queue.add(pair);
            }
            
            System.out.println();
            System.out.println("Previous: " + previous);
            System.out.println("Current:  " + current);
            System.out.println("Queue: ");
            queue.stream()
            .map(String::valueOf)
            .map("    "::concat)
            .forEach(System.out::println);
            System.out.println();
            
            long minDist = Long.MAX_VALUE;
            var  minPath = (FuncList<Dot>)null;
            for (var pair : queue) {
                var edge      = pair._1();
                var distance  = pair._2();
                var to        = edge.to(current);
                var shrstPath = shortestPath(current, to, seen);
                if ((shrstPath != null) && (distance + shrstPath._1() < minDist)) {
                    minDist = shrstPath._1() + distance;
                    minPath = shrstPath._2();
                }
            }
            if (minPath == null) {
                return null;
            }
            
            return Tuple2.of(minDist, minPath.append(current));
        }

        private FuncList<Edge> choicesFor(Dot current) {
            var choice = graphMap.get(current);
            if (choice != null)
                return choice;
            
            // TODO - Add choice for when the current is not a branch.
            return FuncList.empty();
        }
        long distance(Dot previous, Dot current, Edge edge) {
            if (previous == current)
                return edge.distance + 1;
            
            var diffRow1 = signum(current.row - previous.row);
            var diffCol1 = signum(current.col - previous.col);
            
            var to = edge.to(current);
            var diffRow2 = signum(to.row - current.row);
            var diffCol2 = signum(to.col - current.col);
            
            var notTurn = (diffRow1 == diffRow2) || (diffCol1 == diffCol2);
            var turn = !notTurn;
            var distance = (turn ? 1000L : 0L) + edge.distance + 1;
            
            System.out.println("previous: %s, \ncurrent : %s, \nto      : %s, \nturn    : %s, \ndistance: %d".formatted(previous, current, to, turn, distance));
            
            return distance;
        }
    }
    
    Object calulate(FuncList<String> lines) {
        lines.forEach(this::println);
        println();
        
        var graph = createGraph(lines);
        
        println("Graph: ");
        new TreeMap<>(graph.graphMap).entrySet().forEach(println);
        println(graph.graphMap.size());
        println();
        
        println("Path: ");
        var path = graph.shortestPath();
        println(path._1());
        path._2().forEach(println);
        
        return path._1() + 1000;
    }

    Graph createGraph(FuncList<String> lines) {
        var grid  = new Grid(lines);
        var start = grid.select(i -> i == (int)'S').findFirst().get();
        var end   = grid.select(i -> i == (int)'E').findFirst().get();
        
        var graphMap = new ConcurrentHashMap<Dot, FuncListBuilder<Edge>>();
        var dots     = grid.selectDots().cache();
        var edges    = dots.filter(node -> !node.neighbours.contains("..") && node.neighbours.contains(". .")).cache();
        var banches  = dots.excludeIn(edges).cache();
        var sameRows = banches.groupingBy(node -> node.row);
        var sameCols = banches.groupingBy(node -> node.col);
        
        println("Banches: ");
        banches.map(String::valueOf).map("  "::concat).forEach(println);
        println();
        
        sameRows.forEach((row, list) -> {
            list.mapTwo().map(pair -> {
                var s = (Dot)pair._1();
                var e = (Dot)pair._2();
                var connections = edges.filter(edge -> (edge.row == s.row) && (edge.col >= s.col) && (edge.col <= e.col));
                var connCount   = connections.size();
                var distance    = e.col - s.col;
                
                var edge = ((connCount == 0) || (connCount < (distance - 1)))
                         ? (Edge)null
                         : new Edge(s, e, connCount);
                return edge;
            })
            .excludeNull()
            .forEach(edge -> {
                graphMap.computeIfAbsent(edge.start, __ -> new FuncListBuilder<Edge>());
                graphMap.computeIfAbsent(edge.end,   __ -> new FuncListBuilder<Edge>());
                graphMap.get(edge.start).add(edge);
                graphMap.get(edge.end).add(edge);
            });
        });
        
        sameCols.forEach((col, list) -> {
            list.mapTwo().map(pair -> {
                var s = (Dot)pair._1();
                var e = (Dot)pair._2();
                var connections = edges.filter(edge -> (edge.col == s.col) && (edge.row >= s.row) && (edge.row <= e.row));
                var connCount   = connections.size();
                var distance    = e.row - s.row;
                
                var edge = ((connCount == 0) || (connCount < (distance - 1)))
                         ? (Edge)null
                         : new Edge(s, e, connCount);
                return edge;
            })
            .excludeNull()
            .forEach(edge -> {
                graphMap.computeIfAbsent(edge.start, __ -> new FuncListBuilder<Edge>());
                graphMap.computeIfAbsent(edge.end,   __ -> new FuncListBuilder<Edge>());
                graphMap.get(edge.start).add(edge);
                graphMap.get(edge.end).add(edge);
            });
        });

        var startDot = dots.findFirst(dot -> (start.row == dot.row) && (start.col == dot.col)).get();
        var endDot   = dots.findFirst(dot -> (end.row   == dot.row) && (end.col   == dot.col)).get();
        var map      = FuncMap.from(graphMap).mapValue(FuncListBuilder::build);
        return new Graph(grid, startDot, endDot, dots, map);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("7036", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("438512", result);
    }
    
}
