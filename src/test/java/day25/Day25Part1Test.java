package day25;

import static day25.Day25Part1Test.ChangeColumn.forKey;
import static day25.Day25Part1Test.ChangeColumn.forLock;

import org.junit.Test;

import common.BaseTest;
import functionalj.function.Func1;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

public class Day25Part1Test extends BaseTest {
    
    static final String base = "#####";
    
    static enum ChangeColumn {
        forLock(false),
        forKey(true);
        
        final boolean invertColumn;
        ChangeColumn(boolean invertColumn) {
            this.invertColumn = invertColumn;
        }
    }
    
    Object calculate(FuncList<String> lines) {
        var inputs = lines.prepend("");
        
        var locksOrKeys
                = inputs
                .query(theString.thatIsEmpty())
                .map  (t -> inputs.skip(t._1 + 1).limit(7)) // _1 is index.
                .cache();
        var locks
                = locksOrKeys
                .filter(theList.first().asString().thatEquals(base))
                .map   (toColumns(forLock))
                .cache ();
        var keys
                = locksOrKeys
                .filter(theList.last().asString().thatEquals(base))
                .map   (toColumns(forKey))
                .cache ();
        
        return locks.sumToInt(lock -> keys.exclude(key -> hasOverlap(lock, key)).size());
    }
    
    Func1<FuncList<String>, IntFuncList> toColumns(ChangeColumn change) {
        var invert = change.invertColumn;
        return pics -> {
            var nums = new int[pics.get(0).length()];
            for (int col = 0; col < pics.get(0).length(); col++) {
                for (int row = 0; row < pics.size() - 1; row++) {
                    if (pics.get(row).charAt(col) != pics.get(row + 1).charAt(col)) {
                        nums[col] = invert ? 5 - row : row;
                    }
                }
            }
            return IntFuncList.of(nums);
        };
    }
    
    boolean hasOverlap(IntFuncList lock, IntFuncList key) {
        return lock.zipWith(key, (a,b) -> a + b).anyMatch(col -> col > 5);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("3", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("2933", result);
    }
    
}
