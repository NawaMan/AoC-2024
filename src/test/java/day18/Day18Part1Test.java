package day18;

import static functionalj.list.intlist.IntFuncList.range;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Math.abs;
import static java.util.Comparator.comparing;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntPredicate;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.FuncListBuilder;
import functionalj.list.intlist.IntFuncList;
import functionalj.map.FuncMap;
import functionalj.tuple.Tuple2;

public class Day18Part1Test extends BaseTest {
    
    record Position(int row, int col) implements Comparable<Position> {
        FuncList<Position> neighbours() {
            return FuncList.of(
                        new Position(row + 1, col()    ),
                        new Position(row    , col() + 1),
                        new Position(row - 1, col()    ),
                        new Position(row    , col() - 1));
        }
        @Override
        public String toString() {
            return "(%d, %d)".formatted(row, col);
        }
        @Override
        public int compareTo(Position o) {
            return comparing(Position::row)
                    .thenComparing(Position::col)
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
        FuncList<Position> selectPositions() {
            return range(0, lines.size()).toCache().flatMapToObj(row -> {
                return range(0, lines.get(0).length())
                        .toCache ()
                        .mapToObj(col -> new Position(row, col))
                        .filter  (pos -> charAt(pos) == '.')
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
        @Override
        public String toString() {
            return lines.join("\n");
        }
    }
    
    record Edge(Position start, Position end, int distance) {
        Position to(Position from) {
            return from.equals(start) ? end   :
                   from.equals(end)   ? start : null; 
        }
        @Override
        public String toString() {
            return "Edge[(%d,%d)->(%d,%d) : %d]"
                    .formatted(start.row, start.col, end.row, end.col, distance);
        }
    }
    
    record Graph(Grid grid, Position start, Position end, FuncList<Position> nodes, FuncMap<Position, FuncList<Edge>> graphMap) {
        record NodeInfo(Position current, long distance, Position previous) {
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
        
        Tuple2<Long, FuncList<Position>> shortestCostPath() {
            var visiteds  = new HashSet<Position>();
            var nodeInfos = new LinkedHashMap<Position, NodeInfo>();
            var nextInfos = new PriorityQueue<NodeInfo>(comparing(NodeInfo::distance));
            
            var beforeStart = start;//new Position(start.row, start.col - 1);
            nodes.forEach(node -> {
                var nodeInfo
                        = node.equals(start)
                        ? new NodeInfo(node, 0L,        beforeStart)
                        : new NodeInfo(node, MAX_VALUE, null);
                nodeInfos.put(node, nodeInfo);
                nextInfos.add(nodeInfo);
            });
            
            var current      = start;
            var currDistance = 0L;
            
            while (!current.equals(end)) {
                var currNode = current;
                var currDist = currDistance;
                nextInfos.remove(nodeInfos.get(currNode));
                visiteds.add(currNode);
                
                System.out.println("Current: " + currNode);
                var nextNodes = graphMap.get(currNode);
                if (nextNodes != null) {
                    nextNodes.forEach(next -> {
                        var nextNode = next.to(currNode);
                        if (visiteds.contains(nextNode))
                            return;
                        
                        var currInfo = nodeInfos.get(nextNode);
                        var distance = next.distance;
                        if (distance < currInfo.distance) {
                            var nextInfo = new NodeInfo(nextNode, currDist + distance, currNode);
                            nodeInfos.put(nextNode, nextInfo);
                            nextInfos.remove(currInfo);
                            nextInfos.add(nextInfo);
                        }
                    });
                }
    
                var nextInfo = nextInfos.poll();
                if (nextInfo == null)
                    break;
                
                current      = nextInfo.current;
                currDistance = nextInfo.distance;
            }
            
            System.out.println("Node: ");
            nodeInfos.entrySet().forEach(println);
            
            var node = end;
            while (node != start) {
                System.out.println("  " + node);
                var nodeInfo = nodeInfos.get(node);
                if (nodeInfo == null)
                    break;
                
                node = nodeInfo.previous;
            }
            
            var shortestDistance = nodeInfos.get(end).distance;
            return Tuple2.of(shortestDistance, FuncList.empty());
        }
    }

    Graph createGraph(int width, int height, FuncList<String> lines) {
        var grid  = new Grid(lines);
        var start = new Position(0,         0);      // grid.select(i -> i == (int)'S').findFirst().get();
        var end   = new Position(width - 1, height - 1); //grid.select(i -> i == (int)'E').findFirst().get();
        
        var graphMap = new ConcurrentHashMap<Position, FuncListBuilder<Edge>>();
        var banches  = grid.selectPositions().cache();
        banches.forEach(pos -> {
            pos
            .neighbours()
            .filter(n -> grid.charAt(n) == '.')
            .forEach(n -> {
                var edge = new Edge(pos, n, abs(n.col - pos.col) + abs(n.row - pos.row));
                graphMap.putIfAbsent(pos, new FuncListBuilder<Edge>());
                graphMap.get(pos).add(edge);
            });
        });
        
        var map = FuncMap.from(graphMap).mapValue(FuncListBuilder::build);
        map.entries().sortedBy(Map.Entry::getKey).forEach(println);
        return new Graph(grid, start, end, banches, map);
    }
    
    Object calulate(int width, int height, int firstBytes, FuncList<String> input) {
        var inputByRow
                = input
                .limit     (firstBytes)
                .map       (grab(regex("[0-9]+")))
                .map       (line -> line.mapToInt(parseInt))
                .peek      (println)
                .groupingBy(line -> line.get(1))
                .mapValue  (line -> line.map(IntFuncList.class::cast).map(each -> each.get(0)).sorted())
                .toImmutableMap();
        
        inputByRow
        .entries()
        .sortedBy(Map.Entry::getKey)
        .forEach(println);
        
        var lines = IntFuncList.range(0, height).mapToObj(row -> {
            var cols = inputByRow.get(row);
            return range(0, width).mapToObj(col -> {
                return cols.contains(col) ? "#" : ".";
            }).join();
        });
        
        
        var grid = new Grid(lines);
        println(grid);

        var graph = createGraph(width, height, lines);
        var path  = graph.shortestCostPath();
        return path._1();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(7, 7, 12, lines);
        println("result: " + result);
        assertAsString("22", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(71, 71, 1024, lines);
        println("result: " + result);
        assertAsString("340", result);
    }
    
}
