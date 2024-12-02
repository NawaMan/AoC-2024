package day2;

import static functionalj.stream.intstream.IntStreamPlus.range;

import java.util.function.IntBinaryOperator;

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
        var report = FuncList.from(line.split(" "));
        return report.mapToInt(Integer::parseInt);
    }
    
    IntFuncList calculateReportDiffs(IntFuncList report) {
        return report.mapGroup(2, pair -> pair.reduce((IntBinaryOperator)(int a, int b) -> a - b).getAsInt());
    }
    
    boolean isKindOfSafeReport(IntFuncList report) {
        return isSafeReport(report)
                ? true
                : range(0, report.size()).anyMatch(i -> isSafeReport(report.excludeAt(i)));
    }
    
    boolean isSafeReport(IntFuncList report) {
        var diffs    = calculateReportDiffs(report);
        var adjDiffs = (diffs.first().getAsInt() < 0) ? diffs.map(i -> -i) : diffs;
        var isSafe   = adjDiffs.noneMatch(d -> d <= 0) && adjDiffs.noneMatch(d -> d  > 3);
        return isSafe;
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
