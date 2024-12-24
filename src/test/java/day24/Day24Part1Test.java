package day24;

import static functionalj.function.Func.f;
import static functionalj.functions.StrFuncs.capture;
import static java.util.regex.Pattern.compile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.junit.Test;

import common.BaseTest;
import functionalj.function.Func1;
import functionalj.list.FuncList;
import functionalj.map.FuncMap;
import functionalj.types.Choice;
import functionalj.types.choice.Self;

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
        void Eval (String name, Node input1, Node input2, Operator operator);
        
        public default String name(Self node) {
            return ((Node)node.unwrap()).match()
                    .value(Node.Value::name)
                    .ref  (Node.Ref::name)
                    .eval (Node.Eval::name);
        }
    }
    
    static int nodeEval(Node node) {
        return node.match()
                .value(v -> v.value())
                .ref  (r -> r.context().apply(r.name()))
                .eval (e -> e.operator().run(e.input1(), e.input2()));
    }
    
    static class Computer {
        
        private static final Pattern LOGIC = compile("(?<in1>[^ ]+) (?<opr>[^ ]+) (?<in2>[^ ]+) -> (?<out>[^ ]+)");
        
        final ConcurrentHashMap<String, Node>    nodes;
        final ConcurrentHashMap<String, Integer> values;
        
        public Computer(FuncList<String> logicCode) {
            this(logicCode, Map.of());
        }
        
        public Computer(FuncList<String> logicCode, Map<String, Integer> inputs) {
            this();
            
            var valueOf = f("this", this::valueOf);
            
            logicCode
            .map    (capture(LOGIC))
            .toMap  (pair -> pair.get("out"),
                     pair -> Node.Eval(
                                 pair.get("out"),
                                 Node.Ref(pair.get("in1"), valueOf),
                                 Node.Ref(pair.get("in2"), valueOf),
                                 Operator.valueOf(pair.get("opr"))))
            .forEach(nodes::put);
            
            inputs.forEach(values::put);
            inputs.forEach((name, value) -> nodes.put(name, Node.Value(name, value)));
        }
        
        private Computer() {
            this((ConcurrentHashMap<String, Node>)null, (ConcurrentHashMap<String, Integer>) null);
        }
        
        private Computer(ConcurrentHashMap<String, Node> nodes, ConcurrentHashMap<String, Integer> values) {
            this.nodes  = (nodes  != null) ? nodes  : new ConcurrentHashMap<String, Node>();
            this.values = (values != null) ? values : new ConcurrentHashMap<String, Integer>();
        }
        
        public Computer withNewInputs(Map<String, Integer> inputs) {
            // NOTE: I know. ...
            //   This is very hacky ..
            //     ... but sort of need a way to switch in a set of inputs with the same logic
            //     without having to re-compile the logic
            //     and not effect the existing
            //     as well as not working with memoization.
            var newComputer = new Computer(new ConcurrentHashMap<>(nodes), new ConcurrentHashMap<>());
            
            var newNodes = FuncMap.from(newComputer.nodes);
            newComputer.nodes.clear();
            newComputer.nodes.putAll(
                newNodes
                .mapValue(node -> {
                    return node.match()
                            .value(v -> (Node)v)
                            .ref  (r -> (Node)r)
                            .eval (e -> (Node)e.withInput1(Node.Ref(e.input1().asRef().get().name(), n -> newComputer.valueOf(n)))
                                               .withInput2(Node.Ref(e.input2().asRef().get().name(), n -> newComputer.valueOf(n))));
                }));
            
            inputs.forEach(newComputer.values::put);
            inputs.forEach((name, value) -> newComputer.nodes.put(name, Node.Value(name, value)));
            return newComputer;
        }
        
        public int valueOf(String nodeName) {
            var value = values.get(nodeName);
            if (value != null)
                return value.intValue();
            
            var node = nodes.get(nodeName);
            if(node == null)
                throw new NullPointerException("Node is null. nodeName=" + nodeName);
            value = nodeEval(node);
            values.put(nodeName, value);
            return value;
        }
        
        public long calculate(Map<String, Integer> inputs) {
            return withNewInputs(inputs)
                    .calculate();
        }
        
        public long calculate() {
            return FuncList.from(nodes.keySet())
                    .filter(theString.thatStartsWith("z"))
                    .sorted()
                    .mapToLong    ( z     -> valueOf(z))
                    .mapWithIndex((i, z) -> z << i)
                    .sum();
        }
        
    }
    
    Object calulate(FuncList<String> lines) {
        var inputs 
                = lines
                .acceptWhile(line -> !line.isEmpty())
                .map(capture(regex("(?<key>[xy0-9]+): (?<value>[0-9]+)")))
                .toMap(pair -> pair.get("key"), pair -> parseInt(pair.get("value")))
                ;
        
        var logicCode = lines
                        .skipUntil(line -> line.isEmpty())
                        .skip     (1);
        var computer = new Computer(logicCode, inputs);
        return computer.calculate();
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
