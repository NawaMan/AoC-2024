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
        
        
        return selecteds.size();
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
