package graph;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;
import java.util.Map;

public class GraphBuilder {

    public static DefaultDirectedGraph<String, DefaultEdge> buildGraph(
            Map<String, List<String>> allDependencies) {

        DefaultDirectedGraph<String, DefaultEdge> graph =
                new DefaultDirectedGraph<>(DefaultEdge.class);

        for (String key : allDependencies.keySet()) {
            graph.addVertex(key);
            List<String> deps = allDependencies.get(key);
            if (deps != null) {
                for (String dep : deps) {
                    graph.addVertex(dep);
                    graph.addEdge(key, dep);
                }
            }
        }

        return graph;
    }
}
