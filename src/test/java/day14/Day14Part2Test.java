package day14;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

/**
 * --- Part Two ---
 * 
 * During the bathroom break, someone notices that these robots seem awfully similar to ones built and used at 
 *   the North Pole. If they're the same type of robots, they should have a hard-coded Easter egg: very rarely, most of 
 *   the robots should arrange themselves into a picture of a Christmas tree.
 * 
 * What is the fewest number of seconds that must elapse for the robots to display the Easter egg?
 * 
 * Your puzzle answer was 7753.
 */
public class Day14Part2Test extends BaseTest {
    
    record Robot(int x, int y, int vx, int vy) {
        Robot move(int step, int wide, int tall) {
            int newX = (x + step*vx) % wide;
            int newY = (y + step*vy) % tall;
            if (newX < 0) newX += wide;
            if (newY < 0) newY += tall;
            return new Robot(newX, newY, vx, vy);
        }
    }
    
    FuncList<Robot> moveRobots(FuncList<String> lines, int wide, int tall, int step) {
        return lines
                .map  (line  -> grab(regex("-?[0-9]+"), line).map(parseInt))
                .map  (list  -> new Robot(list.get(0), list.get(1), list.get(2), list.get(3)))
                .map  (robot -> robot.move(step, wide, tall))
                .cache();
    }
    
    void draw(int wide, int tall, FuncList<Robot> robots) {
        for (int j = 0; j < tall; j++) {
            for (int i = 0; i < wide; i++) {
                int I = i;
                int J = j;
                int size = robots.filter(robot -> (robot.x + "," + robot.y).equals(I + "," + J)).size();
                System.out.print(size);
            }
            System.out.println();
        }
    }
    
    //== Test ==
    
    @Test
    public void testProd() {
        int wide = 101;
        int tall = 103;
        
        var lines  = readAllLines();
        for (int i = 7753; i < 7754; i++) {
            var robots = moveRobots(lines, wide, tall, i);
            
            var hasLine
                    = robots.groupingBy(robot -> robot.x()).values().filter(list -> list.size() > 30).size() >= 2
                   && robots.groupingBy(robot -> robot.y()).values().filter(list -> list.size() > 30).size() >= 2;
            if (hasLine) {
                println("==| " + i + " |==");
                draw(wide, tall, robots);
                println("-----------------------------------------------------------------------------------------------------------");
                println();
            }
        }
    }
    
}
