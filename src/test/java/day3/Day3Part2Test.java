package day3;

import java.util.regex.Pattern;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day3Part2Test extends BaseTest {
    
    Pattern pattern = Pattern.compile("(mul\\([0-9]{1,3},[0-9]{1,3}\\)|do\\(\\)|don't\\(\\))");
    
    int calulate(FuncList<String> lines) {
        var code = lines.join();
        
        var total   = 0;
        var enabled = true;
        
        var matcher = pattern.matcher(code);
        while (matcher.find()) {
            var match =  matcher.group();
            if      (match.equals("do()"))    enabled = true;
            else if (match.equals("don't()")) enabled = false;
            else if (enabled) {
                total += FuncList.of(match.replaceAll("[^,0-9]",  "").split(","))
                        .mapToInt(Integer::parseInt)
                        .product()
                        .getAsInt();
            }
        }
        return total;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines = readAllLines();
        var result = calulate(lines);
        println(result);
        assertAsString("48", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        var result = calulate(lines);
        println(result);
        assertAsString("98729041", result);
    }
    
}
