package day15;

import static functionalj.stream.intstream.IntStreamPlus.range;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

/**
 * --- Day 15: Warehouse Woes ---
 * 
 * You appear back inside your own mini submarine! Each Historian drives their mini submarine in a different direction; 
 *   maybe the Chief has his own submarine down here somewhere as well?
 * 
 * You look up to see a vast school of lanternfish swimming past you. On closer inspection, they seem quite anxious, 
 *   so you drive your mini submarine over to see if you can help.
 * 
 * Because lanternfish populations grow rapidly, they need a lot of food, and that food needs to be stored somewhere. 
 *   That's why these lanternfish have built elaborate warehouse complexes operated by robots!
 * 
 * These lanternfish seem so anxious because they have lost control of the robot that operates one of their most 
 *   important warehouses! It is currently running amok, pushing around boxes in the warehouse with no regard for 
 *   lanternfish logistics or lanternfish inventory management strategies.
 * 
 * Right now, none of the lanternfish are brave enough to swim up to an unpredictable robot so they could shut it off. 
 *   However, if you could anticipate the robot's movements, maybe they could find a safe option.
 * 
 * The lanternfish already have a map of the warehouse and a list of movements the robot will attempt to make (your 
 *   puzzle input). The problem is that the movements will sometimes fail as boxes are shifted around, making the actual 
 *   movements of the robot difficult to predict.
 * 
 * For example:
 * 
 * ##########
 * #..O..O.O#
 * #......O.#
 * #.OO..O.O#
 * #..O@..O.#
 * #O#..O...#
 * #O..O..O.#
 * #.OO.O.OO#
 * #....O...#
 * ##########
 * 
 * <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
 * vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
 * ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
 * <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
 * ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
 * ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
 * >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
 * <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
 * ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
 * v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
 * 
 * As the robot (@) attempts to move, if there are any boxes (O) in the way, the robot will also attempt to push those 
 *   boxes. However, if this action would cause the robot or a box to move into a wall (#), nothing moves instead, 
 *   including the robot. The initial positions of these are shown on the map at the top of the document 
 *   the lanternfish gave you.
 * 
 * The rest of the document describes the moves (^ for up, v for down, < for left, > for right) that the robot will 
 * attempt to make, in order. (The moves form a single giant sequence; they are broken into multiple lines just to make 
 * copy-pasting easier. Newlines within the move sequence should be ignored.)
 * 
 * Here is a smaller example to get started:
 * 
 * ########
 * #..O.O.#
 * ##@.O..#
 * #...O..#
 * #.#.O..#
 * #...O..#
 * #......#
 * ########
 * 
 * <^^>>>vv<v>>v<<
 * 
 * Were the robot to attempt the given sequence of moves, it would push around the boxes as follows:
 * 
 * Initial state:
 * ########
 * #..O.O.#
 * ##@.O..#
 * #...O..#
 * #.#.O..#
 * #...O..#
 * #......#
 * ########
 * 
 * Move <:
 * ########
 * #..O.O.#
 * ##@.O..#
 * #...O..#
 * #.#.O..#
 * #...O..#
 * #......#
 * ########
 * 
 * Move ^:
 * ########
 * #.@O.O.#
 * ##..O..#
 * #...O..#
 * #.#.O..#
 * #...O..#
 * #......#
 * ########
 * 
 * Move ^:
 * ########
 * #.@O.O.#
 * ##..O..#
 * #...O..#
 * #.#.O..#
 * #...O..#
 * #......#
 * ########
 * 
 * Move >:
 * ########
 * #..@OO.#
 * ##..O..#
 * #...O..#
 * #.#.O..#
 * #...O..#
 * #......#
 * ########
 * 
 * Move >:
 * ########
 * #...@OO#
 * ##..O..#
 * #...O..#
 * #.#.O..#
 * #...O..#
 * #......#
 * ########
 * 
 * Move >:
 * ########
 * #...@OO#
 * ##..O..#
 * #...O..#
 * #.#.O..#
 * #...O..#
 * #......#
 * ########
 * 
 * Move v:
 * ########
 * #....OO#
 * ##..@..#
 * #...O..#
 * #.#.O..#
 * #...O..#
 * #...O..#
 * ########
 * 
 * Move v:
 * ########
 * #....OO#
 * ##..@..#
 * #...O..#
 * #.#.O..#
 * #...O..#
 * #...O..#
 * ########
 * 
 * Move <:
 * ########
 * #....OO#
 * ##.@...#
 * #...O..#
 * #.#.O..#
 * #...O..#
 * #...O..#
 * ########
 * 
 * Move v:
 * ########
 * #....OO#
 * ##.....#
 * #..@O..#
 * #.#.O..#
 * #...O..#
 * #...O..#
 * ########
 * 
 * Move >:
 * ########
 * #....OO#
 * ##.....#
 * #...@O.#
 * #.#.O..#
 * #...O..#
 * #...O..#
 * ########
 * 
 * Move >:
 * ########
 * #....OO#
 * ##.....#
 * #....@O#
 * #.#.O..#
 * #...O..#
 * #...O..#
 * ########
 * 
 * Move v:
 * ########
 * #....OO#
 * ##.....#
 * #.....O#
 * #.#.O@.#
 * #...O..#
 * #...O..#
 * ########
 * 
 * Move <:
 * ########
 * #....OO#
 * ##.....#
 * #.....O#
 * #.#O@..#
 * #...O..#
 * #...O..#
 * ########
 * 
 * Move <:
 * ########
 * #....OO#
 * ##.....#
 * #.....O#
 * #.#O@..#
 * #...O..#
 * #...O..#
 * ########
 * 
 * The larger example has many more moves; after the robot has finished those moves, the warehouse would look like this:
 * 
 * ##########
 * #.O.O.OOO#
 * #........#
 * #OO......#
 * #OO@.....#
 * #O#.....O#
 * #O.....OO#
 * #O.....OO#
 * #OO....OO#
 * ##########
 * 
 * The lanternfish use their own custom Goods Positioning System (GPS for short) to track the locations of the boxes. 
 *   The GPS coordinate of a box is equal to 100 times its distance from the top edge of the map plus its distance from 
 *   the left edge of the map. (This process does not stop at wall tiles; measure all the way to the edges of the map.)
 * 
 * So, the box shown below has a distance of 1 from the top edge of the map and 4 from the left edge of the map, 
 *   resulting in a GPS coordinate of 100 * 1 + 4 = 104.
 * 
 * #######
 * #...O..
 * #......
 * 
 * The lanternfish would like to know the sum of all boxes' GPS coordinates after the robot finishes moving. 
 *   In the larger example, the sum of all boxes' GPS coordinates is 10092. In the smaller example, the sum is 2028.
 * 
 * Predict the motion of the robot and boxes in the warehouse. After the robot is finished moving, what is the sum of 
 *   all boxes' GPS coordinates?
 * 
 * Your puzzle answer was 1463512.
 */
