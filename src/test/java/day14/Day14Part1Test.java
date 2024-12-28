package day14;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day14Part1Test extends BaseTest {
    
    record Robot(int x, int y, int vx, int vy) {
        Robot move(int step, int wide, int tall) {
            int newX = (x + step*vx) % wide;
            int newY = (y + step*vy) % tall;
            if (newX < 0) newX += wide;
            if (newY < 0) newY += tall;
            return new Robot(newX, newY, vx, vy);
        }
    }
    
    Object calculate(FuncList<String> lines, int wide, int tall, int step) {
        var robots = moveRobots(lines, wide, tall, step);
        draw(wide, tall, robots);
        return robots
                .filter(robot -> (robot.x != (wide / 2)) && (robot.y != (tall / 2)))
                .groupingBy(robot -> ("(" + (robot.x < wide / 2) + "," + (robot.y < tall / 2) + ")"))
                .mapValue(FuncList::size)
                .values()
                .mapToInt(i -> i)
                .product()
                .getAsInt();
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
                if ((i == (wide / 2)) || (j == (tall / 2))) {
                    System.out.print(" ");
                    continue;
                }
                
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
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines, 11, 7, 100);
        println("result: " + result);
        assertAsString("12", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines, 101, 103, 100);
        println("result: " + result);
        assertAsString("226548000", result);
    }
    
}
