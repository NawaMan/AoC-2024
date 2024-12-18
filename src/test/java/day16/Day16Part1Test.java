package day16;

import static functionalj.list.intlist.IntFuncList.range;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Math.signum;
import static java.util.Comparator.comparing;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
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
    
    record Graph(Grid grid, Dot start, Dot end, FuncList<Dot> nodes, FuncMap<Dot, FuncList<Edge>> graphMap) {
        record NodeInfo(Dot current, long distance, Dot previous) {
            @Override
            public String toString() {
                return "(%d,%d) : %s (from (%s, %s))".formatted(
                        current.row, 
                        current.col, 
                        distance, 
                        (previous == null) ? "null" : previous.row, 
                        (previous == null) ? "null" : previous.col);
            }
        }
        
        Tuple2<Long, FuncList<Dot>> shortestCostPath() {
            var visiteds  = new HashSet<Dot>();
            var nodeInfos = new LinkedHashMap<Dot, NodeInfo>();
            var nextInfos = new PriorityQueue<NodeInfo>(comparing(NodeInfo::distance));
            
            var beforeStart = new Dot(start.row, start.col - 1, "");
            nodes.forEach(node -> {
                var nodeInfo
                        = node.equals(start)
                        ? new NodeInfo(node, 0L,        beforeStart)
                        : new NodeInfo(node, MAX_VALUE, null);
                nodeInfos.put(node, nodeInfo);
                nextInfos.add(nodeInfo);
            });
            
            var current      = start;
            var previous     = beforeStart;
            var currDistance = 0L;
            
            while (!current.equals(end)) {
                var currNode = current;
                var prevNode = previous;
                var currDist = currDistance;
                nextInfos.remove(nodeInfos.get(currNode));
                visiteds.add(currNode);
                
                System.out.println("Current: " + currNode);
                
                var nextNodes = graphMap.get(currNode);
                nextNodes.forEach(next -> {
                    var nextNode = next.to(currNode);
                    if (visiteds.contains(nextNode))
                        return;
                    
                    var currInfo = nodeInfos.get(nextNode);
                    var distance = distance(prevNode, currNode, next);
                    if (distance < currInfo.distance) {
                        var nextInfo = new NodeInfo(nextNode, currDist + distance, currNode);
                        nodeInfos.put(nextNode, nextInfo);
                        nextInfos.remove(currInfo);
                        nextInfos.add(nextInfo);
                    }
                });
    
                var nextInfo = nextInfos.poll();
                previous = nextInfo.previous;
                current  = nextInfo.current;
                currDistance = nextInfo.distance;
            }
            
            System.out.println("Node: ");
            var node = end;
            while (node != start) {
                System.out.println("  " + node);
                node = nodeInfos.get(node).previous;
            }
            
            var shortestDistance = nodeInfos.get(end).distance;
            return Tuple2.of(shortestDistance, FuncList.empty());
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
            return distance;
        }
    }
    
    Object calulate(FuncList<String> lines) {
        lines.forEach(this::println);
        println();
        
        var graph = createGraph(lines);
        var path  = graph.shortestCostPath();
        return path._1();
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
        return new Graph(grid, startDot, endDot, banches, map);
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
        assertAsString("72412", result);
    }
    
}
