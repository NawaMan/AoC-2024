package day22;

import static functionalj.functions.StrFuncs.join2;
import static functionalj.list.longlist.LongFuncList.compound;
import static functionalj.stream.ZipWithOption.AllowUnpaired;
import static functionalj.stream.intstream.IntStreamPlus.range;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.function.Func2;
import functionalj.list.FuncList;
import functionalj.map.FuncMap;

public class Day22Part2Test extends BaseTest {
    
    private static final int MODULO = 16777216; // 2^24
    
    Object calculate(FuncList<String> lines) {
        var loop = 2000;
        return lines
                .map(Long::parseLong)
                // Create map of the 4-prior-changes -> the value
                .map(n -> firstFoundByPattern(n, loop))
                // Combine all the map by summing all the values (the banana)
                .reduce((a, b) -> a.zipWith(b, AllowUnpaired, sumNullableLongs())).get()
                // Get the key that has the max value
                .sortedByValue((a, b) -> Long.compare(b, a))
                .entries()
                .map(Map.Entry::getValue)
                // ... only pick one from the top
                .findFirst().get();
    }

    FuncMap<String, Long> firstFoundByPattern(long orgNumber, int loop) {
        var secrets
                = compound(orgNumber, this::nextSecretNumber)
                .map(theLong.remainderBy(10L))
                .limit(loop).cache();
        var changes
                = secrets
                .mapTwo((a, b) -> b - a)
                .mapToObj(n -> "%03d".formatted(n))
                .cache();
        var fourPriorChanges    // Example: "002,000,006,-04"
                = range  (0, 4)
                .mapToObj(changes::skip)
                .reduce  ((a,b) -> a.zipWith(b, join2(","))).get();
        var firstFound
            // 4-prefix changes -> to the value that follow
            = fourPriorChanges.zipWith (secrets.skip(4).boxed())
            // then create a map from the 4-changes to value that follow the changes.
            .toMap   (pair  -> pair._1(),   // 4-prior-changes
                      pair  -> pair._2(),   // the following value
                      (a,b)-> a);           // pick just the first.
        return firstFound;
    }
    
    long nextSecretNumber(long secret) {
        secret = mixAndPrune(secret, secret * 64);
        secret = mixAndPrune(secret, secret / 32);
        secret = mixAndPrune(secret, secret * 2048);
        return secret;
    }
    
    long mixAndPrune(long secret, long value) {
        secret ^= value;
        secret %= MODULO;
        if (secret < 0) secret += MODULO;
        return secret;
    }

    Func2<Long, Long, Long> sumNullableLongs() {
        return (a, b) -> ((a != null)?a:0) + ((b != null)?b:0);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("23", result);
    }
    
    @Ignore("Take long time.")
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("1667", result);
    }
    
}
