package com.github.coffeencode.toolWindow;

import com.intellij.ui.components.JBPanel;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class GraphVisualiser {

    public static JBPanel visualise(
        DefaultDirectedGraph<String, DefaultEdge> graph,
        Map<String, List<String>> cyclicDependencies,
        Map<String, List<String>> redundantDependencies) {

        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<>(graph);


        // Style nodes (red nodes)
        for (String vertex : graph.vertexSet()) {
            Object cell = graphAdapter.getVertexToCellMap().get(vertex);
            graphAdapter.getModel().setStyle(cell, "fillColor=red;fontColor=white;strokeColor=black");
        }

        // Style edges
        for (DefaultEdge e : graph.edgeSet()) {
            String source = graph.getEdgeSource(e);
            String target = graph.getEdgeTarget(e);
            Object cell = graphAdapter.getEdgeToCellMap().get(e);

            if (cyclicDependencies.containsKey(source) && cyclicDependencies.get(source).contains(target)) {
                graphAdapter.getModel().setStyle(cell, "strokeColor=orange;strokeWidth=2");
            } else if (redundantDependencies.containsKey(source) && redundantDependencies.get(source).contains(target)) {
                graphAdapter.getModel().setStyle(cell, "strokeColor=blue;strokeWidth=2");
            } else {
                graphAdapter.getModel().setStyle(cell, "strokeColor=black;strokeWidth=1");
            }

            graphAdapter.getModel().setValue(cell, ""); // remove the labels on edges to prevent cluttering
        }

        // apply an organic layout as dependencies map is not usually a hierarchical structure
        mxFastOrganicLayout layout = new mxFastOrganicLayout(graphAdapter);
        layout.setForceConstant(150);
        layout.setMinDistanceLimit(50);
        layout.setMaxIterations(1000);
        layout.execute(graphAdapter.getDefaultParent());

        // create graphComponent
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        graphComponent.setAutoscrolls(true);
        graphComponent.setDragEnabled(true);
        graphComponent.setAutoExtend(true);
        graphComponent.setConnectable(false);
        graphComponent.setPreferredSize(new Dimension(1200, 800));

        // Create legend panel
        JBPanel legend = new JBPanel<>();
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS)); // vertical box layout
        legend.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // padding of 10 for each side
        legend.add(createLegendItem(Color.RED, "Node"));
        legend.add(Box.createVerticalStrut(5)); // add vertical spacing of 5 pixel between two lines
        legend.add(createLegendItem(Color.ORANGE, "Cyclic Dependency"));
        legend.add(Box.createVerticalStrut(5));
        legend.add(createLegendItem(Color.BLUE, "Redundant Dependency"));
        legend.add(Box.createVerticalStrut(5));
        legend.add(createLegendItem(Color.BLACK, "Normal Dependency"));
        legend.setPreferredSize(new Dimension(200, legend.getPreferredSize().height));

        // Main panel with layout: graph left, legend right
        JBPanel mainPanel = new JBPanel<>(new BorderLayout());
        mainPanel.add(graphComponent, BorderLayout.CENTER);
        // To make legend not stretched and start from the top
        JBPanel legendContainer = new JBPanel<>(new BorderLayout()); // create a container, container will be the one that stretched
        legendContainer.add(legend, BorderLayout.PAGE_START); // put the legend from the top of the container, legend will not be stretched
        legendContainer.setPreferredSize(new Dimension(200, 0)); // set the container width to the same as that of legend
        mainPanel.add(legendContainer, BorderLayout.EAST);
        return mainPanel;
    }

    // Helper function to create a color box + label
    private static JBPanel createLegendItem(Color color, String text) {
        JBPanel panel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        // set customised painting
        JBPanel colorBox = new JBPanel() {
            @Override
            protected void paintComponent(Graphics colorBox) {
                colorBox.setColor(color);
                colorBox.fillRect(0, 0, getWidth(), getHeight());
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(20, 20);
            }
        };
        colorBox.setOpaque(false);
        panel.add(colorBox);
        panel.add(new JLabel(text));
        return panel;
    }
}
