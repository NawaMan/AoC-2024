package day2;

import static functionalj.list.FuncList.AllOf;
import static functionalj.stream.intstream.IntStreamPlus.range;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

public class Day2Part2Test extends BaseTest {
    
    int calulate(FuncList<String> lines) {
        var reports     = lines  .map   (line   -> extractReport(line));
        var safeReports = reports.filter(report -> isKindOfSafeReport(report));
        return safeReports.size();
    }
    
    IntFuncList extractReport(String line) {
        return AllOf(line.split(" "))
                .mapToInt(Integer::parseInt);
    }
    
    boolean isKindOfSafeReport(IntFuncList report) {
        return isSafeReport(report)
                ? true
                : range(0, report.size())
                    .anyMatch(i -> isSafeReport(report.excludeAt(i)));
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
        
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("4", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        
        var result = calulate(lines);
        println("result: " + result);
        println();
        assertAsString("465", result);
    }
    
}
