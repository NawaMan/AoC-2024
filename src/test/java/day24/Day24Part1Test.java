package day24;

import static functionalj.function.Func.f;
import static functionalj.functions.StrFuncs.capture;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import common.BaseTest;
import functionalj.function.Func1;
import functionalj.list.FuncList;
import functionalj.types.Choice;

public class Day24Part1Test extends BaseTest {
    
    static enum Operator {
        AND, OR, XOR;
        
        public int run(Node input1, Node input2) {
            switch(this) {
            case AND : return ((nodeEval(input1) == 1) && (nodeEval(input2) == 1)) ? 1 : 0;
            case OR  : return ((nodeEval(input1) == 1) || (nodeEval(input2) == 1)) ? 1 : 0;
            case XOR : return ((nodeEval(input1) == 1) != (nodeEval(input2) == 1)) ? 1 : 0;
            }
            return 0;
        }
    }
    
    @Choice
    static interface NodeSpec {
        void Value(String name, int value);
        void Ref  (String name, Func1<String, Integer> context);
        void Eval (String name, Operator operator, Node input1, Node input2);
    }
    
    static int nodeEval(Node node) {
        return node.match()
                .value(v -> v.value())
                .ref  (r -> r.context().apply(r.name()))
                .eval (e -> e.operator().run(e.input1(), e.input2()));
    }
    
    Object calulate(FuncList<String> lines) {
        var nodes   = new ConcurrentHashMap<String, Node>();
        var values  = new ConcurrentHashMap<String, Integer>();
        var context = f("context", (Func1<String, Integer>)(nodeName -> {
            var value = values.get(nodeName);
            if (value != null)
                return value.intValue();
            
            var node = nodes.get(nodeName);
            value = nodeEval(node);
            values.put(nodeName, value);
            return value;
        }));
        
        var inputs 
                = lines
                .acceptWhile(line -> !line.isEmpty())
                .map(capture(regex("(?<key>[xy0-9]+): (?<value>[0-9]+)")))
                .toMap(pair -> pair.get("key"), pair -> Node.Value(pair.get("key"), parseInt(pair.get("value"))))
                ;
        
        var exprs
                = lines
                .skipUntil(line -> line.isEmpty())
                .skip     (1)    // Skip the empty line itself.
                .map      (capture(regex("(?<input1>[^ ]+) (?<operator>(OR|XOR|AND)) (?<input2>[^ ]+) -> (?<output>[^ ]+)")))
                .toMap    (pair -> pair.get("output"), pair -> Node.Eval(pair.get("output"), Operator.valueOf(pair.get("operator")), Node.Ref(pair.get("input1"), context), Node.Ref(pair.get("input2"), context)));
        
        inputs.forEach((name, node) -> nodes.put(name, node));
        exprs.forEach((name, node) -> nodes.put(name, node));
        
        FuncList.from(nodes.keySet())
        .sorted()
        .forEach( z -> show(z + ": ", context.apply(z)));
        
        return FuncList.from(nodes.keySet())
                .filter(theString.thatStartsWith("z"))
                .sorted()
                .mapToLong    ( z     -> show(z + ": ", context.apply(z)))
                .mapWithIndex((i, z) -> z << i)
                .sum();
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("2024", result);
    }
    
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calulate(lines);
        println("result: " + result);
        assertAsString("57344080719736", result);
    }
    
}
