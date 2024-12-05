package day5;

import static functionalj.functions.StrFuncs.grab;
import static java.lang.Integer.parseInt;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day5Part1Test extends BaseTest {
    
    Object calulate(FuncList<String> lines) {
        var firstSection = lines.acceptUntil(""::equals);
        var lastSection  = lines.skip       (firstSection.count() + 1);
        var rules        = firstSection.map(grab("[0-9]+"));
        var updates      = lastSection .map(grab("[0-9]+"));
        return updates
                .filter  (update -> correctOrderUpdate(rules, update))
                .map     (update -> update.get(update.size() / 2))
                .mapToInt(middle -> parseInt(middle))
                .sum();
    }
    
    private boolean correctOrderUpdate(FuncList<FuncList<String>> rules, FuncList<String> update) {
        return rules.allMatch(rule -> {
            // update=75,47,61,53,29  intersect  rule=47|29  =>  matchOrder=[47,29]   <---  correct
            // update=75,47,61,53,29  intersect  rule=61|13  =>  matchOrder=[61]      <---  incorrect
            var matchOrder = update.filterIn(rule);
            return (matchOrder.size() != 2)
                    || matchOrder.equals(rule);
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
