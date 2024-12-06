package day5;

import org.junit.Test;

import common.BaseTest;
import functionalj.lens.lenses.IntegerAccessPrimitive;
import functionalj.list.FuncList;
import functionalj.stream.AsStreamPlus;

public class Day5Part2Test extends BaseTest {
    
    Object calulate(FuncList<String> lines) {
        var firstSection = lines.acceptUntil(""::equals);
        var lastSection  = lines.skip       (firstSection.count() + 1);
        var rules        = firstSection.map (grab(regex("[0-9]+")));
        var updates      = lastSection .map (grab(regex("[0-9]+")));
        return updates
                .filter  (update   -> incorrectUpdate(rules, update))
                .map     (update   -> relevantRules  (rules, update))
                .map     (relRules -> findMiddlePage (relRules))
                .mapToInt(middle   -> parseInt(middle))
                .sum();
    }
    
    boolean incorrectUpdate(FuncList<FuncList<String>> rules, FuncList<String> update) {
        return rules.anyMatch(rule -> {
            // update=75,47,61,53,29  intersect  rule=47|29  =>  matchOrder=[47,29]   <---  correct
            // update=75,47,61,53,29  intersect  rule=29|47  =>  matchOrder=[29,47]   <---  incorrect
            // update=75,47,61,53,29  intersect  rule=61|13  =>  matchOrder=[61]      <---  irrelevant
            var matchOrder = update.filterIn(rule);
            return (matchOrder.size() == 2) 
                    && !matchOrder.toString().equals(rule.toString());
        });
    }
    
    FuncList<FuncList<String>> relevantRules(FuncList<FuncList<String>> rules, FuncList<String> update) {
        return rules.filter(rule -> {
            // update=75,47,61,53,29  intersect  rule=47|29  =>  matchOrder=[47,29]   <---  relevant
            // update=75,47,61,53,29  intersect  rule=61|13  =>  matchOrder=[61]      <---  irrelevant
            var matchOrder = update.filterIn(rule);
            return matchOrder.size() == 2;
        });
    }
    
    <T> IntegerAccessPrimitive<AsStreamPlus<T>> theSize() {
        return stream -> stream.size();
    }
    
    String findMiddlePage(FuncList<FuncList<String>> rules) {
        // All pages     : 61,13,29
        // Relevant rules: 61|13  29|13  61|29
        
        // First pages: 61, 29, 61  ->  61,29
        // Last pages : 13, 13, 29  ->  13,29
        var firstPages = rules.map(rule -> rule.get(0));
        var lastPages  = rules.map(rule -> rule.get(1));
        
        // First page: [61,29] - [13,29]  ->  61
        // Last page : [13,29] - [61,29]  ->  13
        var firstPage = firstPages.exclude(lastPages::contains) .findFirst().get();
        var lastPage  = lastPages .exclude(firstPages::contains).findFirst().get();
        
        // Middle Pages = [61,13,29] - [61] - [13]  ->  29
        var middlePages
                = rules
                .flatMap(itself())
                .exclude(firstPage)
                .exclude(lastPage)
                .distinct();
        if (middlePages.size() == 1)
            return middlePages.get(0);
        
        var reduceRules
                = rules
                .exclude(rule -> rule.get(0).equals(firstPage))
                .exclude(rule -> rule.last().get().equals(lastPage));
        
        // Recursively reduce.
        return findMiddlePage(reduceRules);
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
