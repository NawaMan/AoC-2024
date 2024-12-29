package day13;

import static functionalj.stream.intstream.IntStreamPlus.loop;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.function.LongUnaryOperator;
import java.util.regex.Pattern;

import org.junit.Test;

import common.BaseTest;
import functionalj.functions.StrFuncs;
import functionalj.lens.lenses.LongToLongAccessPrimitive;
import functionalj.list.FuncList;
import functionalj.types.Struct;

/**
 * --- Day 13: Claw Contraption ---
 * 
 * Next up: the lobby of a resort on a tropical island. The Historians take a moment to admire the hexagonal floor tiles
 *   before spreading out.
 * 
 * Fortunately, it looks like the resort has a new arcade! Maybe you can win some prizes from the claw machines?
 * 
 * The claw machines here are a little unusual. Instead of a joystick or directional buttons to control the claw, these 
 *   machines have two buttons labeled A and B. Worse, you can't just put in a token and play; it costs 3 tokens to push
 *   the A button and 1 token to push the B button.
 * 
 * With a little experimentation, you figure out that each machine's buttons are configured to move the claw a specific
 *   amount to the right (along the X axis) and a specific amount forward (along the Y axis) each time that button is 
 *   pressed.
 * 
 * Each machine contains one prize; to win the prize, the claw must be positioned exactly above the prize on both the X 
 *   and Y axes.
 * 
 * You wonder: what is the smallest number of tokens you would have to spend to win as many prizes as possible? You 
 *   assemble a list of every machine's button behavior and prize location (your puzzle input). For example:
 * 
 * Button A: X+94, Y+34
 * Button B: X+22, Y+67
 * Prize: X=8400, Y=5400
 * 
 * Button A: X+26, Y+66
 * Button B: X+67, Y+21
 * Prize: X=12748, Y=12176
 * 
 * Button A: X+17, Y+86
 * Button B: X+84, Y+37
 * Prize: X=7870, Y=6450
 * 
 * Button A: X+69, Y+23
 * Button B: X+27, Y+71
 * Prize: X=18641, Y=10279
 * 
 * This list describes the button configuration and prize location of four different claw machines.
 * 
 * For now, consider just the first claw machine in the list:
 * 
 *     Pushing the machine's A button would move the claw 94 units along the X axis and 34 units along the Y axis.
 *     Pushing the B button would move the claw 22 units along the X axis and 67 units along the Y axis.
 *     The prize is located at X=8400, Y=5400; this means that from the claw's initial position, it would need to move 
 *       exactly 8400 units along the X axis and exactly 5400 units along the Y axis to be perfectly aligned with 
 *       the prize in this machine.
 * 
 * The cheapest way to win the prize is by pushing the A button 80 times and the B button 40 times. This would line up 
 *   the claw along the X axis (because 80*94 + 40*22 = 8400) and along the Y axis (because 80*34 + 40*67 = 5400). 
 *   Doing this would cost 80*3 tokens for the A presses and 40*1 for the B presses, a total of 280 tokens.
 * 
 * For the second and fourth claw machines, there is no combination of A and B presses that will ever win a prize.
 * 
 * For the third claw machine, the cheapest way to win the prize is by pushing the A button 38 times and the B button 
 *   86 times. Doing this would cost a total of 200 tokens.
 * 
 * So, the most prizes you could possibly win is two; the minimum tokens you would have to spend to win all (two) prizes
 *   is 480.
 * 
 * You estimate that each button would need to be pressed no more than 100 times to win a prize. How else would someone 
 *   be expected to play?
 * 
 * Figure out how to win as many prizes as possible. What is the fewest tokens you would have to spend to win 
 *   all possible prizes?
 * 
 * Your puzzle answer was 39748.
 */
public class Day13Part1Test extends BaseTest {
    
    @Struct void AB(long a, long b) {}
    @Struct void XY(long x, long y) {}
    
    static XY newXY(String line) {
        return newXY(line, theLong);
    }
    static XY newXY(String line, LongToLongAccessPrimitive adjustment) {
        var numbers  = StrFuncs.grab(line, Pattern.compile("[0-9]+")).mapToLong(parseLong);
        var parsedXY = new XY(numbers.get(0), numbers.get(1));
        return adjustXL(parsedXY, adjustment);
    }
    static XY adjustXL(XY xy, LongUnaryOperator mapper) {
        return new XY(mapper.applyAsLong(xy.x), mapper.applyAsLong(xy.y)); 
    }
    
    @Struct
    interface GameSpec {
        XY buttonA();
        XY buttonB();
        XY prize();
        AB cost();
        
        default long maxA() {
            return max(prize().x / buttonA().x, buttonB().y / buttonA().y) + 1L;
        }
        default AB guessAB(long a) {
            return new AB(a, (prize().x - a*buttonA().x) / buttonB().x);
        }
        default long costOf(AB ab) {
            return ab.a*cost().a + ab.b*cost().b;
        }
        default long minCost1() {
            var b = (buttonA().y()*prize().x()   - buttonA().x*prize().y())
                  / (buttonA().y()*buttonB().x() - buttonA().x*buttonB().y());
            var a = (prize().x() - buttonB().x()*b) / buttonA().x();
            
            var isValid = isValid(b, a);
            return isValid ? ((cost().a()*a + cost().b()*b)) : Long.MAX_VALUE;
        }
        default long minCost2() {
            var a = (buttonB().y*prize().x   - buttonB().x*prize().y())
                  / (buttonA().x*buttonB().y - buttonA().y*buttonB().x());
            var b = (prize().x() - buttonA().x()*a) / buttonB().x();
            
            var isValid = isValid(b, a);
            return isValid ? ((cost().a()*a + cost().b()*b)) : Long.MAX_VALUE;
        }
        default long minCost() {
            var min = min(minCost1(), minCost2());
            return (min == Long.MAX_VALUE) ? 0L : min;
        }
        default boolean isValid(long b, long a) {
            return (buttonA().x()*a + buttonB().x()*b) == prize().x()
                && (buttonA().y()*a + buttonB().y()*b) == prize().y();
        }
    }
    
    static Game newGame(FuncList<String> lines, AB cost, long prizeAdjust) {
        return new Game(
                    newXY(lines.get(0)),
                    newXY(lines.get(1)),
                    newXY(lines.get(2), theLong.plus(prizeAdjust)),
                    cost);
    }
    
    Object calculate(FuncList<String> lines) throws Exception {
        var buttonCost  = new AB(3, 1);
        var prizeAdjust = 0L;
        var segments    = lines.segment(4).map(FuncList::toCache).cache();
        return loop(segments.size())
                .boxed()
                .map(i -> newGame(segments.get((int)i), buttonCost, prizeAdjust))
                .sumToLong(Game::minCost);
    }
    
    //== Test ==
    
    @Test
    public void testExample() throws Exception {
        var start = System.currentTimeMillis();
        var lines  = readAllLines();
        var result = calculate(lines);
        println("Run for: " + (System.currentTimeMillis() - start) + "ms");
        println("result: " + result);
        assertAsString("480", result);
    }
    
    @Test
    public void testProd() throws Exception {
        var start = System.currentTimeMillis();
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        println("Run for: " + (System.currentTimeMillis() - start) + "ms");
        assertAsString("39748", result);
    }
    
}
