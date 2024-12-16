package day15;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.FuncListBuilder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

public class Day15Part2Test extends BaseTest {

    record Grid(FuncList<String> lines) {
        char charAt(int row, int col) {
            if (row < 0 || row >= lines.size())            return ' ';
            if (col < 0 || col >= lines.get(row).length()) return ' ';
            return lines.get(row).charAt(col);
        }
    }
    
    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    @Accessors(fluent = true)
    class Position {
        int r;
        int c;
        @Override
        public String toString() {
            return "Position(r=%d, c=%d)".formatted(r, c);
        }
    }

    
    class Warehouse {
        
        Grid grid;
        int width;
        int height;
        Position robot;
        FuncList<Position> goods;
        
        public Warehouse(FuncList<String> lines) {
            var expanded =
                    lines
                    .acceptUntil(""::equals)
                    .map(line -> line.replaceAll("#",   "##"))
                    .map(line -> line.replaceAll("\\.", ".."))
                    .map(line -> line.replaceAll("O",   "[]"))
                    .map(line -> line.replaceAll("@",   "@."))
                    .toFuncList();
            var goodsB       = new FuncListBuilder<Position>();
            var expandedGrid = new Grid(expanded);
            for (int r = 0; r < expandedGrid.lines.size(); r++) {
                for (int c = 0; c < expandedGrid.lines.get(0).length(); c++) {
                    var ch = expandedGrid.charAt(r, c);
                    switch(ch) {
                        case '@': { this.robot = new Position(r, c); break; }
                        case '[': { goodsB.add(new Position(r, c));  break; }
                    }
                }
            }
            
            var adjusted =
                lines
                .acceptUntil(""::equals)
                .map(line -> line.replaceAll("#",   "##"))
                .map(line -> line.replaceAll("\\.", ".."))
                .map(line -> line.replaceAll("O",   ".."))
                .map(line -> line.replaceAll("@",   ".."))
                .toFuncList();
            this.grid   = new Grid(adjusted);
            this.width  = expanded.get(0).length();
            this.height = expanded.size();
            this.goods  = goodsB.build();
        }
        
        public String toString() {
            var str = new StringBuilder();
            for (int r = 0; r < height; r++) {
                var row   = r;
                var chars = grid.lines.get(r).toCharArray();
                goods
                .filter (p -> p.r == row)
                .forEach(p -> {
                    chars[p.c + 0] = '[';
                    chars[p.c + 1] = ']';
                });
                
                if (r == robot.r) {
                    chars[robot.c + 0] = '@';
                }
                
                
                var line = new String(chars);
                if (!str.isEmpty()) {
                    str.append("\n");
                }
                str.append(line);
            }
            return str.toString();
        }
        
        boolean moveLeft(Position good) {
            if (grid.charAt(good.r, good.c - 1) == '#')
                return false;
            
            var front = goods.findFirst(g -> (g.r == good.r) && (g.c == good.c - 2));
            if (front.isPresent()) {
                var frontMoved = moveLeft(front.get());
                if (!frontMoved)
                    return false;
            }

            good.c -= 1;
            return true;
        }
        
        void moveLeft() {
            moveLeft(robot);
        }
        
        boolean moveRight(Position good, boolean isGood) {
            if (grid.charAt(good.r, good.c + (isGood ? 2 : 1)) == '#')
                return false;
            
            var front = goods.findFirst(g -> (g.r == good.r) && (g.c == good.c + (isGood ? 2 : 1)));
            if (front.isPresent()) {
                var frontMoved = moveRight(front.get(), true);
                if (!frontMoved)
                    return false;
            }

            good.c += 1;
            return true;
        }
        
        void moveRight() {
            moveRight(robot, false);
        }
        
        boolean moveUp(Position position, boolean isGood) {
            if ((grid.charAt(position.r - 1, position.c) == '#') || (isGood && (grid.charAt(position.r - 1, position.c + 1) == '#')))
                return false;
            
            if (isGood && (grid.charAt(position.r - 1, position.c + 1) == '#'))
                return false;
            
            var front = goods.findFirst(g -> (g.r == position.r - 1) && (((g.c == position.c) || (g.c + 1 == position.c))
                                                                       || (isGood && ((g.c == position.c + 1) || (g.c + 1 == position.c + 1)))));
            if (front.isPresent()) {
                var frontMoved = moveUp(front.get(), true);
                if (!frontMoved)
                    return false;
            }

            position.r -= 1;
            return true;
        }
        
        void moveUp() {
            moveUp(robot, false);
        }
        
        boolean moveDown(Position position, boolean isGood) {
            if (grid.charAt(position.r + 1, position.c) == '#')
                return false;
            
            if (isGood && (grid.charAt(position.r + 1, position.c + 1) == '#'))
                return false;
            
            var front = goods.findFirst(g -> (g.r == position.r + 1) && (            ((g.c == position.c    ) || (g.c + 1 == position.c    ))
                                                                       || (isGood && ((g.c == position.c + 1) || (g.c + 1 == position.c + 1)))));
            if (front.isPresent()) {
                var frontMoved = moveDown(front.get(), true);
                if (!frontMoved)
                    return false;
            }

            position.r += 1;
            return true;
        }
        
        void moveDown() {
            moveDown(robot, false);
        }

        public long sumGPS() {
            return goods.mapToLong(g -> g.r*100 + g.c).sum();
        }
    }
    
    
    Object calulate(FuncList<String> lines) {
        var warehouse = new Warehouse(lines);
        println(warehouse);

        println();
        println();
        lines.skipUntil(""::equals).forEach(println);
        println();
        println();
        
        var sequence = lines.skipUntil(""::equals).reduce((a, b) -> a + b).get();
        println(sequence);
        
        for (int i = 0; i < sequence.length(); i++) {
            println(warehouse);
            
            char ch = sequence.charAt(i);
            println("--| " + i + ": " + ch + " |--");
            switch (ch) {
                case '^': { warehouse.moveUp();    continue; }
                case 'v': { warehouse.moveDown();  continue; }
                case '>': { warehouse.moveRight(); continue; }
                case '<': { warehouse.moveLeft();  continue; }
            }
        }
        println(warehouse);
        
        return warehouse.sumGPS();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("9021", result);
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
