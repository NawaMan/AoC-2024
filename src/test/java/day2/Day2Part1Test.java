package day2;

import static functionalj.list.FuncList.AllOf;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

public class Day2Part1Test extends BaseTest {
    
    int calulate(FuncList<String> lines) {
        var reports     = lines  .map   (line   -> extractReport(line));
        var safeReports = reports.filter(report -> isSafeReport(report));
        return safeReports.size();
    }
    
    IntFuncList extractReport(String line) {
        return AllOf(line.split(" "))
                .mapToInt(Integer::parseInt);
    }
    
    boolean isSafeReport(IntFuncList report) {
        var diffs  = report.mapTwo((a, b) -> a - b);
        var sign   = (diffs.get(0) < 0) ? -1 : 1;
        return diffs.noneMatch(diff -> (sign*diff <= 0) || (sign*diff > 3));
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines = readAllLines();
        lines.forEach(this::println);
        println();
        
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("2", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        lines.forEach(this::println);
        println();
        
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("411", result);
    }
    
}
