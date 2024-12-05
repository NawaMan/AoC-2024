package day5;

import static functionalj.functions.StrFuncs.grab;
import static java.lang.Integer.parseInt;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day5Part2Test extends BaseTest {
    
    Object calulate(FuncList<String> lines) {
        var firstPart = lines.acceptUntil(""::equals);
        var lastPart  = lines.skip(firstPart.count() + 1);
        
        var rules = firstPart.map(grab("[0-9]+"));
        var updates = lastPart.map(grab("[0-9]+"));
        
        var incorrects = updates
        .filter(update -> {
            return !rules.allMatch(rule -> {
                var clone = update.toMutableList();
                clone.retainAll(rule);
                if (clone.size() != 2) {
                    return true;
                }
                
                return clone.toString().equals(rule.toString());
            });
        });
        
        return incorrects.mapToInt(update -> {
            var updateRules = rules.filter(rule -> {
                var clone = update.toMutableList();
                clone.retainAll(rule);
                return clone.size() == 2;
            });
            
            var middle = reorder(updateRules);
            return parseInt(middle);
        }).sum();
    }
    
    private String reorder(FuncList<FuncList<String>> updateRules) {
        var lastPages  = updateRules.map(rule -> rule.get(1));
        var firstPage  = updateRules.map(rule -> rule.get(0)).exclude(lastPages::contains).findFirst().get();
        var firstPages = updateRules.map(rule -> rule.get(0));
        var lastPage   = updateRules.map(rule -> rule.get(1)).exclude(firstPages::contains).findFirst().get();
        
        var shrinkPages = updateRules.flatMap(rule -> rule).excludeAny(firstPage, lastPage).distinct();
        if (shrinkPages.size() == 1)
            return shrinkPages.get(0);
        
        var shrinkUpdates = updateRules.exclude(rule -> rule.get(0).equals(firstPage) || rule.last().get().equals(lastPage));
        return reorder(shrinkUpdates);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("123", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("5502", result);
    }
    
}
