package com.github.coffeencode.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import javax.swing.*;
import java.util.*;
import java.util.List;

public class VisualisationToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent (Project project, ToolWindow toolWindow){
        // read knit.json file
        String jsonContent = OrganiseHelpers.readFile(project);
        String errMessage = "";
        // if no json content is found
        if (jsonContent.isEmpty()) {
            errMessage = "Sorry, we could not find the knit.json file of your project.";
        }

        // summarise dependencies
        Map<String, List<String>> dependenciesTable = OrganiseHelpers.summariseDependencies(jsonContent);
        if (dependenciesTable.isEmpty()) {
            errMessage = "Sorry, no dependencies were found for this project.";
        }

        // find cycles
        Map<String, List<String>> cyclicDependencies = new Cyclic(dependenciesTable).findCycles();

        // find transitives
        Map<String, List<String>> transitiveDependencies = new Transitive(dependenciesTable).findTransitives();

        // find redundancies
        Map<String, List<String>> redundantDependencies = new Transitive(dependenciesTable).findRedundant();

        // if contains error
        if (!errMessage.isEmpty()) {
            JBPanel panel = new JBPanel<>();
            panel.add(new JBLabel(errMessage));
            Content content = ContentFactory.getInstance().createContent(panel, null, false);
            toolWindow.getContentManager().addContent(content);
            return;
        }

        // set up the panel
        JBPanel panel = new JBPanel<>();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(5)); // add 5 pixels spacing from the top border

        if (cyclicDependencies.isEmpty() && redundantDependencies.isEmpty()) {
            panel.add(new JBLabel("FANTASTIC! Your project has no cyclic or redundant dependencies."));
        } else if (cyclicDependencies.isEmpty()) {
            panel.add(new JBLabel("GREAT! Your project has no cyclic dependencies."));
        } else if (redundantDependencies.isEmpty()) {
            panel.add(new JBLabel("GREAT! Your project has no redundant dependencies."));
        }
        panel.add(Box.createVerticalStrut(5)); // add 5 pixels spacing after the message

        DefaultDirectedGraph<String, DefaultEdge> graph = GraphBuilder.buildGraph(dependenciesTable);
        panel.add(GraphVisualiser.visualise(graph, cyclicDependencies, redundantDependencies));

        JBScrollPane scrollPane = new JBScrollPane(panel); // in case vertically too long

        // add to toolWindow
        Content content = ContentFactory.getInstance().createContent(scrollPane, "Dependencies Visualisation", false);
        toolWindow.getContentManager().addContent(content);
    }
}
