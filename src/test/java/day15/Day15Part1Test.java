package day15;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

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
        int width;
        int height;
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
            var sum = 0L;
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    var ch = this.state[r][c];
                    if (ch == 'O') {
                        sum += 100*r + c;
                    }
                }
            }
            return sum;
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
    
    Object calulate(FuncList<String> lines) {
        var grid      = new Grid(lines.acceptUntil(""::equals));
        var warehouse = new Warehouse(grid);
        println(warehouse);
        
        var sequence = lines.skipUntil(""::equals).reduce((a, b) -> a + b).get();
        println(sequence);
        
        for (int i = 0; i < sequence.length(); i++) {
            char ch = sequence.charAt(i);
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
        assertAsString("10092", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("1463512", result);
    }
    
}
