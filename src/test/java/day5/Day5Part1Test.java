package day5;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

public class Day5Part1Test extends BaseTest {
    
    Object calulate(FuncList<String> lines) {
        var firstSection = lines.acceptUntil(""::equals);
        var lastSection  = lines.skip       (firstSection.count() + 1);
        var rules        = firstSection.map(stringsToInts);
        var updates      = lastSection .map(stringsToInts);
        return updates
                .filter  (update -> correctOrderUpdate(rules, update))
                .mapToInt(update -> update.get(update.size() / 2))
                .sum();
    }
    
    boolean correctOrderUpdate(FuncList<IntFuncList> rules, IntFuncList update) {
        return rules.allMatch(rule -> {
            // update=75,47,61,53,29  intersect  rule=47|29  =>  matchOrder=[47,29]   <---  correct
            // update=75,47,61,53,29  intersect  rule=29|47  =>  matchOrder=[29,47]   <---  incorrect
            // update=75,47,61,53,29  intersect  rule=61|13  =>  matchOrder=[61]      <---  irrelevant
            var matchOrder = update.filterIn(rule);
            return (matchOrder.size() != 2)         // irrelevant?
                    || matchOrder.equals(rule);     // correct?
        });
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines().toCache();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("143", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines().toCache();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("5747", result);
    }
    
}
