package day23;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import common.BaseTest;
import functionalj.list.FuncList;
import functionalj.map.FuncMap;

/**
 * --- Part Two ---
 * 
 * There are still way too many results to go through them all. You'll have to find the LAN party another way and go there yourself.
 * 
 * Since it doesn't seem like any employees are around, you figure they must all be at the LAN party. If that's true, the LAN party will be the largest set of computers that are all connected to each other. That is, for each computer at the LAN party, that computer will have a connection to every other computer at the LAN party.
 * 
 * In the above example, the largest set of computers that are all connected to each other is made up of co, de, ka, and ta. Each computer in this set has a connection to every other computer in the set:
 * 
 * ka-co
 * ta-co
 * de-co
 * ta-ka
 * de-ta
 * ka-de
 * 
 * The LAN party posters say that the password to get into the LAN party is the name of every computer at the LAN party, sorted alphabetically, then joined together with commas. (The people running the LAN party are clearly a bunch of nerds.) In this example, the password would be co,de,ka,ta.
 * 
 * What is the password to get into the LAN party?
 */
public class Day23Part2Test extends BaseTest {
    
    Object calculate(FuncList<String> lines) {
        var nodes
            = lines
            .map(grab(regex("[a-z]+")))
            .flatMap(itself())
            .distinct()
            .sorted()
            .cache();
            
        println(nodes);
        
        var connections
            = lines
            .map(grab(regex("[a-z]+")))
            .map(pair -> pair.sorted())
            .cache();
        var links
            = connections
            .groupingBy (pair -> pair.get(0), s -> s.streamPlus().map(pair -> pair.get(1)).sorted().toFuncList());
        
        links
        .entries()
        .sortedBy(String::valueOf)
        .forEach(this::println);
        println();
        
//        return links
//                .entries()
//                .flatMap(entry -> {
//                    var key   = entry.getKey();
//                    var value = entry.getValue();
//                    return value
//                            .filter(v -> links.containsKey(v))
//                            .flatMap(v -> {
//                                var nexts = links.get(v);
//                                return nexts
//                                            .filter(next -> value.contains(next))
//                                            .map(next -> FuncList.of(key, v, next));
//                            });
//                })
//                .filter(v -> ("," + v.join(",")).contains(",t"))
//                .distinct()
//                .sortedBy(String::valueOf)
//                .peek(v -> println(v))
//                .size();
        
//        var keys = links.keys().cache();
//        
////        keys
////        .map(key -> {
            
            var graph = new Graph(nodes, links);
            
            return links.keys().map(key -> graph.findLargest(key)).sortedBy(g -> -g.size()).findFirst().get().join(",");
            
////            return null;
////        })
////        .forEach(println);
//        
    }
    
    record Graph(FuncList<String> nodes, FuncMap<String, FuncList<String>> links) {
        FuncList<String> findLargest(String seed) {
            return findLargest(FuncList.of(seed));
        }
        FuncList<String> findLargest(FuncList<String> group) {
            var newGroups = tryExpands(group);
            if (newGroups == null)
                return group;
            
            return newGroups.map(g -> findLargest(g)).sortedBy(g -> -g.size()).findFirst().get();
        }
        FuncList<FuncList<String>> tryExpands(FuncList<String> items) {
            var nexts = nodes.exclude(items).filter(k ->allLinkedTo(items, k));
            return nexts.isEmpty()
                    ? null
                    : nexts.map(next -> items.append(next));
        }
        
        boolean allLinkedTo(FuncList<String> items, String k) {
            return items.allMatch(item -> isConnected(item, k));
        }
        
        boolean isConnected( String name1, String name2) {
            var nexts = links.get(name1);
            if (nexts != null) {
                return nexts.contains(name2);
            }
            nexts = links.get(name2);
            if (nexts != null) {
                return nexts.contains(name1);
            }
            
            return false;
        }
        
        public List<String> findLargestClique() {
            List<String> largestClique = new ArrayList<String>();
            Set<String> currentClique = new HashSet<>();

            // Backtracking to find all maximal cliques
            findCliques(0, new ArrayList<>(nodes), currentClique, largestClique);

            return largestClique;
        }

        private void findCliques(int start, List<String> nodeList, Set<String> currentClique, List<String> largestClique) {
            if (currentClique.size() > largestClique.size()) {
                largestClique.clear();
                largestClique.addAll(currentClique);
            }

            for (int i = start; i < nodeList.size(); i++) {
                String node = nodeList.get(i);
                if (isConnectedToClique(node, currentClique)) {
                    currentClique.add(node);
                    findCliques(i + 1, nodeList, currentClique, largestClique);
                    currentClique.remove(node);
                }
            }
        }

        private boolean isConnectedToClique(String node, Set<String> clique) {
            for (String member : clique) {
                if (!links.getOrDefault(node, FuncList.empty()).contains(member)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    //== Test ==
    
    @Test
    public void testExample() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("co,de,ka,ta", result);
    }
    
    @Ignore
    @Test
    public void testProd() {
        var lines  = readAllLines();
        var result = calculate(lines);
        println("result: " + result);
        assertAsString("", result);
    }
    
}
