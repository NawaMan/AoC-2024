package day5;

import java.util.Map;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

/**
 * --- Part Two ---
 * 
 * While the Elves get to work printing the correctly-ordered updates, you have a little time to fix the rest of them.
 * 
 * For each of the incorrectly-ordered updates, use the page ordering rules to put the page numbers in the right order. 
 *   For the above example, here are the three incorrectly-ordered updates and their correct orderings:
 * 
 *     75,97,47,61,53 becomes 97,75,47,61,53.
 *     61,13,29 becomes 61,29,13.
 *     97,13,75,29,47 becomes 97,75,47,29,13.
 * 
 * After taking only the incorrectly-ordered updates and ordering them correctly, their middle page numbers are 47, 29, 
 *   and 47. Adding these together produces 123.
 * 
 * Find the updates which are not in the correct order. What do you get if you add up the middle page numbers after 
 *   correctly ordering just those updates?
 * 
 * Your puzzle answer was 5502.
 */
public class Day5Part2Test extends BaseTest {
    
    Object calulate(FuncList<String> lines) {
        var firstSection = lines.acceptUntil(""::equals);
        var lastSection  = lines.skip       (firstSection.count() + 1);
        var rules        = firstSection.map (stringsToInts);
        var updates      = lastSection .map (stringsToInts);
        return updates
                .filter  (update   -> incorrectUpdate(rules, update))
                .map     (update   -> relevantRules  (rules, update))
                .mapToInt(relRules -> findMiddlePage (relRules))
                .sum();
    }
    
    boolean incorrectUpdate(FuncList<IntFuncList> rules, IntFuncList update) {
        return rules.anyMatch(rule -> {
            // update=75,47,61,53,29  intersect  rule=47|29  =>  matchOrder=[47,29]   <---  correct
            // update=75,47,61,53,29  intersect  rule=29|47  =>  matchOrder=[29,47]   <---  incorrect
            // update=75,47,61,53,29  intersect  rule=61|13  =>  matchOrder=[61]      <---  irrelevant
            var matchOrder = update.filterIn(rule);
            return (matchOrder.size() == 2) 
                    && !matchOrder.toString().equals(rule.toString());
        });
    }
    
    FuncList<IntFuncList> relevantRules(FuncList<IntFuncList> rules, IntFuncList update) {
        return rules.filter(rule -> {
            // update=75,47,61,53,29  intersect  rule=47|29  =>  matchOrder=[47,29]   <---  relevant
            // update=75,47,61,53,29  intersect  rule=61|13  =>  matchOrder=[61]      <---  irrelevant
            var matchOrder = update.filterIn(rule);
            return matchOrder.size() == 2;
        });
    }
    
    /** Quick method to get the middle -- assume rules exists for all pairs */
    int quickFindMiddle(FuncList<IntFuncList> rules) {
        // rules        : [[47, 53], [97, 61], [97, 47], [75, 53], [61, 53], [97, 53], [75, 47], [97, 75], [47, 61], [75, 61]]
        // first pages  : [47, 97, 97, 75, 61, 97, 75, 97, 47, 75]
        // frequencyMap : {47:2, 97:4, 75:3, 61:1}
        // sorted       : [61=1, 47=2, 75=3, 97=4]
        // midPage      :         ^^
        var firstPages   = rules.map(rule -> rule.get(0));
        var frequencyMap = firstPages.groupingBy(itself(), list -> list.size());
        return frequencyMap
                .entries()
                .sortedBy(Map.Entry::getValue)
                .mapToInt(Map.Entry::getKey)
                .pipe    (keys -> keys.get((keys.size() - 1) / 2));
    }
    
    int findMiddlePage(FuncList<IntFuncList> rules) {
        // All pages     : 61,13,29
        // Relevant rules: 61|13  29|13  61|29
        
        return quickFindMiddle(rules);   /*     // Put line comment on this line to select the longer method.
        
        // Below are the longer way --- by finding first and last and remove them until the middle one left.
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
                .flatMapToInt(itself())
                .exclude(firstPage)
                .exclude(lastPage)
                .distinct();
        if (middlePages.size() == 1) {
            return middlePages.get(0);
        }
        
        var reduceRules
                = rules
                .exclude(rule -> rule.get(0) == firstPage)
                .exclude(rule -> rule.last().getAsInt() == lastPage);
        
        // Recursively reduce.
        return findMiddlePage(reduceRules); /* */
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
