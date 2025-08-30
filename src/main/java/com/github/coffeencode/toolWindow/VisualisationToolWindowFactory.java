package com.github.coffeencode.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

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
        for (Map.Entry<String, List<String>> entry : dependenciesTable.entrySet()) {
            String className = entry.getKey();
            String dependencies = String.join(" & ", entry.getValue());
            JTextArea text = new JTextArea(className + " needs " + dependencies);
            text.setLineWrap(true); // in case the line is too long
            text.setWrapStyleWord(true); // in case word does not cut half
            text.setEditable(false); // prevent users from editing
            panel.add(text);
            panel.add(Box.createVerticalStrut(6)); // add 6 pixels of spacings after each class & its dependencies
        }

        // display cycles
        panel.add(Box.createVerticalStrut(12)); // add 12 pixels to spacings to separate from all dependencies above
        panel.add(new JTextArea("Those involved in cyclic dependencies: "));
        for (Map.Entry<String, List<String>> entry : cyclicDependencies.entrySet()) {
            String className = entry.getKey();
            String dependencies = String.join(" & ", entry.getValue());
            JTextArea text = new JTextArea(className + " needs " + dependencies);
            text.setLineWrap(true); // in case the line is too long
            text.setWrapStyleWord(true); // in case word does not cut half
            text.setEditable(false); // prevent users from editing
            panel.add(text);
            panel.add(Box.createVerticalStrut(6)); // add 6 pixels of spacings after each class & its dependencies
        }

        // display transitive dependencies
        panel.add(Box.createVerticalStrut(12)); // add 12 pixels to spacings to separate from all dependencies above
        panel.add(new JTextArea("Transitive dependencies: "));
        for (Map.Entry<String, List<String>> entry : transitiveDependencies.entrySet()) {
            String className = entry.getKey();
            String dependencies = String.join(" & ", entry.getValue());
            JTextArea text = new JTextArea(className + " needs " + dependencies);
            text.setLineWrap(true); // in case the line is too long
            text.setWrapStyleWord(true); // in case word does not cut half
            text.setEditable(false); // prevent users from editing
            panel.add(text);
            panel.add(Box.createVerticalStrut(6)); // add 6 pixels of spacings after each class & its dependencies
        }

        JBScrollPane scrollPane = new JBScrollPane(panel); // in case vertically too long

        // add to toolWindow
        Content content = ContentFactory.getInstance().createContent(scrollPane, "Dependencies Visualisation", false);
        toolWindow.getContentManager().addContent(content);
    }

}
