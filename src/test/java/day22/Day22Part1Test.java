package day22;

import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

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
    
    Object calulate(FuncList<String> lines, int loop) {
        lines.forEach(this::println);
        println();
        
        return lines.map(grab(regex("[0-9]+"))).map(l -> l.get(0)).mapToLong(Long::parseLong).map(num -> nextSecretNumber(num, loop)).sum();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines, 2000);
        println("result: " + result);
        assertAsString("37327623", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines, 2000);
        println("result: " + result);
        assertAsString("14273043166", result);
    }
    
}
