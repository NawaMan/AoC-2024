package day18;

import static functionalj.list.intlist.IntFuncList.range;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Math.abs;
import static java.util.Comparator.comparing;
import static org.junit.Assert.fail;

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
import functionalj.list.intlist.IntFuncList;
import functionalj.map.FuncMap;
import functionalj.tuple.Tuple2;

public class Day18Part2Test extends BaseTest {
    
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
                
//                System.out.println("Current: " + currNode);
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
            
//            System.out.println("Node: ");
//            nodeInfos.entrySet().forEach(println);
            
            var node = end;
            var i    = 0;
            while (node != start) {
                System.out.print("  (%2d, %2d)".formatted((node == null) ? "-1" : node.row, (node == null) ? "-1" : node.col));
                i++;
                if (i == 10) {
                    i = 0;
                    System.out.println();
                }
                
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
//        map.entries().sortedBy(Map.Entry::getKey).forEach(println);
        return new Graph(grid, start, end, banches, map);
    }
    
    long calulate(int width, int height, int firstBytes, FuncList<String> input, boolean showGrid) {
        var inputByRow
                = input
                .limit     (firstBytes)
                .map       (grab(regex("[0-9]+")))
                .map       (line -> line.mapToInt(parseInt))
                .groupingBy(line -> line.get(1))
                .mapValue  (line -> line.map(IntFuncList.class::cast).map(each -> each.get(0)).sorted())
                .toImmutableMap();
        
//        inputByRow
//        .entries()
//        .sortedBy(Map.Entry::getKey)
//        .forEach(println);
        
        var lines = IntFuncList.range(0, height).mapToObj(row -> {
            var cols = inputByRow.get(row);
            return range(0, width).mapToObj(col -> {
                return cols.contains(col) ? "#" : ".";
            }).join();
        });
        
        
        var grid = new Grid(lines);
        if (showGrid) {
            println(grid);
        }

        var graph = createGraph(width, height, lines);
        var path  = graph.shortestCostPath();
        return path._1();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        for (int i = 12; i < lines.size(); i++) {
            var result = calulate(7, 7, i, lines, true);
            println("result: " + result);
            
            if (Math.abs(result) > 23372036854775799L) {
                assertAsString("21", i);
                return;
            }
        }
        fail("Not found!");
    }
    
