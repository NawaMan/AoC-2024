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

/**
 * --- Day 20: Race Condition ---
 * 
 * The Historians are quite pixelated again. This time, a massive, black building looms over you - you're right outside 
 *   the CPU!
 * 
 * While The Historians get to work, a nearby program sees that you're idle and challenges you to a race. Apparently, 
 *   you've arrived just in time for the frequently-held race condition festival!
 * 
 * The race takes place on a particularly long and twisting code path; programs compete to see who can finish in 
 *   the fewest picoseconds. The winner even gets their very own mutex!
 * 
 * They hand you a map of the racetrack (your puzzle input). For example:
 * 
 * ###############
 * #...#...#.....#
 * #.#.#.#.#.###.#
 * #S#...#.#.#...#
 * #######.#.#.###
 * #######.#.#...#
 * #######.#.###.#
 * ###..E#...#...#
 * ###.#######.###
 * #...###...#...#
 * #.#####.#.###.#
 * #.#...#.#.#...#
 * #.#.#.#.#.#.###
 * #...#...#...###
 * ###############
 * 
 * The map consists of track (.) - including the start (S) and end (E) positions (both of which also count as track) - 
 *   and walls (#).
 * 
 * When a program runs through the racetrack, it starts at the start position. Then, it is allowed to move up, down, 
 *   left, or right; each such move takes 1 picosecond. The goal is to reach the end position as quickly as possible. 
 *   In this example racetrack, the fastest time is 84 picoseconds.
 * 
 * Because there is only a single path from the start to the end and the programs all go the same speed, the races used 
 *   to be pretty boring. To make things more interesting, they introduced a new rule to the races: programs are allowed 
 *   to cheat.
 * 
 * The rules for cheating are very strict. Exactly once during a race, a program may disable collision for up to 
 *   2 picoseconds. This allows the program to pass through walls as if they were regular track. At the end of the cheat, 
 *   the program must be back on normal track again; otherwise, it will receive a segmentation fault and get disqualified.
 * 
 * So, a program could complete the course in 72 picoseconds (saving 12 picoseconds) by cheating for the two moves 
 *   marked 1 and 2:
 * 
 * ###############
 * #...#...12....#
 * #.#.#.#.#.###.#
 * #S#...#.#.#...#
 * #######.#.#.###
 * #######.#.#...#
 * #######.#.###.#
 * ###..E#...#...#
 * ###.#######.###
 * #...###...#...#
 * #.#####.#.###.#
 * #.#...#.#.#...#
 * #.#.#.#.#.#.###
 * #...#...#...###
 * ###############
 * 
 * Or, a program could complete the course in 64 picoseconds (saving 20 picoseconds) by cheating for the two moves marked 1 and 2:
 * 
 * ###############
 * #...#...#.....#
 * #.#.#.#.#.###.#
 * #S#...#.#.#...#
 * #######.#.#.###
 * #######.#.#...#
 * #######.#.###.#
 * ###..E#...12..#
 * ###.#######.###
 * #...###...#...#
 * #.#####.#.###.#
 * #.#...#.#.#...#
 * #.#.#.#.#.#.###
 * #...#...#...###
 * ###############
 * 
 * This cheat saves 38 picoseconds:
 * 
 * ###############
 * #...#...#.....#
 * #.#.#.#.#.###.#
 * #S#...#.#.#...#
 * #######.#.#.###
 * #######.#.#...#
 * #######.#.###.#
 * ###..E#...#...#
 * ###.####1##.###
 * #...###.2.#...#
 * #.#####.#.###.#
 * #.#...#.#.#...#
 * #.#.#.#.#.#.###
 * #...#...#...###
 * ###############
 * 
 * This cheat saves 64 picoseconds and takes the program directly to the end:
 * 
 * ###############
 * #...#...#.....#
 * #.#.#.#.#.###.#
 * #S#...#.#.#...#
 * #######.#.#.###
 * #######.#.#...#
 * #######.#.###.#
 * ###..21...#...#
 * ###.#######.###
 * #...###...#...#
 * #.#####.#.###.#
 * #.#...#.#.#...#
 * #.#.#.#.#.#.###
 * #...#...#...###
 * ###############
 * 
 * Each cheat has a distinct start position (the position where the cheat is activated, just before the first move that is allowed to go through walls) and end position; cheats are uniquely identified by their start position and end position.
 * 
 * In this example, the total number of cheats (grouped by the amount of time they save) are as follows:
 * 
 *     There are 14 cheats that save 2 picoseconds.
 *     There are 14 cheats that save 4 picoseconds.
 *     There are 2 cheats that save 6 picoseconds.
 *     There are 4 cheats that save 8 picoseconds.
 *     There are 2 cheats that save 10 picoseconds.
 *     There are 3 cheats that save 12 picoseconds.
 *     There is one cheat that saves 20 picoseconds.
 *     There is one cheat that saves 36 picoseconds.
 *     There is one cheat that saves 38 picoseconds.
 *     There is one cheat that saves 40 picoseconds.
 *     There is one cheat that saves 64 picoseconds.
 * 
 * You aren't sure what the conditions of the racetrack will be like, so to give yourself as many options as possible, you'll need a list of the best cheats. How many cheats would save you at least 100 picoseconds?
 * 
 * Your puzzle answer was 1317.
 */
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
    
    Object calculate(FuncList<String> lines, int allowCheat, int atLeastSaved) {
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
        var result = calculate(lines, 2, 100);
        println("result: " + result);
        assertAsString("0", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines, 2, 100);
        println("result: " + result);
        assertAsString("1317", result);
    }
    
}
