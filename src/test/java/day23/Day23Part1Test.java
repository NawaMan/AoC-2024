package day23;

import static functionalj.functions.StrFuncs.*;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day23Part1Test extends BaseTest {
    
    
    Object calculate(FuncList<String> lines) {
        var links = 
                lines
                .map(grab(regex("[a-z]+")))
                .map(pair -> pair.sorted())
                .groupingBy (pair -> pair.get(0), s -> s.streamPlus().map(pair -> pair.get(1)).sorted().toFuncList());
        
        links
        .entries()
        .sortedBy(String::valueOf)
        .forEach(this::println);
        println();
        
        return links
                .entries()
                .flatMap(entry -> {
                    var key   = entry.getKey();
                    var value = entry.getValue();
                    return value
                            .filter(v -> links.containsKey(v))
                            .flatMap(v -> {
                                var nexts = links.get(v);
                                return nexts
                                            .filter(next -> value.contains(next))
                                            .map(next -> FuncList.of(key, v, next));
                            });
                })
                .filter(v -> ("," + v.join(",")).contains(",t"))
                .distinct()
                .sortedBy(String::valueOf)
                .peek(v -> println(v))
                .size();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("7", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("1238", result);
    }
    
}
