import java.util.*;

public class Transitive {
    private final Map<String, List<String>> graph;

    /**
     * Transitive constructor
     * @param graph
     */
    public Transitive(Map<String, List<String>> graph) {
        this.graph = graph;
    }

    /**
     * finds all transitively reachable nodes for each node in the graph
     * usage: create a Transitive instance with graph of interest then
     *        call this method.
     * Eg: Transitive t = new Transitive(graph);
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
     * -> javac Transitive.java
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

    /**
     * find redundant nodes: direct edges is redundant if the node is reachable
     * even without the direct dependencies.
     * for example:
     * A -> B -> C
     * A -> C
     * A -> C is redundant as A can still reach C via B
     * @return a Map where each key is a node in a graph, and the corresponding
     *         value is a list of nodes that is redundant from that node
     * Example output:
     * {A=[C]} this means that A->C is redundant
     */
    public Map<String, List<String>> findRedundant() {
        Map<String, List<String>> redundantEdges = new HashMap<>();

        for (String node : graph.keySet()) {
            List<String> redundant = new ArrayList<>();

            for (String neighbour : graph.getOrDefault(node, Collections.emptyList())) {
                // Create a visited set to check if there's an alternative path
                Set<String> visited = new HashSet<>();
                if (canReachWithoutEdge(node, neighbour, visited, node, neighbour)) {
                    redundant.add(neighbour);
                }
            }

            if (!redundant.isEmpty()) {
                redundantEdges.put(node, redundant);
            }
        }

        return redundantEdges;
    }

    private boolean canReachWithoutEdge(String current, String target, Set<String> visited, String src, String excluded) {
        if (current.equals(target)) return true;

        for (String neighbour : graph.getOrDefault(current, Collections.emptyList())) {
            if (current.equals(src) && neighbour.equals(excluded)) continue;
            if (visited.add(neighbour)) {
                if (canReachWithoutEdge(neighbour, target, visited, src, excluded)) {
                    return true;
                }
            }
        }
        return false;
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
        System.out.println("Transitives 1: " + t1.findTransitives());
        System.out.println("Redundant edges 1: " + t1.findRedundant());

        //testing 2
        Map<String, List<String>> graph2 = new HashMap<>();
        graph2.put("A", Arrays.asList("B", "C", "F"));
        graph2.put("B", Arrays.asList("D", "E"));
        graph2.put("C", Arrays.asList("E"));
        graph2.put("D", Arrays.asList("F"));
        graph2.put("E", Collections.emptyList());
        graph2.put("F", Collections.emptyList());

        Transitive t2 = new Transitive(graph2);
        System.out.println("Transitives 2: " + t2.findTransitives());
        System.out.println("Redundant edges: " + t2.findRedundant());
    }
}




