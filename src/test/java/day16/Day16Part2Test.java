package day16;

import static functionalj.functions.StrFuncs.*;
import static functionalj.list.intlist.IntFuncList.range;

import java.util.HashSet;
import java.util.function.Function;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import day16.Day16Part1Test.Graph;
import day16.Day16Part1Test.Grid;
import day16.Day16Part1Test.Position;
import functionalj.list.FuncList;
import functionalj.map.FuncMap;
import functionalj.map.ImmutableFuncMap;
import functionalj.tuple.IntTuple2;
import functionalj.tuple.Tuple2;

public class Day16Part2Test extends BaseTest {
    
    
    Object calulate(FuncList<String> lines) {
        var grid = Grid.from(lines);
        println(grid);
        
        var graph = Graph.from(grid);
        var shortestPath = graph.shortestCostPath();
        var distance = shortestPath._1();
        var path     = shortestPath._2().cache();
        
        var selecteds = new HashSet<>(path);
        println(distance);
        
        var toBlockeds = findNodeToBeBlocked(grid, path);
        toBlockeds.forEach(this::println);
        
        for (var toBlocked : toBlockeds) {
            var exit    = toBlocked._1();
            var another = toBlocked._2();
            System.out.println("Block: " + exit);
            grid.data()[exit.row()][exit.col()] = "#";
            var shortestPathWithoutExit = Graph.from(grid).shortestCostPath();
            println("Distance: " + shortestPathWithoutExit._1());
            if (shortestPathWithoutExit._1() == distance) {
                selecteds.addAll(shortestPathWithoutExit._2());
            }
            if (another != null) {
                System.out.println("Also Block: " + exit);
                grid.data()[another.row()][another.col()] = "#";
                var shortestPathWithoutExitAndAnother = Graph.from(grid).shortestCostPath();
                println("Distance: " + shortestPathWithoutExitAndAnother._1());
                if (shortestPathWithoutExitAndAnother._1() == distance) {
                    selecteds.addAll(shortestPathWithoutExitAndAnother._2());
                }
                grid.data()[another.row()][another.col()] = ".";
            }
            grid.data()[exit.row()][exit.col()] = ".";
        }
        
        FuncList.from(selecteds)
        .segment(10)
        .forEach(println);
        

//        var display = grid.clone();
//        range(0, display.data().length).toCache().forEach(row -> {
//            range(0, display.data()[0].length).forEach(col -> {
//                var pos = new Po
//            });
//        });
        
        return selecteds.size();
    }

    private void tryBlock(Grid grid, Graph graph, Long distance, HashSet<Position> selecteds, Position exit, Position another) {
        grid.data()[exit.row()][exit.col()] = "#";
        var shortestPathWithoutExit = graph.shortestCostPath();
        if (shortestPathWithoutExit._1() == distance) {
            selecteds.addAll(shortestPathWithoutExit._2());
        }
        if (another != null) {
            grid.data()[another.row()][another.col()] = "#";
            var shortestPathWithoutExitAndAnother = graph.shortestCostPath();
            if (shortestPathWithoutExitAndAnother._1() == distance) {
                selecteds.addAll(shortestPathWithoutExitAndAnother._2());
            }
            grid.data()[another.row()][another.col()] = ".";
        }
        grid.data()[exit.row()][exit.col()] = ".";
    }

    FuncList<Tuple2<Position, Position>> findNodeToBeBlocked(Grid grid, FuncList<Position> path) {
        var visiteds = path.mapWithIndex().toMap(IntTuple2::_2).mapValue(IntTuple2::_1).toImmutableMap();
        println(visiteds);
        var junctions
                = path
                // Exclude start and end.
                .skip(1).limit(path.size() - 2)
                // Position with walkable neighbours
                .map   (pos -> pos.neighbours().filter(n -> grid.at(n).charAt(0) == '.').cache ())
                // ... more than 2.
                .filter(n   -> n.size() > 2)
                .cache ();
        var toBlockeds = junctions.map(neighboursToBlock(visiteds)).cache();
        return toBlockeds;
    }

    Function<FuncList<Position>, Tuple2<Position, Position>> neighboursToBlock(FuncMap<Position, Integer> visiteds) {
        return nodes -> {
            // The idea is to block the current path at junctions and see if we still get the shortest path.
            //
            // There are two cases:                        |
            //   1) with more option, and        e.g.: ===>+===>    ... we should try to block the exit.
            //
            //                                             |
            //   2) with two more options.       e.g.: ===>+===>    ... we should try to block the exit
            //                                             |            and they block one of the option as well.
            
            // Case 1: The node in the later order (in path) is the exit
            var exit = nodes.filter(node -> visiteds.get(node) != null).sortedBy(visiteds::get).skip(1).get(0);
            // Case 2: Any one of the option can be block. So we try to get the second one.
            var another = nodes.filter(node -> visiteds.get(node) == null).skip(1).findFirst().orElse(null);
            
            return Tuple2.of(exit, another);
        };
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
    @Ignore
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
}
