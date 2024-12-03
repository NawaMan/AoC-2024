package day3;

import static functionalj.list.FuncList.AllOf;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;

public class Day3Part1Test extends BaseTest {
    
    int calulate(FuncList<String> cards) {
        return cards.sumToInt(this::pointOfCard);
    }
    
    int pointOfCard(String card) {
        var parts = card.split(" *+[:|] *+");
        var winnings = AllOf(parts[1].split(" +")).mapToInt(Integer::parseInt);
        var numbers  = AllOf(parts[2].split(" +")).mapToInt(Integer::parseInt);
        var size     = winnings.filter(numbers::contains).size();
        return (size == 0) ? 0 : (int)Math.pow(2, (size - 1));
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines = readAllLines();
        var result = calulate(lines);
        println(result);
        assertAsString("13", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        var result = calulate(lines);
        println(result);
        assertAsString("15205", result);
    }
    
}
