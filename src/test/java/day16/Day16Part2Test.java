package day16;

import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import day16.Day16Part1Test.Graph;
import day16.Day16Part1Test.Grid;
import functionalj.list.FuncList;

/**
 * --- Part Two ---
 * 
 * Now that you know what the best paths look like, you can figure out the best spot to sit.
 * 
 * Every non-wall tile (S, ., or E) is equipped with places to sit along the edges of the tile. While determining which 
 *   of these tiles would be the best spot to sit depends on a whole bunch of factors (how comfortable the seats are, 
 *   how far away the bathrooms are, whether there's a pillar blocking your view, etc.), the most important factor is 
 *   whether the tile is on one of the best paths through the maze. If you sit somewhere else, you'd miss all the action!
 * 
 * So, you'll need to determine which tiles are part of any best path through the maze, including the S and E tiles.
 * 
 * In the first example, there are 45 tiles (marked O) that are part of at least one of the various best paths through 
 *   the maze:
 * 
 * ###############
 * #.......#....O#
 * #.#.###.#.###O#
 * #.....#.#...#O#
 * #.###.#####.#O#
 * #.#.#.......#O#
 * #.#.#####.###O#
 * #..OOOOOOOOO#O#
 * ###O#O#####O#O#
 * #OOO#O....#O#O#
 * #O#O#O###.#O#O#
 * #OOOOO#...#O#O#
 * #O###.#.#.#O#O#
 * #O..#.....#OOO#
 * ###############
 * 
 * In the second example, there are 64 tiles that are part of at least one of the best paths:
 * 
 * #################
 * #...#...#...#..O#
 * #.#.#.#.#.#.#.#O#
 * #.#.#.#...#...#O#
 * #.#.#.#.###.#.#O#
 * #OOO#.#.#.....#O#
 * #O#O#.#.#.#####O#
 * #O#O..#.#.#OOOOO#
 * #O#O#####.#O###O#
 * #O#O#..OOOOO#OOO#
 * #O#O###O#####O###
 * #O#O#OOO#..OOO#.#
 * #O#O#O#####O###.#
 * #O#O#OOOOOOO..#.#
 * #O#O#O#########.#
 * #O#OOO..........#
 * #################
 * 
 * Analyze your map further. How many tiles are part of at least one of the best paths through the maze?
 */
public class Day16Part2Test extends BaseTest {
    
    Object calculate(FuncList<String> lines) {
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
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("64", result);
    }
    
    @Ignore
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
}
