package day13;

import static day13.Day13Part1Test.newGame;
//import static day13.Day13Part1Test.newGame;
import static functionalj.function.Func.f;
import static functionalj.stream.intstream.IntStreamPlus.loop;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

/**
 * As you go to win the first prize, you discover that the claw is nowhere near where you expected it would be. 
 *   Due to a unit conversion error in your measurements, the position of every prize is actually 10000000000000 higher 
 *   on both the X and Y axis!
 * 
 * Add 10000000000000 to the X and Y position of every prize. After making this change, the example above would now look
 *   like this:
 * 
 * Button A: X+94, Y+34
 * Button B: X+22, Y+67
 * Prize: X=10000000008400, Y=10000000005400
 * 
 * Button A: X+26, Y+66
 * Button B: X+67, Y+21
 * Prize: X=10000000012748, Y=10000000012176
 * 
 * Button A: X+17, Y+86
 * Button B: X+84, Y+37
 * Prize: X=10000000007870, Y=10000000006450
 * 
 * Button A: X+69, Y+23
 * Button B: X+27, Y+71
 * Prize: X=10000000018641, Y=10000000010279
 * 
 * Now, it is only possible to win a prize on the second and fourth claw machines. Unfortunately, it will take many more
 *   than 100 presses to do so.
 * 
 * Using the corrected prize coordinates, figure out how to win as many prizes as possible. What is the fewest tokens 
 *   you would have to spend to win all possible prizes?
 * 
 * Your puzzle answer was 74478585072604.
 */
public class Day13Part2Test extends BaseTest {
    
    static final long PRIZE_OFFSET = 10000000000000L;
    
    // Most of the code is in part 1 : Day13Part1Test
    
    Object calculate(FuncList<String> lines) throws Exception {
        var buttonCost  = new AB(3, 1);
        var prizeAdjust = PRIZE_OFFSET;
        var segments    = lines.segment(4).map(FuncList::toCache).cache();
        return loop(segments.size())
                .boxed    ()
                .map      (i -> newGame(segments.get((int)i), buttonCost, prizeAdjust))
                .spawn    (f(Game::minCost).defer())
                .sumToLong(promise -> promise.getResult().get());
    }
    
    //== Test ==
    
    @Test
    public void testProd() throws Exception {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("74478585072604", result);
    }
    
}
