package graph;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class GraphVisualiser {

    public static void visualise(
            DefaultDirectedGraph<String, DefaultEdge> graph,
            Map<String, List<String>> cyclicDependencies,
            Map<String, List<String>> redundantDependencies) {

        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<>(graph);
        mxGraph jgx = graphAdapter;

        // Style nodes (red nodes)
        for (String vertex : graph.vertexSet()) {
            Object cell = graphAdapter.getVertexToCellMap().get(vertex);
            jgx.getModel().setStyle(cell, "fillColor=red;fontColor=white;strokeColor=black");
        }

        // Style edges
        for (DefaultEdge e : graph.edgeSet()) {
            String source = graph.getEdgeSource(e);
            String target = graph.getEdgeTarget(e);
            Object cell = graphAdapter.getEdgeToCellMap().get(e);

            if (cyclicDependencies.containsKey(source) &&
                    cyclicDependencies.get(source).contains(target)) {
                jgx.getModel().setStyle(cell, "strokeColor=orange;strokeWidth=2");
            } else if (redundantDependencies.containsKey(source) &&
                    redundantDependencies.get(source).contains(target)) {
                jgx.getModel().setStyle(cell, "strokeColor=blue;strokeWidth=2");
            } else {
                jgx.getModel().setStyle(cell, "strokeColor=black;strokeWidth=1");
            }
        }

        // Graph component
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);

        // Create legend panel
        JPanel legend = new JPanel();
        legend.setLayout(new GridLayout(0, 1)); // one column
        legend.add(createLegendItem(Color.RED, "Node"));
        legend.add(createLegendItem(Color.ORANGE, "Cyclic Edge"));
        legend.add(createLegendItem(Color.BLUE, "Redundant Edge"));
        legend.add(createLegendItem(Color.BLACK, "Normal Edge"));

        // Main frame
        JFrame frame = new JFrame("Dependency Graph");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(new Dimension(1000, 600));

        // Layout: graph left, legend right
        frame.setLayout(new BorderLayout());
        frame.add(graphComponent, BorderLayout.CENTER);
        frame.add(legend, BorderLayout.EAST);

        frame.setVisible(true);
    }

    // Helper function to create a color box + label
    private static JPanel createLegendItem(Color color, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel colorBox = new JLabel();
        colorBox.setOpaque(true);
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(20, 20));
        panel.add(colorBox);
        panel.add(new JLabel(text));
        return panel;
    }
}
