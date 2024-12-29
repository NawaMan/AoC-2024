package day25;

import static day25.Day25Part1Test.ChangeColumn.forKey;
import static day25.Day25Part1Test.ChangeColumn.forLock;

import org.junit.Test;

import common.BaseTest;
import functionalj.function.Func1;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

/**
 * --- Day 25: Code Chronicle ---
 * 
 * Out of ideas and time, The Historians agree that they should go back to check the Chief Historian's office one last 
 *   time, just in case he went back there without you noticing.
 * 
 * When you get there, you are surprised to discover that the door to his office is locked! You can hear someone inside, 
 *   but knocking yields no response. The locks on this floor are all fancy, expensive, virtual versions of five-pin 
 *   tumbler locks, so you contact North Pole security to see if they can help open the door.
 * 
 * Unfortunately, they've lost track of which locks are installed and which keys go with them, so the best they can do 
 * is send over schematics of every lock and every key for the floor you're on (your puzzle input).
 * 
 * The schematics are in a cryptic file format, but they do contain manufacturer information, so you look up their 
 *   support number.
 * 
 * "Our Virtual Five-Pin Tumbler product? That's our most expensive model! Way more secure than--" You explain that you 
 *   need to open a door and don't have a lot of time.
 * 
 * "Well, you can't know whether a key opens a lock without actually trying the key in the lock (due to quantum hidden 
 *   variables), but you can rule out some of the key/lock combinations."
 * 
 * "The virtual system is complicated, but part of it really is a crude simulation of a five-pin tumbler lock, mostly 
 *   for marketing reasons. If you look at the schematics, you can figure out whether a key could possibly fit in a lock."
 * 
 * He transmits you some example schematics:
 * 
 * #####
 * .####
 * .####
 * .####
 * .#.#.
 * .#...
 * .....
 * 
 * #####
 * ##.##
 * .#.##
 * ...##
 * ...#.
 * ...#.
 * .....
 * 
 * .....
 * #....
 * #....
 * #...#
 * #.#.#
 * #.###
 * #####
 * 
 * .....
 * .....
 * #.#..
 * ###..
 * ###.#
 * ###.#
 * #####
 * 
 * .....
 * .....
 * .....
 * #....
 * #.#..
 * #.#.#
 * #####
 * 
 * "The locks are schematics that have the top row filled (#) and the bottom row empty (.); the keys have the top row 
 *   empty and the bottom row filled. If you look closely, you'll see that each schematic is actually a set of columns 
 *   of various heights, either extending downward from the top (for locks) or upward from the bottom (for keys)."
 * 
 * "For locks, those are the pins themselves; you can convert the pins in schematics to a list of heights, one per column. 
 *   For keys, the columns make up the shape of the key where it aligns with pins; those can also be converted to a list 
 *   of heights."
 * 
 * "So, you could say the first lock has pin heights 0,5,3,4,3:"
 * 
 * #####
 * .####
 * .####
 * .####
 * .#.#.
 * .#...
 * .....
 * 
 * "Or, that the first key has heights 5,0,2,1,3:"
 * 
 * .....
 * #....
 * #....
 * #...#
 * #.#.#
 * #.###
 * #####
 * 
 * "These seem like they should fit together; in the first four columns, the pins and key don't overlap. However, this 
 *   key cannot be for this lock: in the rightmost column, the lock's pin overlaps with the key, which you know because 
 *   in that column the sum of the lock height and key height is more than the available space."
 * 
 * "So anyway, you can narrow down the keys you'd need to try by just testing each key with each lock, which means you 
 *   would have to check... wait, you have how many locks? But the only installation that size is at the North--" 
 *   You disconnect the call.
 * 
 * In this example, converting both locks to pin heights produces:
 * 
 * 0,5,3,4,3
 * 1,2,0,5,3
 * 
 * Converting all three keys to heights produces:
 * 
 * 5,0,2,1,3
 * 4,3,4,0,2
 * 3,0,2,0,1
 * 
 * Then, you can try every key with every lock:
 * 
 *     Lock 0,5,3,4,3 and key 5,0,2,1,3: overlap in the last column.
 *     Lock 0,5,3,4,3 and key 4,3,4,0,2: overlap in the second column.
 *     Lock 0,5,3,4,3 and key 3,0,2,0,1: all columns fit!
 *     Lock 1,2,0,5,3 and key 5,0,2,1,3: overlap in the first column.
 *     Lock 1,2,0,5,3 and key 4,3,4,0,2: all columns fit!
 *     Lock 1,2,0,5,3 and key 3,0,2,0,1: all columns fit!
 * 
 * So, in this example, the number of unique lock/key pairs that fit together without overlapping in any column is 3.
 * 
 * Analyze your lock and key schematics. How many unique lock/key pairs fit together without overlapping in any column?
 * 
 * Your puzzle answer was 2933.
 * 
 * The first half of this puzzle is complete! It provides one gold star: *
 * --- Part Two ---
 * 
 * You and The Historians crowd into the office, startling the Chief Historian awake! The Historians all take turns 
 *   looking confused until one asks where he's been for the last few months.
 * 
 * "I've been right here, working on this high-priority request from Santa! I think the only time I even stepped away 
 *   was about a month ago when I went to grab a cup of coffee..."
 * 
 * Just then, the Chief notices the time. "Oh no! I'm going to be late! I must have fallen asleep trying to put 
 *   the finishing touches on this chronicle Santa requested, but now I don't have enough time to go visit the last 50 
 *   places on my list and complete the chronicle before Santa leaves! He said he needed it before tonight's sleigh launch."
 * 
 * One of The Historians holds up the list they've been using this whole time to keep track of where they've been searching. 
 *   Next to each place you all visited, they checked off that place with a star. Other Historians hold up their own notes 
 *   they took on the journey; as The Historians, how could they resist writing everything down while visiting all those 
 *   historically significant places?
 * 
 * The Chief's eyes get wide. "With all this, we might just have enough time to finish the chronicle! Santa said 
 *   he wanted it wrapped up with a bow, so I'll call down to the wrapping department and... hey, could you bring it up 
 *   to Santa? I'll need to be in my seat to watch the sleigh launch by then."
 * 
 * You nod, and The Historians quickly work to collect their notes into the final set of pages for the chronicle.
 * 
 * You don't have enough stars to finish the chronicle, though. You need 7 more.
 */
