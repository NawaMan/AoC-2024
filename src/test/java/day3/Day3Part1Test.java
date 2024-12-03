package day3;

import java.util.regex.Pattern;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day3Part1Test extends BaseTest {
    
    Pattern pattern = Pattern.compile("mul\\([0-9]{1,3},[0-9]{1,3}\\)");
    
    int calulate(FuncList<String> lines) {
        return lines.mapToInt(this::lineTotal).sum();
    }
    
    int lineTotal(String line) {
        var total = 0;
        
        var matcher = pattern.matcher(line);
        while (matcher.find()) {
            var match =  matcher.group();
            total += FuncList.of(match.replaceAll("[^,0-9]",  "").split(","))
                    .mapToInt(Integer::parseInt)
                    .product()
                    .getAsInt();
        }
        return total;
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines = readAllLines();
        var result = calulate(lines);
        println(result);
        assertAsString("161", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        var result = calulate(lines);
        println(result);
        assertAsString("181345830", result);
    }
    
}
