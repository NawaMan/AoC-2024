package day16;

import static functionalj.list.intlist.IntFuncList.range;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Math.abs;
import static java.lang.Math.signum;
import static java.util.Comparator.comparing;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.FuncListBuilder;
import functionalj.map.FuncMap;
import functionalj.tuple.Tuple2;

/**
 * --- Day 16: Reindeer Maze ---
 * 
 * It's time again for the Reindeer Olympics! This year, the big event is the Reindeer Maze, where the Reindeer compete 
 *   for the lowest score.
 * 
 * You and The Historians arrive to search for the Chief right as the event is about to start. It wouldn't hurt to watch 
 *   a little, right?
 * 
 * The Reindeer start on the Start Tile (marked S) facing East and need to reach the End Tile (marked E). They can move 
 *   forward one tile at a time (increasing their score by 1 point), but never into a wall (#). They can also rotate 
 *   clockwise or counterclockwise 90 degrees at a time (increasing their score by 1000 points).
 * 
 * To figure out the best place to sit, you start by grabbing a map (your puzzle input) from a nearby kiosk. 
 * For example:
 * 
 * ###############
 * #.......#....E#
 * #.#.###.#.###.#
 * #.....#.#...#.#
 * #.###.#####.#.#
 * #.#.#.......#.#
 * #.#.#####.###.#
 * #...........#.#
 * ###.#.#####.#.#
 * #...#.....#.#.#
 * #.#.#.###.#.#.#
 * #.....#...#.#.#
 * #.###.#.#.#.#.#
 * #S..#.....#...#
 * ###############
 * 
 * There are many paths through this maze, but taking any of the best paths would incur a score of only 7036. 
 *   This can be achieved by taking a total of 36 steps forward and turning 90 degrees a total of 7 times:
 * 
 * 
 * ###############
 * #.......#....E#
 * #.#.###.#.###^#
 * #.....#.#...#^#
 * #.###.#####.#^#
 * #.#.#.......#^#
 * #.#.#####.###^#
 * #..>>>>>>>>v#^#
 * ###^#.#####v#^#
 * #>>^#.....#v#^#
 * #^#.#.###.#v#^#
 * #^....#...#v#^#
 * #^###.#.#.#v#^#
 * #S..#.....#>>^#
 * ###############
 * 
 * Here's a second example:
 * 
 * #################
 * #...#...#...#..E#
 * #.#.#.#.#.#.#.#.#
 * #.#.#.#...#...#.#
 * #.#.#.#.###.#.#.#
 * #...#.#.#.....#.#
 * #.#.#.#.#.#####.#
 * #.#...#.#.#.....#
 * #.#.#####.#.###.#
 * #.#.#.......#...#
 * #.#.###.#####.###
 * #.#.#...#.....#.#
 * #.#.#.#####.###.#
 * #.#.#.........#.#
 * #.#.#.#########.#
 * #S#.............#
 * #################
 * 
 * In this maze, the best paths cost 11048 points; following one such path would look like this:
 * 
 * #################
 * #...#...#...#..E#
 * #.#.#.#.#.#.#.#^#
 * #.#.#.#...#...#^#
 * #.#.#.#.###.#.#^#
 * #>>v#.#.#.....#^#
 * #^#v#.#.#.#####^#
 * #^#v..#.#.#>>>>^#
 * #^#v#####.#^###.#
 * #^#v#..>>>>^#...#
 * #^#v###^#####.###
 * #^#v#>>^#.....#.#
 * #^#v#^#####.###.#
 * #^#v#^........#.#
 * #^#v#^#########.#
 * #S#>>^..........#
 * #################
 * 
 * Note that the path shown above includes one 90 degree turn as the very first move, rotating the Reindeer from facing 
 *   East to facing North.
 * 
 * Analyze your map carefully. What is the lowest score a Reindeer could possibly get?
 * 
 * Your puzzle answer was 66404.
 */
public class Day16Part1Test extends BaseTest {
    
    public static final String BOLD = "\033[1m";
    public static final String BLUE  = "\033[34m";
    public static final String RESET = "\033[0m"; // Reset to default color
    
    record Position(int row, int col) {
        FuncList<Position> neighbours() {
            return FuncList.of(
                        new Position(row + 1, col()    ),
                        new Position(row    , col() + 1),
                        new Position(row - 1, col()    ),
                        new Position(row    , col() - 1));
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
        FuncList<Position> positions() {
            return range(0, data.length).toCache().flatMapToObj(row -> {
                return range(0, data[0].length)
                        .mapToObj(col -> new Position(row, col))
                        .filter  (pos -> '.' == charAt(pos))
                        .cache   ()
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
        
        Tuple2<Long, FuncList<Position>> shortestCostPath() {
            var visiteds    = new HashSet<Position>();
            var nodeInfos   = new LinkedHashMap<Position, NodeInfo>();
            var nextInfos   = new PriorityQueue<NodeInfo>(comparing(n -> n.distance));
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
                if (nextNodes != null) {
                    nextNodes.forEach(next -> {
                        var nextNode = next.to(currNode);
                        if (visiteds.contains(nextNode))
                            return;
                        
                        var nextInfo = nodeInfos.get(nextNode);
                        var distance = distance(currInfo.previous, currInfo.current, next);
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
            
            var path    = new FuncListBuilder<Position>();
//            var display = grid.clone();
            var node = end;
            while (node != start) {
                path.add(node);
//                display.data[node.row][node.col] = BOLD + BLUE + "#" + RESET;
                
                var nodeInfo = nodeInfos.get(node);
                if (nodeInfo == null)
                    break;
                
                node = nodeInfo.previous;
            }
            path.add(start);
//            display.data[node.row][node.col] = BOLD + BLUE + "#" + RESET;
//            System.out.println();
//            System.out.println(display);
            
            var shortestDistance = nodeInfos.get(end).distance;
            return Tuple2.of(shortestDistance, path.build().reverse());
        }
        long distance(Position previous, Position current, Edge edge) {
            var diffRow1 = signum(current.row - previous.row);
            var diffCol1 = signum(current.col - previous.col);
            
            var to = edge.to(current);
            var diffRow2 = signum(to.row - current.row);
            var diffCol2 = signum(to.col - current.col);
            
            var notTurn = (diffRow1 == diffRow2) || (diffCol1 == diffCol2);
            var turn = !notTurn;
            var distance = (turn ? 1000L : 0L) + edge.distance;
            return distance;
        }
    }
    
    Object calculate(FuncList<String> lines) {
        var grid = Grid.from(lines);
//        fillDeadEnds(grid);
        
        var graph = Graph.from(grid);
        var path  = graph.shortestCostPath();
        return path._1();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("7036", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("66404", result);
    }
    
    void fillDeadEnds(Grid grid) {
        var seens = new LinkedHashSet<Position>();
        var nodes = new LinkedHashSet<Position>(grid.positions());
        while (nodes.size() != 0) {
            var node = nodes.removeFirst();
            if (node.equals(grid.start) || node.equals(grid.end))
                continue;
            
            int count = node.neighbours().filter(n -> grid.charAt(n) == '.').size();
            if (count <= 1) {
                grid.data[node.row][node.col] = "#";
                seens.add(node);
                
                var freeNeighbors = node.neighbours().toCache().filter(n -> grid.charAt(n) == '.').excludeIn(seens);
                nodes.addAll(freeNeighbors);
            }
        }
    }
    
}