public class Day25Part1Test extends BaseTest {
    
    static final String base = "#####";
    
    static enum ChangeColumn {
        forLock(false),
        forKey(true);
        
        final boolean invertColumn;
        ChangeColumn(boolean invertColumn) {
            this.invertColumn = invertColumn;
        }
    }
    
    Object calculate(FuncList<String> lines) {
        var inputs = lines.prepend("");
        
        var locksOrKeys
                = inputs
                .query(theString.thatIsEmpty())
                .map  (t -> inputs.skip(t._1 + 1).limit(7)) // _1 is index.
                .cache();
        var locks
                = locksOrKeys
                .filter(theList.first().asString().thatEquals(base))
                .map   (toColumns(forLock))
                .cache ();
        var keys
                = locksOrKeys
                .filter(theList.last().asString().thatEquals(base))
                .map   (toColumns(forKey))
                .cache ();
        
        return locks.sumToInt(lock -> keys.exclude(key -> hasOverlap(lock, key)).size());
    }
    
    Func1<FuncList<String>, IntFuncList> toColumns(ChangeColumn change) {
        var invert = change.invertColumn;
        return pics -> {
            var nums = new int[pics.get(0).length()];
            for (int col = 0; col < pics.get(0).length(); col++) {
                for (int row = 0; row < pics.size() - 1; row++) {
                    if (pics.get(row).charAt(col) != pics.get(row + 1).charAt(col)) {
                        nums[col] = invert ? 5 - row : row;
                    }
                }
            }
            return IntFuncList.of(nums);
        };
    }
    
    boolean hasOverlap(IntFuncList lock, IntFuncList key) {
        return lock.zipWith(key, (a,b) -> a + b).anyMatch(col -> col > 5);
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("3", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("2933", result);
    }
    
}
