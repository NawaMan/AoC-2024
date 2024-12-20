package day20;

import static day20.Day20Part1Test.BLUE;
import static day20.Day20Part1Test.BOLD;
import static day20.Day20Part1Test.GREEN;
import static day20.Day20Part1Test.RED;
import static day20.Day20Part1Test.RESET;
import static day20.Day20Part1Test.YELLOW;
import static java.lang.Math.abs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import day20.Day20Part1Test.Graph;
import day20.Day20Part1Test.Grid;
import day20.Day20Part1Test.Position;
import functionalj.list.FuncList;
import functionalj.map.FuncMap;
import functionalj.tuple.Tuple2;

public class Day20Part2Test extends BaseTest {
    
    Object calulate(FuncList<String> lines) {
        var grid = Grid.from(lines);
        
        println(grid);
        
        var graph = Graph.from(grid);
//        graph.graphMap.entries().sortedBy(Map.Entry::getKey).forEach(println);;
        var shortestPath = graph.shortestCostPath(true);
        var shortestDist = shortestPath._1();
//        println(shortestPath);
        
        var path          = shortestPath._2();
        var nodeWithIndex = path.mapWithIndex();
        
//        println(nodeWithIndex);
        
        var invertGrid  = invertGrid(grid);
        var seenCuts    = new HashMap<Long, Set<String>>();
        var savedCounts = new HashMap<Long, AtomicInteger>();
        
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
                
                if (cheatTime <= 20 && savedTime >= 100)
                    count++;

////                
////                if (((onePos.row() == 3) && (onePos.col() == 1))
////                 && ((twoPos.row() == 7) && (twoPos.col() == 3))) {
//                    checkCheat(shortestPath, grid, invertGrid, onePos, twoPos, oneOrder, twoOrder, savedCounts, seenCuts);
////                }
            }
        }
        
//        FuncMap.from(savedCounts).entries().filter(entry -> entry.getKey() >= 50).sortedBy(Map.Entry::getKey).forEach(println);
//        
//        return FuncMap.from(savedCounts).entries().filter(entry -> entry.getKey() >= 100).map(Map.Entry::getValue).sumToInt(AtomicInteger::intValue);
        return count;
    }
    
    private Grid invertGrid(Grid grid) {
        var clone = grid.clone();
        for (var r = 0; r < clone.height(); r++) {
            for (var c = 0; c < clone.width(); c++) {
                var ch = clone.data()[r][c].charAt(0);
                clone.data()[r][c] = (ch == '.') ? "#" : ".";
            }
        }
        return clone;
    }

    private void checkCheat(Tuple2<Long, FuncList<Position>> shortestPath, Grid orgGrid, Grid invertGrid, Position onePos, Position twoPos, int oneOrder, int twoOrder, HashMap<Long, AtomicInteger> savedCounts, HashMap<Long, Set<String>>seenCuts) {
        var grid = new Grid(invertGrid.clone().data(), onePos, twoPos);
        grid.data()[onePos.row()][onePos.col()] = ".";
        grid.data()[twoPos.row()][twoPos.col()] = ".";
        
        var invGraph  = Graph.from(grid);
        var invShPath = invGraph.shortestCostPath(false);
        if (invShPath._1() > 20)
            return;
        if (invShPath._2().isEmpty())
            return;
        
        var newGrid = orgGrid.clone();
        invShPath._2().forEach(pos -> {
            newGrid.data()[pos.row()][pos.col()] = ".";
        });
        var newGraph  = Graph.from(newGrid);
        var newShPath = newGraph.shortestCostPath(false);
        var newShDist = newShPath._1();
        
        var saved = abs(shortestPath._1() - newShDist);
        if (saved >= 50) {
//            println(grid);
//            println(invShPath);
//            println("newGrid: ");
//            println(newGrid);
            var cuts = new HashSet<Position>();
            
            var display = orgGrid.clone();
            for (int r = 0; r < display.height(); r++) {
                for (int c = 0; c < display.width(); c++) {
                    var pos = new Position(r, c);
                    if ((display.data()[r][c].equals("#")) && newShPath._2().contains(pos)) {
                        cuts.add(pos);
                        display.data()[r][c] = BOLD + YELLOW + "+" + RESET;
                    } else if (shortestPath._2().contains(pos) && (!newShPath._2().contains(pos) || (display.data()[r][c].equals(".")))) {
                        display.data()[r][c] = BOLD + BLUE + "+" + RESET;
                    }
                }
            }
            display.data()[display.end().row()][display.end().col()]     = BOLD + GREEN + "@" + RESET;
            display.data()[display.start().row()][display.start().col()] = BOLD + RED + "@" + RESET;
            
            var enterExit = newShPath._2().mapWithIndex().filter(pair -> !cuts.contains(pair._2)).mapTwo().filter(ab -> (ab._2()._1 - ab._1()._1) > 1).flatMap(ab -> FuncList.of(ab._1()._2, ab._2()._2));
            
            seenCuts.putIfAbsent(saved, new HashSet<String>());
            if ((enterExit.size() == 2) && !seenCuts.get(saved).contains(enterExit.toString())) {
                println("onePos: " + onePos + ", twoPos: " + twoPos + ", saved: " + saved);
//                println(display);
//                println(enterExit);
//                println();
                
                seenCuts.get(saved).add(enterExit.toString());
                savedCounts.putIfAbsent(saved, new AtomicInteger());
                savedCounts.get(saved).incrementAndGet();
            }
        }
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
        assertAsString("982474", result);
    }
    
}
