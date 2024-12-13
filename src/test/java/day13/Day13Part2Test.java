package day13;

import static day13.Day13Part1Test.newGame;
import static functionalj.function.Func.f;
import static functionalj.stream.intstream.IntStreamPlus.loop;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.promise.DeferAction;

public class Day13Part2Test extends BaseTest {
    
    static final long PRIZE_OFFSET = 10000000000000L;
    
    // Must of the code is in part 1 : Day13Part1Test
    
    Object calulate(FuncList<String> lines) throws Exception {
        var buttonCost  = new AB(3, 1);
        var prizeAdjust = PRIZE_OFFSET;
        var segments = lines.segment(4).map(FuncList::toCache).cache();
        return loop(segments.size())
                .boxed()
                .map(i -> newGame(segments.get((int)i), buttonCost, prizeAdjust))
                .map(f(Game::minCost).defer())
                .map(DeferAction::start)
                .sumToLong(promise -> promise.getResult().get());
    }
    
    //== Test ==
    
    @Ignore
    @Test
    public void testProd() throws Exception {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("74478585072604", result);
    }
    
}
