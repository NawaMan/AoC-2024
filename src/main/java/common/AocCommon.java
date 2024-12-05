package common;

import java.nio.file.Files;
import java.nio.file.Path;

import functionalj.function.Func;
import functionalj.function.Func1;
import functionalj.function.FuncUnit1;
import functionalj.lens.lenses.IntegerAccessPrimitive;
import functionalj.list.FuncList;
import functionalj.list.intlist.IntFuncList;

public interface AocCommon {
    
    static final String dataPath = "data";
    
    public static enum Kind {
        example,
        prod;
    }
    
    static final Kind example = Kind.example;
    static final Kind prod    = Kind.prod;
    
    static final FuncUnit1<Object> println = Func.f(thing -> { System.out.println(thing); });
    
    static final IntegerAccessPrimitive<String> parseInt = Integer::parseInt;
    
    default String challengeName() {
        return  this.getClass().getSimpleName().replaceFirst("Test$", "");
    }
    
    default Kind challengeKind() {
        return challengeKind(1);
    }
    
    default Kind challengeKind(int offset) {
        try {
            throw new NullPointerException();
        } catch (Exception e) {
            var trace = e.getStackTrace();
            return Kind.valueOf((trace[1 + offset] + "").replaceAll("^.*\\.test(.*)\\(.*$", "$1").toLowerCase());
        }
    }
    
    default FuncList<String> split(String text) {
        return FuncList.of(text.split(text));
    }
    
    default int parseInt(String text) {
        return Integer.parseInt(text);
    }
    
    default <T> Func1<T, String> toStr() {
        return str -> "" + str;
    }
    
    default <T> Func1<T, String> indent() {
        return str -> "    " + str;
    }
    
    default void println() {
        System.out.println();
    }
    
    default void println(Object object) {
        System.out.println(object);
    }
    
    default <T> T show(String name, T object) {
        System.out.println(name + ": " + object);
        return object;
    }
    
    default <T> Func1<FuncList<T>, FuncList<T>> printEach() {
        return list -> list.peek(println);
    }
    
    default Func1<IntFuncList, IntFuncList> printInts() {
        return list -> list.peek(i -> println(i));
    }
    
    default FuncList<String> readAllLines() {
        return readAllLines(challengeKind(1), challengeName());
    }
    
    default FuncList<String> readAllLines(Kind kind, String challenge) {
        return readAllLines(dataPath, kind, challenge);
    }
    
    default FuncList<String> readAllLines(String inputBase , Kind kind, String challengeName) {
        try {
            var inputFolder = challengeName.replaceAll("^Day([0-9]+).*$", "day$1");
            var challenge   = challengeName.replaceAll("^Day([0-9]+)Part([0-9]+)$", "day$1-part$2");
            var inputFile   = challenge + "-" + kind + ".txt";
            var lines       = Files.readAllLines(Path.of(inputBase, inputFolder, inputFile));
            return FuncList.from(lines).toCache();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