public class Day15Part1Test extends BaseTest {
    
    record Grid(FuncList<String> lines) {
        char charAt(int row, int col) {
            if (row < 0 || row >= lines.size())            return ' ';
            if (col < 0 || col >= lines.get(row).length()) return ' ';
            return lines.get(row).charAt(col);
        }
    }
    
    class Warehouse {
        
        Grid grid;
        int  width;
        int  height;
        char[][] state;
        
        int robotR;
        int robotC;
        
        Warehouse(Grid grid) {
            this.grid   = grid;
            this.width  = grid.lines.get(0).length();
            this.height = grid.lines.size();
            this.state  = new char[height][];
            for (int r = 0; r < height; r++) {
                this.state[r] = new char[width];
                for (int c = 0; c < width; c++) {
                    this.state[r][c] = grid.charAt(r, c);
                    if (this.state[r][c] == '@') {
                        this.state[r][c] = '.';
                        this.robotR = r;
                        this.robotC = c;
                    }
                }
            }
        }
        
        void moveLeft() {
            int startMove = robotC;
            for (int c = robotC - 1; c > 0; c--) {
                if (this.state[robotR][c] == '#') {
                    return;
                }
                if (this.state[robotR][c] == '.') {
                    startMove = c;
                    break;
                }
            }
            if (startMove != robotC) {
                for (int c = startMove; c < robotC; c++) {
                    this.state[robotR][c] = this.state[robotR][c + 1];
                }
                robotC--;
            }
        }
        
        void moveRight() {
            int startMove = robotC;
            for (int c = robotC + 1; c < width; c++) {
                if (this.state[robotR][c] == '#') {
                    return;
                }
                if (this.state[robotR][c] == '.') {
                    startMove = c;
                    break;
                }
            }
            if (startMove != robotC) {
                for (int c = startMove; c > robotC; c--) {
                    this.state[robotR][c] = this.state[robotR][c - 1];
                }
                robotC++;
            }
        }
        
        void moveUp() {
            int startMove = robotR;
            for (int r = robotR - 1; r > 0; r--) {
                if (this.state[r][robotC] == '#') {
                    return;
                }
                if (this.state[r][robotC] == '.') {
                    startMove = r;
                    break;
                }
            }
            if (startMove != robotR) {
                for (int r = startMove; r < robotR; r++) {
                    this.state[r][robotC] = this.state[r + 1][robotC];
                }
                robotR--;
            }
        }
        
        void moveDown() {
            int startMove = robotR;
            for (int r = robotR + 1; r < height; r++) {
                if (this.state[r][robotC] == '#') {
                    return;
                }
                if (this.state[r][robotC] == '.') {
                    startMove = r;
                    break;
                }
            }
            if (startMove != robotR) {
                for (int r = startMove; r > robotR; r--) {
                    this.state[r][robotC] = this.state[r - 1][robotC];
                }
                robotR++;
            }
        }
        
        long sumGPS() {
            return range(0, height)
                    .flatMap(r -> {
                        return range(0, width)
                                .filter(c -> this.state[r][c] == 'O')
                                .map   (c -> 100 * r + c);
                    })
                    .asLongStream()
                    .sum();
        }
        
        public String toString() {
            var str = new StringBuilder();
            for (int r = 0; r < height; r++) {
                var chars = this.state[r];
                if (r == robotR) {
                    chars[robotC] = '@';
                }
                var line = new String(this.state[r]);
                if (r == robotR) {
                    chars[robotC] = '.';
                }
                if (!str.isEmpty()) {
                    str.append("\n");
                }
                str.append(line);
            }
            return str.toString();
        }
    }
    
    Object calculate(FuncList<String> lines) {
        var grid      = new Grid(lines.acceptUntil(""::equals));
        var warehouse = new Warehouse(grid);
        var sequence  = lines.skipUntil(""::equals).reduce((a, b) -> a + b).get();
        for (int i = 0; i < sequence.length(); i++) {
            char ch = sequence.charAt(i);
                switch (ch) {
                case '^': { warehouse.moveUp();    continue; }
                case 'v': { warehouse.moveDown();  continue; }
                case '>': { warehouse.moveRight(); continue; }
                case '<': { warehouse.moveLeft();  continue; }
            }
        }
        return warehouse.sumGPS();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("10092", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("1463512", result);
    }
    
}
