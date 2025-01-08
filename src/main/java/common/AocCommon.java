package common;

import static functionalj.lens.Access.theLong;
import static functionalj.stream.intstream.IntStreamPlus.range;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;

import functionalj.function.Func;
import functionalj.function.Func1;
import functionalj.function.FuncUnit1;
import functionalj.function.IntBiPredicatePrimitive;
import functionalj.function.IntFunctionPrimitive;
import functionalj.function.IntIntBiFunction;
import functionalj.functions.StrFuncs;
import functionalj.lens.lenses.BooleanAccessPrimitive;
import functionalj.lens.lenses.IntegerAccessPrimitive;
import functionalj.lens.lenses.IntegerToBooleanAccessPrimitive;
import functionalj.lens.lenses.LongAccessPrimitive;
import functionalj.list.AsFuncList;
import functionalj.list.FuncList;
import functionalj.list.FuncList.Mode;
import functionalj.list.ImmutableFuncList;
import functionalj.list.intlist.IntFuncList;
import functionalj.stream.AsStreamPlus;
import functionalj.stream.StreamPlus;

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
    
    static final LongAccessPrimitive<String> parseLong = Long::parseLong;
    
    static final Func1<String, IntFuncList> stringsToInts = strValue -> StrFuncs.grab(strValue, Pattern.compile("[0-9]+")).mapToInt(parseInt).cache();
    
    static final Func1<FuncList<Long>, Long> sumListOfLong = list -> list.sumToLong(theLong);
    
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
    
    static interface TwoRanges {
        <T> StreamPlus<T> map   (IntIntBiFunction<T>     mapper);
        TwoRanges         filter(IntBiPredicatePrimitive predicate);
        long              count (IntBiPredicatePrimitive predicate);
        
        default <T, S extends AsStreamPlus<T>> StreamPlus<T> flatMap(IntIntBiFunction<AsStreamPlus<T>> mapper) {
            return map(mapper)
                    .flatMap(it -> it.streamPlus());
        }
        
        static class Impl implements TwoRanges {
            final int range1;
            final int range2;
            final IntBiPredicatePrimitive predicate;
            Impl(int range1, int range2, IntBiPredicatePrimitive predicate) {
                this.range1 = range1;
                this.range2 = range2;
                this.predicate
                        = (predicate == null)
                        ? ((IntBiPredicatePrimitive)(__,___)->true)
                        : predicate;
            }
            @Override
            public <T> StreamPlus<T> map(IntIntBiFunction<T> mapper) {
                return range(0, range1).flatMapToObj(idx1 -> {
                    return range(0, range2)
                            .filter  (idx2 -> predicate.testIntInt(idx1, idx2))
                            .mapToObj(idx2 -> {
                                return mapper.apply(idx1, idx2);
                            });
                });
            }
            @Override
            public TwoRanges filter(IntBiPredicatePrimitive predicate) {
                return new TwoRanges.Impl(range1, range2, (i1,i2) -> {
                    return this.predicate.testIntInt(i1,i2)
                            && predicate.testIntInt(i1,i2);
                });
            }
            @Override
            public long count(IntBiPredicatePrimitive predicate) {
                return range(0, range1).sum(idx1 -> {
                    return (int)range(0, range2)
                            .mapToObj(idx2 -> predicate.testIntInt(idx1, idx2))
                            .filter  (bool -> bool)
                            .count();
                });
            }
        }
        
        static TwoRanges loop2(int range1, int range2) {
            return new TwoRanges.Impl(range1, range2, null);
        }
    }
    
    static interface TwoLists<T1, T2> {
        <T> FuncList<T>  map   (BiFunction<T1, T2, T> mapper);
        TwoLists<T1, T2> filter(BiPredicate<T1, T2>   predicate);
        long             count (BiPredicate<T1, T2>   predicate);
        
        @SuppressWarnings("unchecked")
        default <T, S extends AsStreamPlus<T>> S flatMap(BiFunction<T1, T2, ? extends AsStreamPlus<T>> mapper) {
            return (S)map(mapper)
                    .streamPlus()
                    .flatMap(it -> it.streamPlus());
        }
        
        static class Impl<T1, T2> implements TwoLists<T1, T2> {
            final FuncList<T1> list1;
            final FuncList<T2> list2;
            final BiPredicate<T1, T2> predicate;
            
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Impl(FuncList<T1> list1, FuncList<T2> list2, BiPredicate<T1, T2> predicate) {
                this.list1 = list1;
                this.list2 = list2;
                this.predicate
                        = (predicate == null)
                        ? ((BiPredicate)(__,___)->true)
                        : predicate;
            }
            @Override
            public <T> FuncList<T> map(BiFunction<T1, T2, T> mapper) {
                return list1.flatMapToObj(item1 -> {
                    return list2
                            .filter  (item2 -> predicate.test(item1, item2))
                            .mapToObj(item2 -> {
                                return mapper.apply(item1, item2);
                            });
                });
            }
            @Override
            public TwoLists<T1, T2> filter(BiPredicate<T1, T2> predicate) {
                return new TwoLists.Impl<>(list1, list2, (i1,i2) -> {
                    return this.predicate.test(i1,i2)
                            && predicate.test(i1,i2);
                });
            }
            @Override
            public long count(BiPredicate<T1, T2> predicate) {
                return list1.sumToLong(item1 -> {
                    return list2
                            .map   (item2 -> predicate.test(item1, item2))
                            .filter(bool -> bool)
                            .count();
                });
            }
        }
        
        static <T> TwoLists<T, T> nestLoopList2(FuncList<T> list) {
            return new TwoLists.Impl<>(list, list, null);
        }
        static <T1, T2> TwoLists<T1, T2> loopList2(FuncList<T1> list1, FuncList<T2> list2) {
            return new TwoLists.Impl<>(list1, list2, null);
        }
    }
    
    default FuncList<String> readAllLines() {
        var kind = challengeKind(1);
        var name = challengeName();
        return readAllLines(kind, name);
    }
    
    default FuncList<String> readAllLines(Kind kind, String challenge) {
        return readAllLines(dataPath, kind, challenge);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    default FuncList<String> readAllLines(String inputBase , Kind kind, String challengeName) {
        try {
            var inputFolder = challengeName.replaceAll("^Day([0-9]+).*$", "day$1");
            var challenge   = challengeName.replaceAll("^Day([0-9]+)Part([0-9]+)$", "day$1-part$2");
            var inputFile   = challenge + "-" + kind + ".txt";
            var lines       = Files.readAllLines(Path.of(inputBase, inputFolder, inputFile));
            return ImmutableFuncList.from(Mode.cache, (AsFuncList)FuncList.from(lines));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
