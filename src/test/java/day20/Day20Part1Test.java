package day20;

import static functionalj.list.intlist.IntFuncList.range;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Math.abs;
import static java.util.Comparator.comparing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntPredicate;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.FuncListBuilder;
import functionalj.map.FuncMap;
import functionalj.tuple.Tuple2;

public class Day20Part1Test extends BaseTest {

    public static final String BOLD   = "\033[1m";
    public static final String RED    = "\033[31m";
    public static final String GREEN  = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE   = "\033[34m";
    public static final String RESET  = "\033[0m"; // Reset to default color
    
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
        
        int width() {
            return data.length;
        }
        
        int height() {
            return data[0].length;
        }
        
        String at(Position position) {
            return at(position.row, position.col);
        }
        String at(int row, int col) {
            if (row < 0 || row >= data.length)      return "#";
            if (col < 0 || col >= data[row].length) return "#";
            return data[row][col];
        }
        char charAt(Position position) {
            return at(position).charAt(0);
        }
        char charAt(int row, int col) {
            return at(row, col).charAt(0);
        }
        FuncList<Position> positions() {
            return range(0, data.length).toCache().flatMapToObj(row -> {
                return range(0, data[0].length)
                        .toCache ()
                        .mapToObj(col -> new Position(row, col))
                        .filter  (pos -> '.' == charAt(pos))
                        ;
            });
        }
        FuncList<Position> select(IntPredicate charSelector) {
            return range(0, data.length).toCache().flatMapToObj(row -> {
                return range(0, data[0].length)
                        .toCache ()
                        .filter  (col -> charSelector.test((int)charAt(row, col)))
                        .mapToObj(col -> new Position(row, col))
                        ;
            });
        }
        
        protected Grid clone() {
            var clone = new String[data.length][];
            for (int i = 0; i < clone.length; i++) {
                clone[i] = data[i].clone();
            }
            return new Grid(clone, start, end);
        }
        
        @Override
        public String toString() {
            return FuncList.of(data).map(chs -> FuncList.of(chs).join()).join("\n");
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
//            map.entries().sortedBy(Map.Entry::getKey).forEach(println);
            return new Graph(grid, grid.start, grid.end, banches, map);
        }
        
        Tuple2<Long, FuncList<Position>> shortestCostPath() {
            return shortestCostPath(true);
        }
        
        Tuple2<Long, FuncList<Position>> shortestCostPath(boolean showPath) {
            var visiteds  = new HashSet<Position>();
            var nodeInfos = new LinkedHashMap<Position, NodeInfo>();
            var nextInfos = new PriorityQueue<NodeInfo>(comparing(n -> n.distance));
            
//            System.out.println("nodes: ");
//            nodes.stream().map(String::valueOf).map("  "::concat).forEach(println);
            
            var beforeStart = new Position(start.row, start.col - 1);
            nodes.forEach(node -> {
                var nodeInfo
                        = node.equals(start)
                        ? new NodeInfo(node, 0L,        beforeStart)
                        : new NodeInfo(node, MAX_VALUE, null);
                nodeInfos.put(node, nodeInfo);
                nextInfos.add(nodeInfo);
            });
            
//            System.out.println("nextInfos: ");
//            nextInfos.stream().map(String::valueOf).map("  "::concat).forEach(println);
            
            var current      = start;
            var currDistance = 0L;
            
            while (!current.equals(end)) {
                var currNode = current;
                var currDist = currDistance;
                var currInfo = nodeInfos.get(currNode);
                nextInfos.remove(currInfo);
                visiteds.add(currNode);
                
//                System.out.println("Current: " + currNode);
                var nextNodes = graphMap.get(currNode);
                if (nextNodes != null) {
                    nextNodes.forEach(next -> {
                        var nextNode = next.to(currNode);
                        if (visiteds.contains(nextNode))
                            return;
                        
//                        System.out.println("Next: " + nextNode);
                        if (currInfo.previous == null)
                            System.out.print("");
                        
                        var nextInfo = nodeInfos.get(nextNode);
                        var distance = next.distance;
                        if (distance < nextInfo.distance) {
                            nextInfo = new NodeInfo(nextNode, currDist + distance, currNode);
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
            
//            System.out.println("Node: ");
//            nodeInfos.entrySet().forEach(println);
            
            var path    = new FuncListBuilder<Position>();
            var display = grid.clone();
            var node = end;
            var i    = 0;
            while (node != start) {
                path.add(node);
                display.data[node.row][node.col] = BOLD + BLUE + "#" + RESET;
                
                if (showPath)
                    System.out.print("  (%2d, %2d)".formatted((node == null) ? -1 : node.row, (node == null) ? -1 : node.col));
                i++;
                if (i == 10) {
                    i = 0;
                    if (showPath)
                        System.out.println();
                }
                
                var nodeInfo = nodeInfos.get(node);
                if (nodeInfo == null)
                    break;
                
                node = nodeInfo.previous;
            }
            if (node == start) {
                path.add(start);
            }
            
            display.data[end.row][end.col]     = BOLD + GREEN + "#" + RESET;
            display.data[start.row][start.col] = BOLD + RED + "#" + RESET;
            
            if (showPath) {
                System.out.println();
                System.out.println(display);
            }
            
            var endNodeInfo = nodeInfos.get(end);
            if (endNodeInfo == null) {
                return Tuple2.of(Long.MAX_VALUE, FuncList.empty());
            }
            
            var shortestDistance = endNodeInfo.distance;
            return Tuple2.of(shortestDistance, path.build().reverse());
        }
    }
    
    Object calulate(FuncList<String> lines) {
        var grid = Grid.from(lines);
        
        println(grid);
        
        var graph = Graph.from(grid);
//        graph.graphMap.entries().sortedBy(Map.Entry::getKey).forEach(println);;
        var shortestPath = graph.shortestCostPath();
        println(shortestPath);
        
        var path          = shortestPath._2();
        var nodeWithIndex = path.mapWithIndex();
        
        println(nodeWithIndex);
        
        var savedCounts = new HashMap<Integer, AtomicInteger>();
        var count = 0;
        for (var one : nodeWithIndex) {
            for (var two : nodeWithIndex) {
                var oneOrder = one._1;
                var twoOrder = two._1;
                if ((twoOrder - oneOrder) < 4)
                    continue;
                
                var onePos = one._2;
                var twoPos = two._2;
                var cheatTime = abs(onePos.row() - twoPos.row()) + abs(onePos.col() - twoPos.col());
                var savedTime = twoOrder - oneOrder - cheatTime;
                
                if (cheatTime <= 2 && savedTime >= 100)
                    count++;
            }
        }
        
//        FuncMap.from(savedCounts).entries().filter(entry -> entry.getKey() >= 100).sortedBy(Map.Entry::getKey).forEach(println);
        
//        return FuncMap.from(savedCounts).entries().filter(entry -> entry.getKey() >= 100).map(Map.Entry::getValue).sumToInt(AtomicInteger::intValue);
        return count;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("0", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("1317", result);
    }
    
}
