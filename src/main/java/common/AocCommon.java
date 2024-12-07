package common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;

import functionalj.function.Func;
import functionalj.function.Func1;
import functionalj.function.FuncUnit1;
import functionalj.function.IntFunctionPrimitive;
import functionalj.functions.StrFuncs;
import functionalj.lens.lenses.BooleanAccessPrimitive;
import functionalj.lens.lenses.IntegerAccessPrimitive;
import functionalj.lens.lenses.IntegerToBooleanAccessPrimitive;
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
    
    static final Func1<String, IntFuncList> stringsToInts = strValue -> StrFuncs.grab(strValue, Pattern.compile("[0-9]+")).mapToInt(parseInt).cache();
    
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
    
    default <TYPE> Func1<TYPE, TYPE> itself() {
        return it -> it;
    }
    
    default Pattern regex(String regex) {
        return Pattern.compile(regex);
    }
    
    default FuncList<String> grab(Pattern pattern, CharSequence strValue) {
        return StrFuncs.grab(strValue, pattern);
    }
    
    default Func1<String, FuncList<String>> grab(Pattern pattern) {
        return strValue -> StrFuncs.grab(strValue, pattern);
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
    
    default IntPredicate inspectTest(IntPredicate func) {
        return input -> {
            var output = func.test(input);
            println(input + " -> " + output);
            return output;
        };
    }
    
    default <O> IntFunctionPrimitive<O> inspect(IntFunctionPrimitive<O> func) {
        return input -> {
            var output = func.apply(input);
            println(input + " -> " + output);
            return output;
        };
    }
    
    default <I, O> Func1<I, O> inspect(Func1<I, O> func) {
        return input -> {
            var output = func.apply(input);
            println(input + " -> " + output);
            return output;
        };
    }
    
    default <I> BooleanAccessPrimitive<I> inspect(BooleanAccessPrimitive<I> func) {
        return input -> {
            var output = func.apply(input);
            println(input + " -> " + output);
            return output;
        };
    }
    
    default IntegerToBooleanAccessPrimitive inspect(IntegerToBooleanAccessPrimitive func) {
        return input -> {
            var output = func.apply(input);
            println(input + " -> " + output);
            return output;
        };
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
