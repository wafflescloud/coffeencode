package com.github.coffeencode.toolWindow;

import java.util.*;

public class Transitive {
    private final Map<String, List<String>> graph;

    /**
     * com.github.coffeencode.toolWindow.Transitive constructor
     * @param graph
     */
    public Transitive(Map<String, List<String>> graph) {
        this.graph = graph;
    }

    /**
     * finds all transitively reachable nodes for each node in the graph
     * usage: create a com.github.coffeencode.toolWindow.Transitive instance with graph of interest then
     *        call this method.
     * Eg: com.github.coffeencode.toolWindow.Transitive t = new com.github.coffeencode.toolWindow.Transitive(graph);
     *     t.findTransitives();
     * @return a Map where each key is a node in a graph, and the corresponding
     *          value is a list of nodes transitively reachable from that node
     * Example output:
     * {A=[D, E], B=[E], C=[], D=[], E=[]}
     * This means:
     * -> A can reach D and E through indirect paths
     * -> B can reach E indirectly
     * -> C, D, E do not have indirect reachability beyond their neighbours.
     *
     * check out the main function used for testing for more example usage.
     * To test this java file:
     * -> cd src\main\java
     * -> javac com.github.coffeencode.toolWindow.Transitive.java
     * -> java Transtive (this runs the file and output the test cases from main)
     */
    public Map<String, List<String>> findTransitives() {
        Map<String, List<String>> result = new HashMap<>();
        for (String node : graph.keySet()) {
            Set<String> visited = new HashSet<>();
            dfs(node, visited);

            visited.remove(node);
            visited.removeAll(graph.getOrDefault(node, Collections.emptyList()));

            result.put(node, new ArrayList<>(visited));
        }
        return result;
    }

    private void dfs(String node, Set<String> visited) {
        for (String neighbour : graph.getOrDefault(node, Collections.emptyList())) {
            if (visited.add(neighbour)) {
                dfs(neighbour, visited);
            }
        }
    }


    public static void main(String[] args) {
        //testing 1
        Map<String, List<String>> graph1 = new HashMap<>();
        graph1.put("A", Arrays.asList("B", "C"));
        graph1.put("B", Arrays.asList("C", "D"));
        graph1.put("C", Arrays.asList("E"));
        graph1.put("D", Arrays.asList("E"));
        graph1.put("E", Collections.emptyList());

        Transitive t1 = new Transitive(graph1);
        System.out.println("Test 1 result: " + t1.findTransitives());

        //testing 2
        Map<String, List<String>> graph2 = new HashMap<>();
        graph2.put("A", Arrays.asList("B", "C"));
        graph2.put("B", Arrays.asList("D", "E"));
        graph2.put("C", Arrays.asList("E"));
        graph2.put("D", Arrays.asList("F"));
        graph2.put("E", Collections.emptyList());
        graph2.put("F", Collections.emptyList());

        Transitive t2 = new Transitive(graph2);
        System.out.println("Test 2 result: " + t2.findTransitives());
    }
}




