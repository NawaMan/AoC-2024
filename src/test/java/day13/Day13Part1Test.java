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
    
    Object calulate(FuncList<String> lines) throws Exception {
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
        var result = calulate(lines);
        println("Run for: " + (System.currentTimeMillis() - start) + "ms");
        println("result: " + result);
        assertAsString("480", result);
    }
    
    @Test
    public void testProd() throws Exception {
        var start = System.currentTimeMillis();
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        println("Run for: " + (System.currentTimeMillis() - start) + "ms");
        assertAsString("39748", result);
    }
    
}
