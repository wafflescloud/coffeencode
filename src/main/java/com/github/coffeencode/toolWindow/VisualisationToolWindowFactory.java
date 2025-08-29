package com.github.coffeencode.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import javax.swing.JButton;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;
import java.nio.charset.StandardCharsets;

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
     * @return empty list if method and class belongs to the same class,
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
        List<String> dependenciesOfChild = dependenciesTable.computeIfAbsent(childClass, key -> new ArrayList<>());
        // add the new dependency to the dependencies list of the childClass
        if (!dependenciesOfChild.contains(parentClass)) {
            dependenciesOfChild.add(parentClass);
        }
    }

    public static void recursivelyCheckDependencies(JsonNode parametersNode, Map<String, List<String>> dependenciesTable) {
        
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
                    if (!dependency.isEmpty()) {
                        String childClass = dependency.get(0);
                        String parentClass = dependency.get(1);
                        VisualisationToolWindowFactory.insertADependency(dependenciesTable, childClass, parentClass);
                    }

                    // analyse parameters if exists
                }
            }
        }
    }


    @Override
    public void createToolWindowContent (Project project, ToolWindow toolWindow){

    }

}
