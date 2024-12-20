package day20;

import static functionalj.list.intlist.IntFuncList.range;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Math.abs;
import static java.util.Comparator.comparing;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.FuncListBuilder;
import functionalj.map.FuncMap;

public class Day20Part1Test extends BaseTest {
    
    record Position(int row, int col) {
        FuncList<Position> neighbours() {
            return FuncList.of(
                        new Position(row + 1, col()    ),
                        new Position(row    , col() + 1),
                        new Position(row - 1, col()    ),
                        new Position(row    , col() - 1));
        }
    }
    
    record Edge(Position start, Position end, int distance) {}
    record Shortest(long distance, FuncList<Position> path) {}
    
    record Grid(String[][] data, Position start, Position end) {
        
        static Grid from(FuncList<String> lines) {
            var data = lines
                     .map    (line -> line.chars())
                     .map    (line -> line.mapToObj(i -> "" + (char)i))
                     .map    (line -> line.toArray (String[]::new))
                     .toArray(String[][]::new);
            var start = (Position)null;
            var end   = (Position)null;
            for (int r = 0; r < data.length; r++) {
                for (int c = 0; c < data[r].length; c++) {
                    var ch = data[r][c].charAt(0);
                    if (ch == 'S') { start = new Position(r, c); data[r][c] = "."; }
                    if (ch == 'E') { end   = new Position(r, c); data[r][c] = "."; }
                }
            }
            return new Grid(data, start, end);
        }
        
        char charAt(Position position) {
            return data[position.row][position.col].charAt(0);
        }
        FuncList<Position> positions() {
            return range(0, data.length).toCache().flatMapToObj(row -> {
                return range(0, data[0].length)
                        .mapToObj(col -> new Position(row, col))
                        .filter  (pos -> '.' == charAt(pos))
                        ;
            });
        }
    }
    
    record Graph(Grid grid, Position start, Position end, FuncList<Position> nodes, FuncMap<Position, FuncList<Edge>> graphMap) {
        record NodeInfo(Position current, long distance, Position previous) {}
        
        static Graph from(Grid grid) {
            var graphMap = new ConcurrentHashMap<Position, FuncListBuilder<Edge>>();
            var banches  = grid.positions().cache();
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
            return new Graph(grid, grid.start, grid.end, banches, map);
        }
        
        Shortest shortestPath() {
            var visiteds  = new HashSet<Position>();
            var nodeInfos = new LinkedHashMap<Position, NodeInfo>();
            var nextInfos = new PriorityQueue<NodeInfo>(comparing(n -> n.distance));
            
            var beforeStart = new Position(start.row, start.col - 1);
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
                var currInfo = nodeInfos.get(currNode);
                nextInfos.remove(currInfo);
                visiteds.add(currNode);
                
                var nextNodes = graphMap.get(currNode);
                nextNodes.forEach(next -> {
                    var nextNode = next.end();
                    if (visiteds.contains(nextNode))
                        return;
                    
                    var nextInfo = nodeInfos.get(nextNode);
                    var distance = next.distance;
                    if (distance < nextInfo.distance) {
                        nextInfo = new NodeInfo(nextNode, currDist + distance, currNode);
                        nodeInfos.put(nextNode, nextInfo);
                        nextInfos.remove(currInfo);
                        nextInfos.add(nextInfo);
                    }
                });
                
                var nextInfo = nextInfos.poll();
                current      = nextInfo.current;
                currDistance = nextInfo.distance;
            }
            
            var path = new FuncListBuilder<Position>();
            var node = end;
            while (node != start) {
                path.add(node);
                node = nodeInfos.get(node).previous;
            }
            path.add(start);
            
            var shortestDistance = nodeInfos.get(end).distance;
            return new Shortest(shortestDistance, path.build().reverse().cache());
        }
    }
    
    Object calulate(FuncList<String> lines, int allowCheat, int atLeastSaved) {
        var grid  = Grid.from(lines);
        var graph = Graph.from(grid);
        
        var shortestPath = graph.shortestPath();
        var nodeWithIndex = shortestPath.path.mapWithIndex();
        return nodeWithIndex.sumToInt(one -> {
            return nodeWithIndex.filter(two -> {
                var oneOrder = one._1;
                var twoOrder = two._1;
                var onePos   = one._2;
                var twoPos   = two._2;
                var cheatTime = abs(onePos.row() - twoPos.row()) + abs(onePos.col() - twoPos.col());
                var savedTime = twoOrder - oneOrder - cheatTime;
                return (cheatTime <= allowCheat && savedTime >= atLeastSaved);
            })
            .size();
        });
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines, 2, 100);
        println("result: " + result);
        assertAsString("0", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines, 2, 100);
        println("result: " + result);
        assertAsString("1317", result);
    }
    
}
