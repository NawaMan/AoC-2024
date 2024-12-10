package day10;

import static functionalj.list.intlist.IntFuncList.range;

import org.junit.Test;

import common.BaseTest;
import functionalj.function.Func3;
import functionalj.list.FuncList;

public class Day10Part2Test extends BaseTest {
    
    record Position(int row, int col) {}
    
    record Grid(FuncList<String> lines) {
        int at(int r, int c) {
            if (r < 0 || r >= lines.size())          return -1;
            if (c < 0 || c >= lines.get(r).length()) return -1;
            var ch = lines.get(r).charAt(c);
            return ((ch < '0' || ch > '9')) ? -1 : (ch - '0');
        }
        <T> FuncList<T> allPositions(Func3<Integer, Integer, Integer, T> mapper) {
            return range(0, lines.size()).toCache().flatMapToObj(row -> {
                return range(0, lines.get(0).length())
                        .toCache ()
                        .mapToObj(col -> mapper.apply(row, col, at(row, col)))
                        .excludeNull();
            });
        }
    }
    
    int countTrails(FuncList<String> lines) {
        var grid   = new Grid(lines);
        var starts = grid.allPositions((r, c, ch) -> (ch == 0) ? new Position(r, c): null);
        return starts.mapToInt(start -> seachForTailAt(grid, start, 0, 0, 0)).sum();
    }
    
    int seachForTailAt(Grid grid, Position pos, int diffRow, int diffCol, int nextLevel) {
        var nextPost = new Position(pos.row + diffRow, pos.col + diffCol);
        var h = grid.at(nextPost.row, nextPost.col);
        if (nextLevel != h) return 0;
        if (nextLevel == 9) return 1;
        return seachForTailAt(grid, nextPost,  1,  0, nextLevel + 1)
             + seachForTailAt(grid, nextPost, -1,  0, nextLevel + 1)
             + seachForTailAt(grid, nextPost,  0,  1, nextLevel + 1)
             + seachForTailAt(grid, nextPost,  0, -1, nextLevel + 1);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = countTrails(lines);
        println("result: " + result);
        assertAsString("81", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = countTrails(lines);
        println("result: " + result);
        assertAsString("928", result);
    }
    
}
