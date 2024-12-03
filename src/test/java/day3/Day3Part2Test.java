package day3;

import static functionalj.list.FuncList.AllOf;
import static functionalj.list.intlist.IntFuncList.AllOf;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

public class Day3Part2Test extends BaseTest {
    
    int calulate(FuncList<String> cards) {
        var cardMatches = cards.mapToInt(this::matchesOfCard);
        
        var size   = cards.size();
        var copies = IntFuncList.ones(size).toArray();
        
        for (int cardIndex = 0; cardIndex < cards.size(); cardIndex++) {
            var matches = cardMatches.get(cardIndex);
            updateCopies(size, copies, cardIndex, matches);
        }
        return AllOf(copies).sum();
    }
    
    void updateCopies(int size, int[] copies, int cardIndex, int matches) {
        for (int matchIndex = 1; matchIndex < (matches + 1); matchIndex++) {
            if ((cardIndex + matchIndex) >= size)
                break;
            
            copies[cardIndex + matchIndex] += copies[cardIndex];
        }
    }
    
    int matchesOfCard(String card) {
        var parts = card.split(" *+[:|] *+");
        var winnings = AllOf(parts[1].split(" +")).mapToInt(Integer::parseInt);
        var numbers  = AllOf(parts[2].split(" +")).mapToInt(Integer::parseInt);
        return winnings.filter(numbers::contains).size();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines = readAllLines();
        var result = calulate(lines);
        println(result);
        assertAsString("30", result);
    }
    
    @Test
    public void testProd() {
        var lines = readAllLines();
        var result = calulate(lines);
        println(result);
        assertAsString("6189740", result);
    }
    
}