    String knownPath = """
                (70, 70)
                  (70, 69)
                  (70, 68)
                  (69, 68)
                  (68, 68)
                  (68, 67)
                  (68, 66)
                  (67, 66)
                  (66, 66)
                  (66, 67)
                  (66, 68)
                  (66, 69)
                  (66, 70)
                  (65, 70)
                  (64, 70)
                  (64, 69)
                  (64, 68)
                  (63, 68)
                  (62, 68)
                  (62, 67)
                  (62, 66)
                  (61, 66)
                  (60, 66)
                  (60, 65)
                  (60, 64)
                  (61, 64)
                  (62, 64)
                  (62, 63)
                  (62, 62)
                  (63, 62)
                  (64, 62)
                  (64, 61)
                  (64, 60)
                  (63, 60)
                  (62, 60)
                  (61, 60)
                  (60, 60)
                  (60, 59)
                  (60, 58)
                  (59, 58)
                  (58, 58)
                  (58, 57)
                  (58, 56)
                  (57, 56)
                  (56, 56)
                  (56, 57)
                  (56, 58)
                  (55, 58)
                  (54, 58)
                  (53, 58)
                  (52, 58)
                  (51, 58)
                  (50, 58)
                  (49, 58)
                  (48, 58)
                  (48, 57)
                  (48, 56)
                  (49, 56)
                  (50, 56)
                  (51, 56)
                  (52, 56)
                  (53, 56)
                  (54, 56)
                  (54, 55)
                  (54, 54)
                  (54, 53)
                  (54, 52)
                  (54, 51)
                  (54, 50)
                  (55, 50)
                  (56, 50)
                  (56, 51)
                  (56, 52)
                  (57, 52)
                  (58, 52)
                  (58, 51)
                  (58, 50)
                  (59, 50)
                  (60, 50)
                  (60, 51)
                  (60, 52)
                  (60, 53)
                  (60, 54)
                  (60, 55)
                  (60, 56)
                  (61, 56)
                  (62, 56)
                  (62, 57)
                  (62, 58)
                  (63, 58)
                  (64, 58)
                  (64, 57)
                  (64, 56)
                  (64, 55)
                  (64, 54)
                  (64, 53)
                  (64, 52)
                  (64, 51)
                  (64, 50)
                  (65, 50)
                  (66, 50)
                  (66, 51)
                  (66, 52)
                  (66, 53)
                  (66, 54)
                  (67, 54)
                  (68, 54)
                  (69, 54)
                  (70, 54)
                  (70, 53)
                  (70, 52)
                  (69, 52)
                  (68, 52)
                  (68, 51)
                  (68, 50)
                  (68, 49)
                  (68, 48)
                  (69, 48)
                  (70, 48)
                  (70, 47)
                  (70, 46)
                  (69, 46)
                  (68, 46)
                  (68, 45)
                  (68, 44)
                  (69, 44)
                  (70, 44)
                  (70, 43)
                  (70, 42)
                  (70, 41)
                  (70, 40)
                  (70, 39)
                  (70, 38)
                  (69, 38)
                  (68, 38)
                  (68, 39)
                  (68, 40)
                  (68, 41)
                  (68, 42)
                  (67, 42)
                  (66, 42)
                  (66, 41)
                  (66, 40)
                  (65, 40)
                  (64, 40)
                  (64, 39)
                  (64, 38)
                  (63, 38)
                  (62, 38)
                  (62, 37)
                  (62, 36)
                  (63, 36)
                  (64, 36)
                  (65, 36)
                  (66, 36)
                  (67, 36)
                  (68, 36)
                  (68, 35)
                  (68, 34)
                  (69, 34)
                  (70, 34)
                  (70, 33)
                  (70, 32)
                  (70, 31)
                  (70, 30)
                  (70, 29)
                  (70, 28)
                  (69, 28)
                  (68, 28)
                  (67, 28)
                  (66, 28)
                  (66, 27)
                  (66, 26)
                  (66, 25)
                  (66, 24)
                  (66, 23)
                  (66, 22)
                  (65, 22)
                  (64, 22)
                  (64, 21)
                  (64, 20)
                  (64, 19)
                  (64, 18)
                  (63, 18)
                  (62, 18)
                  (62, 19)
                  (62, 20)
                  (61, 20)
                  (60, 20)
                  (59, 20)
                  (58, 20)
                  (57, 20)
                  (56, 20)
                  (55, 20)
                  (54, 20)
                  (53, 20)
                  (52, 20)
                  (52, 21)
                  (52, 22)
                  (53, 22)
                  (54, 22)
                  (54, 23)
                  (54, 24)
                  (53, 24)
                  (52, 24)
                  (52, 25)
                  (52, 26)
                  (52, 27)
                  (52, 28)
                  (53, 28)
                  (54, 28)
                  (55, 28)
                  (56, 28)
                  (56, 29)
                  (56, 30)
                  (55, 30)
                  (54, 30)
                  (53, 30)
                  (52, 30)
                  (52, 31)
                  (52, 32)
                  (52, 33)
                  (52, 34)
                  (53, 34)
                  (54, 34)
                  (54, 35)
                  (54, 36)
                  (54, 37)
                  (54, 38)
                  (55, 38)
                  (56, 38)
                  (56, 39)
                  (56, 40)
                  (56, 41)
                  (56, 42)
                  (55, 42)
                  (54, 42)
                  (54, 43)
                  (54, 44)
                  (55, 44)
                  (56, 44)
                  (57, 44)
                  (58, 44)
                  (58, 43)
                  (58, 42)
                  (58, 41)
                  (58, 40)
                  (59, 40)
                  (60, 40)
                  (60, 41)
                  (60, 42)
                  (61, 42)
                  (62, 42)
                  (62, 43)
                  (62, 44)
                  (61, 44)
                  (60, 44)
                  (60, 45)
                  (60, 46)
                  (59, 46)
                  (58, 46)
                  (58, 47)
                  (58, 48)
                  (57, 48)
                  (56, 48)
                  (55, 48)
                  (54, 48)
                  (53, 48)
                  (52, 48)
                  (51, 48)
                  (50, 48)
                  (49, 48)
                  (48, 48)
                  (47, 48)
                  (46, 48)
                  (46, 47)
                  (46, 46)
                  (45, 46)
                  (44, 46)
                  (44, 45)
                  (44, 44)
                  (43, 44)
                  (42, 44)
                  (42, 45)
                  (42, 46)
                  (41, 46)
                  (40, 46)
                  (40, 45)
                  (40, 44)
                  (40, 43)
                  (40, 42)
                  (40, 41)
                  (40, 40)
                  (39, 40)
                  (38, 40)
                  (37, 40)
                  (36, 40)
                  (35, 40)
                  (34, 40)
                  (34, 41)
                  (34, 42)
                  (35, 42)
                  (36, 42)
                  (37, 42)
                  (38, 42)
                  (38, 43)
                  (38, 44)
                  (38, 45)
                  (38, 46)
                  (37, 46)
                  (36, 46)
                  (36, 45)
                  (36, 44)
                  (35, 44)
                  (34, 44)
                  (34, 45)
                  (34, 46)
                  (34, 47)
                  (34, 48)
                  (35, 48)
                  (36, 48)
                  (36, 49)
                  (36, 50)
                  (35, 50)
                  (34, 50)
                  (33, 50)
                  (32, 50)
                  (32, 51)
                  (32, 52)
                  (32, 53)
                  (32, 54)
                  (31, 54)
                  (30, 54)
                  (30, 53)
                  (30, 52)
                  (30, 51)
                  (30, 50)
                  (29, 50)
                  (28, 50)
                  (28, 51)
                  (28, 52)
                  (27, 52)
                  (26, 52)
                  (25, 52)
                  (24, 52)
                  (24, 53)
                  (24, 54)
                  (25, 54)
                  (26, 54)
                  (27, 54)
                  (28, 54)
                  (28, 55)
                  (28, 56)
                  (27, 56)
                  (26, 56)
                  (25, 56)
                  (24, 56)
                  (23, 56)
                  (22, 56)
                  (22, 55)
                  (22, 54)
                  (21, 54)
                  (20, 54)
                  (19, 54)
                  (18, 54)
                  (18, 55)
                  (18, 56)
                  (18, 57)
                  (18, 58)
                  (18, 59)
                  (18, 60)
                  (17, 60)
                  (16, 60)
                  (15, 60)
                  (14, 60)
                  (14, 61)
                  (14, 62)
                  (14, 63)
                  (14, 64)
                  (13, 64)
                  (12, 64)
                  (12, 63)
                  (12, 62)
                  (12, 61)
                  (12, 60)
                  (12, 59)
                  (12, 58)
                  (13, 58)
                  (14, 58)
                  (14, 57)
                  (14, 56)
                  (14, 55)
                  (14, 54)
                  (14, 53)
                  (14, 52)
                  (13, 52)
                  (12, 52)
                  (12, 53)
                  (12, 54)
                  (11, 54)
                  (10, 54)
                  (9, 54)
                  (8, 54)
                  (8, 53)
                  (8, 52)
                  (8, 51)
                  (8, 50)
                  (9, 50)
                  (10, 50)
                  (10, 49)
                  (10, 48)
                  (11, 48)
                  (12, 48)
                  (12, 49)
                  (12, 50)
                  (13, 50)
                  (14, 50)
                  (14, 49)
                  (14, 48)
                  (15, 48)
                  (16, 48)
                  (17, 48)
                  (18, 48)
                  (18, 47)
                  (18, 46)
                  (17, 46)
                  (16, 46)
                  (16, 45)
                  (16, 44)
                  (17, 44)
                  (18, 44)
                  (19, 44)
                  (20, 44)
                  (21, 44)
                  (22, 44)
                  (23, 44)
                  (24, 44)
                  (24, 43)
                  (24, 42)
                  (24, 41)
                  (24, 40)
                  (24, 39)
                  (24, 38)
                  (24, 37)
                  (24, 36)
                  (24, 35)
                  (24, 34)
                  (24, 33)
                  (24, 32)
                  (25, 32)
                  (26, 32)
                  (27, 32)
                  (28, 32)
                  (28, 33)
                  (28, 34)
                  (29, 34)
                  (30, 34)
                  (30, 35)
                  (30, 36)
                  (31, 36)
                  (32, 36)
                  (32, 35)
                  (32, 34)
                  (32, 33)
                  (32, 32)
                  (32, 31)
                  (32, 30)
                  (31, 30)
                  (30, 30)
                  (30, 29)
                  (30, 28)
                  (29, 28)
                  (28, 28)
                  (27, 28)
                  (26, 28)
                  (26, 27)
                  (26, 26)
                  (26, 25)
                  (26, 24)
                  (25, 24)
                  (24, 24)
                  (24, 23)
                  (24, 22)
                  (24, 21)
                  (24, 20)
                  (23, 20)
                  (22, 20)
                  (21, 20)
                  (20, 20)
                  (20, 21)
                  (20, 22)
                  (21, 22)
                  (22, 22)
                  (22, 23)
                  (22, 24)
                  (22, 25)
                  (22, 26)
                  (21, 26)
                  (20, 26)
                  (20, 25)
                  (20, 24)
                  (19, 24)
                  (18, 24)
                  (18, 25)
                  (18, 26)
                  (17, 26)
                  (16, 26)
                  (15, 26)
                  (14, 26)
                  (14, 27)
                  (14, 28)
                  (13, 28)
                  (12, 28)
                  (11, 28)
                  (10, 28)
                  (9, 28)
                  (8, 28)
                  (8, 27)
                  (8, 26)
                  (7, 26)
                  (6, 26)
                  (5, 26)
                  (4, 26)
                  (4, 27)
                  (4, 28)
                  (3, 28)
                  (2, 28)
                  (2, 29)
                  (2, 30)
                  (1, 30)
                  (0, 30)
                  (0, 29)
                  (0, 28)
                  (0, 27)
                  (0, 26)
                  (0, 25)
                  (0, 24)
                  (1, 24)
                  (2, 24)
                  (2, 23)
                  (2, 22)
                  (3, 22)
                  (4, 22)
                  (4, 23)
                  (4, 24)
                  (5, 24)
                  (6, 24)
                  (7, 24)
                  (8, 24)
                  (9, 24)
                  (10, 24)
                  (10, 25)
                  (10, 26)
                  (11, 26)
                  (12, 26)
                  (12, 25)
                  (12, 24)
                  (12, 23)
                  (12, 22)
                  (11, 22)
                  (10, 22)
                  (9, 22)
                  (8, 22)
                  (7, 22)
                  (6, 22)
                  (6, 21)
                  (6, 20)
                  (7, 20)
                  (8, 20)
                  (8, 19)
                  (8, 18)
                  (7, 18)
                  (6, 18)
                  (5, 18)
                  (4, 18)
                  (4, 19)
                  (4, 20)
                  (3, 20)
                  (2, 20)
                  (2, 19)
                  (2, 18)
                  (2, 17)
                  (2, 16)
                  (1, 16)
                  (0, 16)
                  (0, 15)
                  (0, 14)
                  (1, 14)
                  (2, 14)
                  (2, 13)
                  (2, 12)
                  (1, 12)
                  (0, 12)
                  (0, 11)
                  (0, 10)
                  (0, 9)
                  (0, 8)
                  (0, 7)
                  (0, 6)
                  (0, 5)
                  (0, 4)
                  (1, 4)
                  (2, 4)
                  (3, 4)
                  (4, 4)
                  (4, 3)
                  (4, 2)
                  (5, 2)
                  (6, 2)
                  (6, 3)
                  (6, 4)
                  (7, 4)
                  (8, 4)
                  (8, 3)
                  (8, 2)
                  (8, 1)
                  (8, 0)
                  (7, 0)
                  (6, 0)
                  (5, 0)
                  (4, 0)
                  (3, 0)
                  (2, 0)
                  (1, 0)
              """;
    
