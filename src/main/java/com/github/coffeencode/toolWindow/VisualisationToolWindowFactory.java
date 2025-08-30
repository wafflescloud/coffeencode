package com.github.coffeencode.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class VisualisationToolWindowFactory implements ToolWindowFactory {
    // read JSON file
    public static String readFile(Project project) {
        // find the project
        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir == null) {
            return "";
        }

        // assume only the demo project is being used, so the path to find knit.json is hardcoded
        // find the file
        VirtualFile knitJsonFile = projectDir.findChild("demo-jvm")
                .findChild("build")
                .findChild("knit.json");

        // read the content
        try {
            String jsonContent = new String(knitJsonFile.contentsToByteArray(), StandardCharsets.UTF_8);
            return jsonContent;
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * extract dependencies from the input format "method -> class"
     * @param string in the format "method -> class"
     * @return list with 1 item if method and class belongs to the same class,
     *      list with 2 items if method and class belongs to two different classes
     */
    public static List<String> extractDependencies(String string) {
        String[] substrings = string.split("->");
        String method = substrings[0].trim();
        String parentClassInFull = substrings[1].trim();

        // get only the class name
        String childClass = method.substring(0, method.lastIndexOf(".")); // exclude the method name part
        // exclude the () part for parent class
        int indexOfLeftBracket = parentClassInFull.indexOf("(");
        String parentClass = parentClassInFull;
        if (indexOfLeftBracket != -1) { // () part exists
            parentClass = parentClassInFull.substring(0, indexOfLeftBracket).trim();
        }

        // compare childClass and parentClass
        List<String> dependency = new ArrayList<>();
        // if they are not equal
        if (!childClass.equals(parentClass)) {
            dependency.add(childClass); // first element
            dependency.add(parentClass); // second element
        } else {
            dependency.add(childClass);
        }
        return dependency;
    }

    /**
     * Insert a new dependency
     * @param dependenciesTable: hashmap that stores the dependencies
     * @param childClass: a key of the hashmap
     * @param parentClass: to be added to the arrayList of childClass's dependencies if it is not already there
     */
    public static void insertADependency(Map<String, List<String>> dependenciesTable, String childClass, String parentClass) {
        if (childClass.isEmpty() || parentClass.isEmpty()) {
            return;
        }
        List<String> dependenciesOfChild = dependenciesTable.computeIfAbsent(childClass, key -> new ArrayList<>());
        // add the new dependency to the dependencies list of the childClass
        if (!dependenciesOfChild.contains(parentClass)) {
            dependenciesOfChild.add(parentClass);
        }
    }

    /**
     * for analysing dependencies in each injection method
     * @param lowerLevelClass remember outer level child class
     * @param node the JsonNode to evaluate
     * @param dependenciesTable main dependencies table that records all the dependencies
     */
    public static void recursivelyCheckDependencies(String lowerLevelClass, JsonNode node, Map<String, List<String>> dependenciesTable) {
        String childClass;

        // methodId field always exist
        String method = node.get("methodId").asText();
        List<String> dependencies = VisualisationToolWindowFactory.extractDependencies(method);
        if (dependencies.size() == 2) {
            childClass = dependencies.get(0);
            String parentClass = dependencies.get(1);
            VisualisationToolWindowFactory.insertADependency(dependenciesTable, childClass, parentClass);
        } else {
            childClass = dependencies.get(0);
        }
        if (!lowerLevelClass.isEmpty()) {
            // inner level child class is needed before outer level child class (lowerLevelClass) exists
            VisualisationToolWindowFactory.insertADependency(dependenciesTable, lowerLevelClass, childClass);
        }

        // recursion terminates when there is no more inner parameters field
        if (node.has("parameters")) {
            JsonNode parameterNode = node.get("parameters");
            for (JsonNode parameter : parameterNode) {
                recursivelyCheckDependencies(childClass, parameter, dependenciesTable);
            }
        }
    }

    public static Map<String, List<String>> summariseDependencies(String jsonContent) {
        // parse Json content
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(jsonContent);
        } catch (IOException e) {
            return new HashMap<>();
        }

        // initialise a dependenciesTable
        HashMap<String, List<String>> dependenciesTable = new HashMap<>();

        // organise dependencies
        Iterator<String> classes = root.fieldNames();
        while (classes.hasNext()) {
            // always store the . format instead of the / format
            String className = classes.next();
            String formattedClassName = className.replace("/", ".");
            JsonNode dependencies = root.get(className);

            // analyse parent
            JsonNode parentNode = dependencies.get("parent");
            if (parentNode != null && parentNode.isArray() && !parentNode.isEmpty()) { // there should be only one parent per class
                String parent = parentNode.get(0).asText();
                VisualisationToolWindowFactory.insertADependency(dependenciesTable, formattedClassName, parent);
            }

            // analyse providers
            JsonNode providers = dependencies.get("providers");
            if (providers != null && providers.isArray()) {
                for (JsonNode providerNode : providers) {
                    // analyse provider
                    String provider = providerNode.get("provider").asText();
                    List<String> dependency = VisualisationToolWindowFactory.extractDependencies(provider);
                    String childClass;
                    String parentClass;
                    if (dependency.size() == 2) {
                        childClass = dependency.get(0);
                        parentClass = dependency.get(1);
                        VisualisationToolWindowFactory.insertADependency(dependenciesTable, childClass, parentClass);
                    } else {
                        childClass = dependency.get(0);
                    }

                    // analyse parameters if exists
                    if (providerNode.has("parameters")) {
                        JsonNode parameters = providerNode.get("parameters");
                        // parameters of each provider if exists is always a string[] according to observation, not an Object[]
                        for (JsonNode parameterNode : parameters) {
                            String parameter = parameterNode.asText();
                            VisualisationToolWindowFactory.insertADependency(dependenciesTable, childClass, parameter);
                        }
                    }
                }
            }

            // analyse composite
            JsonNode compositeNode = dependencies.get("composite");
            if (compositeNode != null) {
                Iterator<String> compositeNames = compositeNode.fieldNames();
                while (compositeNames.hasNext()) {
                    String compositeName = compositeNames.next();
                    String composite = compositeNode.get(compositeName).asText();
                    VisualisationToolWindowFactory.insertADependency(dependenciesTable, formattedClassName, composite);
                }
            }

            // analyse injections
            JsonNode injectionsNode = dependencies.get("injections");
            if (injectionsNode != null) {
                Iterator<String> injectionNames = injectionsNode.fieldNames();
                while (injectionNames.hasNext()) {
                    String injectionName = injectionNames.next();
                    JsonNode injection = injectionsNode.get(injectionName);
                    VisualisationToolWindowFactory.recursivelyCheckDependencies(formattedClassName, injection, dependenciesTable);
                }
            }
        }
        return dependenciesTable;
    }


    @Override
    public void createToolWindowContent (Project project, ToolWindow toolWindow){
        // read knit.json file
        String jsonContent = readFile(project);
        String errMessage = "";
        // if no json content is found
        if (jsonContent.isEmpty()) {
            errMessage = "Sorry, we could not find the knit.json file of your project.";
        }

        // summarise dependencies
        Map<String, List<String>> dependenciesTable = summariseDependencies(jsonContent);
        if (dependenciesTable.isEmpty()) {
            errMessage = "Sorry, no dependencies were found for this project.";
        }

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
        JBScrollPane scrollPane = new JBScrollPane(panel); // in case vertically too long

        // add to toolWindow
        Content content = ContentFactory.getInstance().createContent(scrollPane, "Dependencies Visualisation", false);
        toolWindow.getContentManager().addContent(content);
    }

}
