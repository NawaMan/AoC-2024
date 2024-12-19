package day19;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.map.FuncMap;

public class Day19Part1Test extends BaseTest {
    
    
    Object calulate(FuncList<String> lines) {
        var availables
                = grab(regex("[a-z]+"), lines.get(0))
                .groupingBy(available -> (Character)available.charAt(0))
                .mapValue  (available -> available.map(String.class::cast));
        return lines
                .skip(2)
                .filter(pattern -> isPossible(pattern, availables))
                .size();
    }
    
    boolean isPossible(String pattern, FuncMap<Character, FuncList<String>> availables) {
        return isPossible(0, pattern, availables);
    }
    
    boolean isPossible(int offset, String pattern, FuncMap<Character, FuncList<String>>  availables) {
        if (offset >= pattern.length())
            return true;
        
        var first      = pattern.charAt(offset);
        var choices    = availables.get(first);
        var isPossible = (choices != null)
                       && choices
                           .filter  (choice -> pattern.substring(offset).startsWith(choice))
                           .anyMatch(choice -> isPossible(offset + choice.length(), pattern, availables));
        println(pattern.substring(offset) + " vs " + choices + " = " + isPossible);
        return isPossible;
    }
    
    //== Test ==

    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("6", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("209", result);
    }
    
}
