package day22;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

/**
 * --- Day 22: Monkey Market ---
 * 
 * As you're all teleported deep into the jungle, a monkey steals The Historians' device! You'll need to get it back 
 *   while The Historians are looking for the Chief.
 * 
 * The monkey that stole the device seems willing to trade it, but only in exchange for an absurd number of bananas. 
 *   Your only option is to buy bananas on the Monkey Exchange Market.
 * 
 * You aren't sure how the Monkey Exchange Market works, but one of The Historians senses trouble and comes over to help.
 *   Apparently, they've been studying these monkeys for a while and have deciphered their secrets.
 * 
 * Today, the Market is full of monkeys buying good hiding spots. Fortunately, because of the time you recently spent in
 *   this jungle, you know lots of good hiding spots you can sell! If you sell enough hiding spots, you should be able 
 *   to get enough bananas to buy the device back.
 * 
 * On the Market, the buyers seem to use random prices, but their prices are actually only pseudorandom! If you know 
 *   the secret of how they pick their prices, you can wait for the perfect time to sell.
 * 
 * The part about secrets is literal, the Historian explains. Each buyer produces a pseudorandom sequence of secret 
 *   numbers where each secret is derived from the previous.
 * 
 * In particular, each buyer's secret number evolves into the next secret number in the sequence via the following process:
 * 
 *     Calculate the result of multiplying the secret number by 64. Then, mix this result into the secret number. 
 *       Finally, prune the secret number.
 *     Calculate the result of dividing the secret number by 32. Round the result down to the nearest integer.
 *       Then, mix this result into the secret number. Finally, prune the secret number.
 *     Calculate the result of multiplying the secret number by 2048. Then, mix this result into the secret number.
 *       Finally, prune the secret number.
 * 
 * Each step of the above process involves mixing and pruning:
 * 
 *     To mix a value into the secret number, calculate the bitwise XOR of the given value and the secret number. 
 *       Then, the secret number becomes the result of that operation. (If the secret number is 42 and you were to mix 
 *       15 into the secret number, the secret number would become 37.)
 *     To prune the secret number, calculate the value of the secret number modulo 16777216. Then, the secret number 
 *       becomes the result of that operation. (If the secret number is 100000000 and you were to prune the secret number,
 *       the secret number would become 16113920.)
 * 
 * After this process completes, the buyer is left with the next secret number in the sequence. The buyer can repeat 
 *   this process as many times as necessary to produce more secret numbers.
 * 
 * So, if a buyer had a secret number of 123, that buyer's next ten secret numbers would be:
 * 
 * 15887950
 * 16495136
 * 527345
 * 704524
 * 1553684
 * 12683156
 * 11100544
 * 12249484
 * 7753432
 * 5908254
 * 
 * Each buyer uses their own secret number when choosing their price, so it's important to be able to predict 
 *   the sequence of secret numbers for each buyer. Fortunately, the Historian's research has uncovered the initial 
 *   secret number of each buyer (your puzzle input). For example:
 * 
 * 1
 * 10
 * 100
 * 2024
 * 
 * This list describes the initial secret number of four different secret-hiding-spot-buyers on the Monkey Exchange Market.
 *   If you can simulate secret numbers from each buyer, you'll be able to predict all of their future prices.
 * 
 * In a single day, buyers each have time to generate 2000 new secret numbers. In this example, for each buyer, their 
 *   initial secret number and the 2000th new secret number they would generate are:
 * 
 * 1: 8685429
 * 10: 4700978
 * 100: 15273692
 * 2024: 8667524
 * 
 * Adding up the 2000th new secret number for each buyer produces 37327623.
 * 
 * For each buyer, simulate the creation of 2000 new secret numbers. What is the sum of the 2000th secret number generated 
 *   by each buyer?
 * 
 * Your puzzle answer was 14273043166.
 */
public class Day22Part1Test extends BaseTest {
    
    private static final int MODULO = 16777216; // 2^24
    
    long nextSecretNumber(long secret, int loop) {
        for (int i = 0; i < loop; i++) {
            secret = nextSecretNumber(secret);
        }
        return secret;
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
        if (secret < 0) {
            secret += MODULO;
        }
        return secret;
    }
    
    Object calculate(FuncList<String> lines, int loop) {
        return lines
                .map      (grab(regex("[0-9]+")))
                .map      (l -> l.get(0))
                .mapToLong(Long::parseLong)
                .map      (num -> nextSecretNumber(num, loop))
                .sum      ();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines, 2000);
        println("result: " + result);
        assertAsString("37327623", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines, 2000);
        println("result: " + result);
        assertAsString("14273043166", result);
    }
    
}
