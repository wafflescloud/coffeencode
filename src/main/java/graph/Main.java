package graph;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // ----------------------------
        // Hardcoded example dependencies
        // ----------------------------

        // All edges
        Map<String, List<String>> allDependencies = Map.of(
                "A", List.of("B"),
                "B", List.of("C"),
                "C", List.of("A") // cycle
        );

        // Edges that are part of cycles
        Map<String, List<String>> cyclicDependencies = Map.of(
                "A", List.of("B"),
                "B", List.of("C"),
                "C", List.of("A")
        );

        // Redundant / transitive edges
        Map<String, List<String>> redundantDependencies = Map.of(
                "A", List.of("C")
        );

        // ----------------------------
        // Build graph
        // ----------------------------
        DefaultDirectedGraph<String, DefaultEdge> graph =
                GraphBuilder.buildGraph(allDependencies);

        // ----------------------------
        // Visualize graph with colors and legend
        // ----------------------------
        GraphVisualiser.visualise(graph, cyclicDependencies, redundantDependencies);
    }
}

