package day2;

import static functionalj.stream.intstream.IntStreamPlus.range;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

/**
 * --- Part Two ---
 * 
 * The engineers are surprised by the low number of safe reports until they realize they forgot to tell you about the
 *   Problem Dampener.
 * 
 * The Problem Dampener is a reactor-mounted module that lets the reactor safety systems tolerate a single bad level in
 *   what would otherwise be a safe report. It's like the bad level never happened!
 * 
 * Now, the same rules apply as before, except if removing a single level from an unsafe report would make it safe, the
 *   report instead counts as safe.
 * 
 * More of the above example's reports are now safe:
 * 
 *     7 6 4 2 1: Safe without removing any level.
 *     1 2 7 8 9: Unsafe regardless of which level is removed.
 *     9 7 6 2 1: Unsafe regardless of which level is removed.
 *     1 3 2 4 5: Safe by removing the second level, 3.
 *     8 6 4 4 1: Safe by removing the third level, 4.
 *     1 3 6 7 9: Safe without removing any level.
 * 
 * Thanks to the Problem Dampener, 4 reports are actually safe!
 * 
 * Update your analysis by handling situations where the Problem Dampener can remove a single level from unsafe reports.
 *   How many reports are now safe?
 * 
 * Your puzzle answer was 465.
 * 
 */
public class Day2Part2Test extends BaseTest {
    
    int countKindOfSafeReports(FuncList<String> lines) {
        var reports     = lines  .map   (line   -> extractReport(line));
        var safeReports = reports.filter(report -> isKindOfSafeReport(report));
        return safeReports.size();
    }
    
    IntFuncList extractReport(String line) {
        return grab(regex("[0-9]+"), line)
                .mapToInt(parseInt);
    }
    
    boolean isKindOfSafeReport(IntFuncList report) {
        return isSafeReport(report)
                || range(0, report.size()).anyMatch(i -> isSafeReport(report.excludeAt(i)));
    }
    
    boolean isSafeReport(IntFuncList report) {
        var diffs  = report.mapTwo((a, b) -> a - b);
        var sign   = (diffs.get(0) < 0) ? -1 : 1;
        return diffs
                .map(diff -> sign*diff)
                .noneMatch(this::unsafeCondition);
    }

    boolean unsafeCondition(int unsignedDiff) {
        return (unsignedDiff <= 0) || (unsignedDiff > 3);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = countKindOfSafeReports(lines);
        println("result: " + result);
        assertAsString("4", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = countKindOfSafeReports(lines);
        println("result: " + result);
        assertAsString("465", result);
    }
    
}