    @Ignore
    @Test
    public void testProd() {
        var lines = readAllLines();
//        var path = FuncList.of(knownPath.split("\n")).map(grab(regex("[0-9]+"))).map(each -> each.mapToInt(parseInt)).cache();
//        var drops = lines.map(grab(regex("[0-9]+"))).map(each -> each.mapToInt(parseInt)).cache();
//        for (int i = 2912; i < 2924; i++) {
//            var drop = drops.get(i);
//            if (!path.contains(drop))
//                continue;
//            
//            var neighbours =
//                    drops
//                    .limit(i - 1)
//                    .filter(n -> ((abs(n.get(0) - drop.get(0)) == 1) && (abs(n.get(1) - drop.get(1)) == 0))
//                              || ((abs(n.get(0) - drop.get(0)) == 0) && (abs(n.get(1) - drop.get(1)) == 1)))
//                    .cache();
//            
//            if (neighbours.size() != 0) {
//                println("index: " + i);
//                println("neighbours: ");
//                neighbours.map(indent()).forEach(println);
//                println();
//            }
//        }
//        
//        
        for (int i = 2914; i < 2916/*lines.size()*/; i++) {
            println("Drop at: " + lines.get(i));
            var result = calulate(71, 71, i, lines, i == 2914);
            println();
            println("index: " + i + ", result:" + result);
            
            if (abs(result) > 23372036854775799L) {
                assertAsString("2915", i);
                return;
            }
        }
        fail("Not found!");
    }
    
}
