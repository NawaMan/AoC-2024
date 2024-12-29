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

/**
 * --- Part Two ---
 * 
 * Of course, the secret numbers aren't the prices each buyer is offering! That would be ridiculous. Instead, the prices the buyer offers are just the ones digit of each of their secret numbers.
 * 
 * So, if a buyer starts with a secret number of 123, that buyer's first ten prices would be:
 * 
 * 3 (from 123)
 * 0 (from 15887950)
 * 6 (from 16495136)
 * 5 (etc.)
 * 4
 * 4
 * 6
 * 4
 * 4
 * 2
 * 
 * This price is the number of bananas that buyer is offering in exchange for your information about a new hiding spot. However, you still don't speak monkey, so you can't negotiate with the buyers directly. The Historian speaks a little, but not enough to negotiate; instead, he can ask another monkey to negotiate on your behalf.
 * 
 * Unfortunately, the monkey only knows how to decide when to sell by looking at the changes in price. Specifically, the monkey will only look for a specific sequence of four consecutive changes in price, then immediately sell when it sees that sequence.
 * 
 * So, if a buyer starts with a secret number of 123, that buyer's first ten secret numbers, prices, and the associated changes would be:
 * 
 *      123: 3 
 * 15887950: 0 (-3)
 * 16495136: 6 (6)
 *   527345: 5 (-1)
 *   704524: 4 (-1)
 *  1553684: 4 (0)
 * 12683156: 6 (2)
 * 11100544: 4 (-2)
 * 12249484: 4 (0)
 *  7753432: 2 (-2)
 * 
 * Note that the first price has no associated change because there was no previous price to compare it with.
 * 
 * In this short example, within just these first few prices, the highest price will be 6, so it would be nice to give 
 *   the monkey instructions that would make it sell at that time. The first 6 occurs after only two changes, so there's
 *   no way to instruct the monkey to sell then, but the second 6 occurs after the changes -1,-1,0,2. So, if you gave 
 *   the monkey that sequence of changes, it would wait until the first time it sees that sequence and then immediately
 *   sell your hiding spot information at the current price, winning you 6 bananas.
 * 
 * Each buyer only wants to buy one hiding spot, so after the hiding spot is sold, the monkey will move on to the next 
 *   buyer. If the monkey never hears that sequence of price changes from a buyer, the monkey will never sell, and will
 *   instead just move on to the next buyer.
 * 
 * Worse, you can only give the monkey a single sequence of four price changes to look for. You can't change the sequence 
 *   between buyers.
 * 
 * You're going to need as many bananas as possible, so you'll need to determine which sequence of four price changes 
 *   will cause the monkey to get you the most bananas overall. Each buyer is going to generate 2000 secret numbers after
 *   their initial secret number, so, for each buyer, you'll have 2000 price changes in which your sequence can occur.
 * 
 * Suppose the initial secret number of each buyer is:
 * 
 * 1
 * 2
 * 3
 * 2024
 * 
 * There are many sequences of four price changes you could tell the monkey, but for these four buyers, the sequence that
 *   will get you the most bananas is -2,1,-1,3. Using that sequence, the monkey will make the following sales:
 * 
 *     For the buyer with an initial secret number of 1, changes -2,1,-1,3 first occur when the price is 7.
 *     For the buyer with initial secret 2, changes -2,1,-1,3 first occur when the price is 7.
 *     For the buyer with initial secret 3, the change sequence -2,1,-1,3 does not occur in the first 2000 changes.
 *     For the buyer starting with 2024, changes -2,1,-1,3 first occur when the price is 9.
 * 
 * So, by asking the monkey to sell the first time each buyer's prices go down 2, then up 1, then down 1, then up 3, you
 *   would get 23 (7 + 7 + 9) bananas!
 * 
 * Figure out the best sequence to tell the monkey so that by looking for that same sequence of changes in every buyer's
 *   future prices, you get the most bananas in total. What is the most bananas you can get?
 * 
 * Your puzzle answer was 1667.
 */
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
