package day15;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.FuncListBuilder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * --- Part Two ---
 * 
 * The lanternfish use your information to find a safe moment to swim in and turn off the malfunctioning robot! Just as 
 *   they start preparing a festival in your honor, reports start coming in that a second warehouse's robot is also 
 *   malfunctioning.
 * 
 * This warehouse's layout is surprisingly similar to the one you just helped. There is one key difference: everything 
 *   except the robot is twice as wide! The robot's list of movements doesn't change.
 * 
 * To get the wider warehouse's map, start with your original map and, for each tile, make the following changes:
 * 
 *     If the tile is #, the new map contains ## instead.
 *     If the tile is O, the new map contains [] instead.
 *     If the tile is ., the new map contains .. instead.
 *     If the tile is @, the new map contains @. instead.
 * 
 * This will produce a new warehouse map which is twice as wide and with wide boxes that are represented by []. 
 *   (The robot does not change size.)
 * 
 * The larger example from before would now look like this:
 * 
 * ####################
 * ##....[]....[]..[]##
 * ##............[]..##
 * ##..[][]....[]..[]##
 * ##....[]@.....[]..##
 * ##[]##....[]......##
 * ##[]....[]....[]..##
 * ##..[][]..[]..[][]##
 * ##........[]......##
 * ####################
 * 
 * Because boxes are now twice as wide but the robot is still the same size and speed, boxes can be aligned such that 
 *   they directly push two other boxes at once. For example, consider this situation:
 * 
 * #######
 * #...#.#
 * #.....#
 * #..OO@#
 * #..O..#
 * #.....#
 * #######
 * 
 * <vv<<^^<<^^
 * 
 * After appropriately resizing this map, the robot would push around these boxes as follows:
 * 
 * Initial state:
 * ##############
 * ##......##..##
 * ##..........##
 * ##....[][]@.##
 * ##....[]....##
 * ##..........##
 * ##############
 * 
 * Move <:
 * ##############
 * ##......##..##
 * ##..........##
 * ##...[][]@..##
 * ##....[]....##
 * ##..........##
 * ##############
 * 
 * Move v:
 * ##############
 * ##......##..##
 * ##..........##
 * ##...[][]...##
 * ##....[].@..##
 * ##..........##
 * ##############
 * 
 * Move v:
 * ##############
 * ##......##..##
 * ##..........##
 * ##...[][]...##
 * ##....[]....##
 * ##.......@..##
 * ##############
 * 
 * Move <:
 * ##############
 * ##......##..##
 * ##..........##
 * ##...[][]...##
 * ##....[]....##
 * ##......@...##
 * ##############
 * 
 * Move <:
 * ##############
 * ##......##..##
 * ##..........##
 * ##...[][]...##
 * ##....[]....##
 * ##.....@....##
 * ##############
 * 
 * Move ^:
 * ##############
 * ##......##..##
 * ##...[][]...##
 * ##....[]....##
 * ##.....@....##
 * ##..........##
 * ##############
 * 
 * Move ^:
 * ##############
 * ##......##..##
 * ##...[][]...##
 * ##....[]....##
 * ##.....@....##
 * ##..........##
 * ##############
 * 
 * Move <:
 * ##############
 * ##......##..##
 * ##...[][]...##
 * ##....[]....##
 * ##....@.....##
 * ##..........##
 * ##############
 * 
 * Move <:
 * ##############
 * ##......##..##
 * ##...[][]...##
 * ##....[]....##
 * ##...@......##
 * ##..........##
 * ##############
 * 
 * Move ^:
 * ##############
 * ##......##..##
 * ##...[][]...##
 * ##...@[]....##
 * ##..........##
 * ##..........##
 * ##############
 * 
 * Move ^:
 * ##############
 * ##...[].##..##
 * ##...@.[]...##
 * ##....[]....##
 * ##..........##
 * ##..........##
 * ##############
 * 
 * This warehouse also uses GPS to locate the boxes. 
 * For these larger boxes, distances are measured from the edge of the map to the closest edge of the box in question. 
 * So, the box shown below has a distance of 1 from the top edge of the map and 5 from the left edge of the map, 
 *   resulting in a GPS coordinate of 100 * 1 + 5 = 105.
 * 
 * ##########
 * ##...[]...
 * ##........
 * 
 * In the scaled-up version of the larger example from above, after the robot has finished all of its moves, 
 *   the warehouse would look like this:
 * 
 * ####################
 * ##[].......[].[][]##
 * ##[]...........[].##
 * ##[]........[][][]##
 * ##[]......[]....[]##
 * ##..##......[]....##
 * ##..[]............##
 * ##..@......[].[][]##
 * ##......[][]..[]..##
 * ####################
 * 
 * The sum of these boxes' GPS coordinates is 9021.
 * 
 * Predict the motion of the robot and boxes in this new, scaled-up warehouse. What is the sum of all boxes' final GPS 
 *   coordinates?
 */
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
        
        boolean moveUp(Position position, boolean isGood, boolean isCheckOnly) {
            if (grid.charAt(position.r - 1, position.c) == '#')
                return false;
            
            if (isGood && (grid.charAt(position.r - 1, position.c + 1) == '#'))
                return false;
            
            var fronts
                    = goods
                    .filter(g -> (g.r == position.r - 1) && (((g.c == position.c    ) || (g.c + 1 == position.c    ))
                              || (isGood                 &&  ((g.c == position.c + 1) || (g.c + 1 == position.c + 1)))))
                    .cache();
           if (fronts.size() != 0) {
               var frontMoved = fronts.allMatch(front -> moveUp(front, true, true));
                if (!frontMoved)
                    return false;
                
                fronts.forEach(front -> moveUp(front, true, false));
            }
           
            if (!isCheckOnly) {
                position.r -= 1;
            }
            return true;
        }
        
        void moveUp() {
            moveUp(robot, false, false);
        }
        
        boolean moveDown(Position position, boolean isGood, boolean isCheckOnly) {
            if (grid.charAt(position.r + 1, position.c) == '#')
                return false;
            
            if (isGood && (grid.charAt(position.r + 1, position.c + 1) == '#'))
                return false;
            
            var fronts
                    = goods
                    .filter(g -> (g.r == position.r + 1) && (((g.c == position.c    ) || (g.c + 1 == position.c    ))
                              || (isGood                 &&  ((g.c == position.c + 1) || (g.c + 1 == position.c + 1)))))
                    .cache();
            if (fronts.size() != 0) {
                var frontMoved = fronts.allMatch(front -> moveDown(front, true, true));
                if (!frontMoved)
                    return false;
                
                fronts.forEach(front -> moveDown(front, true, false));
            }
            
            if (!isCheckOnly) {
                position.r += 1;
            }
            
            return true;
        }
        
        void moveDown() {
            moveDown(robot, false, false);
        }
        
        public long sumGPS() {
            return goods.mapToLong(g -> (long)g.r*100L + (long)g.c).sum();
        }
    }
    
    
    Object calculate(FuncList<String> lines) {
        var warehouse = new Warehouse(lines);
        println(warehouse);
        println();
        
        var sequence = lines.skipUntil(""::equals).reduce((a, b) -> a + b).get();
        
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
        println();
        
        return warehouse.sumGPS();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("9021", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
}
