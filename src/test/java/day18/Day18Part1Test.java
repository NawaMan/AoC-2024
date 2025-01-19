package day18;

import static functionalj.list.intlist.IntFuncList.range;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Math.abs;
import static java.util.Comparator.comparing;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.FuncListBuilder;
import functionalj.list.intlist.IntFuncList;
import functionalj.map.FuncMap;
import functionalj.tuple.Tuple2;

/**
 * --- Day 18: RAM Run ---
 * 
 * You and The Historians look a lot more pixelated than you remember. You're inside a computer at the North Pole!
 * 
 * Just as you're about to check out your surroundings, a program runs up to you. "This region of memory isn't safe! 
 *   The User misunderstood what a pushdown automaton is and their algorithm is pushing whole bytes down on top of us! 
 *   Run!"
 * 
 * The algorithm is fast - it's going to cause a byte to fall into your memory space once every nanosecond! Fortunately, 
 *   you're faster, and by quickly scanning the algorithm, you create a list of which bytes will fall (your puzzle input) 
 *   in the order they'll land in your memory space.
 * 
 * Your memory space is a two-dimensional grid with coordinates that range from 0 to 70 both horizontally and vertically. 
 *   However, for the sake of example, suppose you're on a smaller grid with coordinates that range from 0 to 6 and 
 *   the following list of incoming byte positions:
 * 
 * 5,4
 * 4,2
 * 4,5
 * 3,0
 * 2,1
 * 6,3
 * 2,4
 * 1,5
 * 0,6
 * 3,3
 * 2,6
 * 5,1
 * 1,2
 * 5,5
 * 2,5
 * 6,5
 * 1,4
 * 0,4
 * 6,4
 * 1,1
 * 6,1
 * 1,0
 * 0,5
 * 1,6
 * 2,0
 * 
 * Each byte position is given as an X,Y coordinate, where X is the distance from the left edge of your memory space and
 *   Y is the distance from the top edge of your memory space.
 * 
 * You and The Historians are currently in the top left corner of the memory space (at 0,0) and need to reach the exit 
 *   in the bottom right corner (at 70,70 in your memory space, but at 6,6 in this example). You'll need to simulate 
 *   the falling bytes to plan out where it will be safe to run; for now, simulate just the first few bytes falling into
 *   your memory space.
 * 
 * As bytes fall into your memory space, they make that coordinate corrupted. Corrupted memory coordinates cannot 
 *   be entered by you or The Historians, so you'll need to plan your route carefully. You also cannot leave 
 *   the boundaries of the memory space; your only hope is to reach the exit.
 * 
 * In the above example, if you were to draw the memory space after the first 12 bytes have fallen (using . for safe and
 *   # for corrupted), it would look like this:
 * 
 * ...#...
 * ..#..#.
 * ....#..
 * ...#..#
 * ..#..#.
 * .#..#..
 * #.#....
 * 
 * You can take steps up, down, left, or right. After just 12 bytes have corrupted locations in your memory space, 
 *   the shortest path from the top left corner to the exit would take 22 steps. Here (marked with O) is one such path:
 * 
 * OO.#OOO
 * .O#OO#O
 * .OOO#OO
 * ...#OO#
 * ..#OO#.
 * .#.O#..
 * #.#OOOO
 * 
 * Simulate the first kilobyte (1024 bytes) falling onto your memory space. Afterward, what is the minimum number of 
 *   steps needed to reach the exit?
 * 
 * Your puzzle answer was 340.
 */
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
            
            var node = end;
            while (node != start) {
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
        var start = new Position(0,         0);
        var end   = new Position(width - 1, height - 1);
        
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
        return new Graph(grid, start, end, banches, map);
    }
    
    Object calculate(int width, int height, int firstBytes, FuncList<String> input) {
        var inputByRow
                = input
                .limit     (firstBytes)
                .map       (grab(regex("[0-9]+")))
                .map       (line -> line.mapToInt(parseInt))
                .groupingBy(line -> line.get(1))
                .mapValue  (line -> line.map(IntFuncList.class::cast).map(each -> each.get(0)).sorted())
                .toImmutableMap();
        
        var lines = IntFuncList.range(0, height).mapToObj(row -> {
            var cols = inputByRow.get(row);
            return range(0, width).mapToObj(col -> {
                return cols.contains(col) ? "#" : ".";
            }).join();
        });
        
        var graph = createGraph(width, height, lines);
        var path  = graph.shortestCostPath();
        return path._1();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(7, 7, 12, lines);
        println("result: " + result);
        assertAsString("22", result);
    }
    
    @Ignore
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(71, 71, 1024, lines);
        println("result: " + result);
        assertAsString("340", result);
    }
    
}
