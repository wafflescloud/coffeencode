package com.github.coffeencode.toolWindow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OrganiseHelpers {
    /**
     * read knit.json
     * @param project
     * @return content as String type
     */
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
     * @param dependenciesTable might need to add dependencies during processClassName
     * @return list with 1 item if method and class belongs to the same class,
     *      list with 2 items if method and class belongs to two different classes
     */
    public static List<String> extractDependencies(String string, Map<String, List<String>> dependenciesTable) {
        String[] substrings = string.split("->");
        String method = substrings[0].trim();
        String parentClassInFull = substrings[1].trim();

        // get only the class name
        String childClassBeforeCleaned = method.substring(0, method.lastIndexOf(".")); // exclude the method name part
        // clean childClass and parentClass
        String parentClass = OrganiseHelpers.processClassName(parentClassInFull, dependenciesTable);
        String childClass = OrganiseHelpers.processClassName(childClassBeforeCleaned, dependenciesTable);

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
        List<String> dependencies = OrganiseHelpers.extractDependencies(method, dependenciesTable);
        if (dependencies.size() == 2) {
            childClass = dependencies.get(0); // has been processed in extractDependencies
            String parentClass = dependencies.get(1); // has been processed in extractDependencies
            OrganiseHelpers.insertADependency(dependenciesTable, childClass, parentClass);
        } else {
            childClass = dependencies.get(0); // has been processed in extractDependencies
        }
        if (!lowerLevelClass.isEmpty()) {
            // inner level child class is needed before outer level child class (lowerLevelClass) exists
            // lowerLevelClass has been processed in the precious recursive call, childClass has been processed in extractDependencies
            OrganiseHelpers.insertADependency(dependenciesTable, lowerLevelClass, childClass);
        }

        // recursion terminates when there is no more inner parameters field
        if (node.has("parameters")) {
            JsonNode parameterNode = node.get("parameters");
            for (JsonNode parameter : parameterNode) {
                recursivelyCheckDependencies(childClass, parameter, dependenciesTable);
            }
        }
    }

    /**
     *
     * @param className className to be cleaned and processed
     * @param dependenciesTable may need to insert dependencies if generic types exist
     * @return
     */
    public static String processClassName(String className, Map<String, List<String>> dependenciesTable) {
        String cleanedClassName = className;

        // eliminate () part
        int indexOfLeftBracket = className.indexOf("(");
        if (indexOfLeftBracket != -1) { // () part exists
            cleanedClassName = className.substring(0, indexOfLeftBracket).trim();
        }

        // eliminate those in "priority: no className" format and only keep className
        if (cleanedClassName.startsWith("priority:")) {
            String[] parts = cleanedClassName.split("\\s+"); // split by consecutive white space
            cleanedClassName = parts[parts.length - 1];
        }

        // check if it is a generic type e.g. List<T> needs T
        int indexOfSmallerSign = cleanedClassName.lastIndexOf("<");
        int indexOfGreaterSign = cleanedClassName.lastIndexOf(">");
        int indexOfLastDot = cleanedClassName.lastIndexOf(".");
        if ((indexOfSmallerSign != -1) && (indexOfLastDot != (indexOfSmallerSign - 1))) { // if dot is not immediately before <>, meaning it is not the init method
            String typeParameters = cleanedClassName.substring((indexOfSmallerSign + 1), indexOfGreaterSign);
            String[] types = typeParameters.split(", ");
            for (String type : types) {
                String[] parts = type.split(" ");
                String typeOnly = parts[parts.length - 1]; // Kotlin generic types may have in/out... e.g. <out T>
                OrganiseHelpers.insertADependency(dependenciesTable, cleanedClassName, typeOnly);
            }
        }

        return cleanedClassName;
    }

    /**
     * parse json content and summarise dependencies to a hashMap
     * @param jsonContent as String type
     * @return hashMap of class as key, all its dependencies organised in a list non-repeatedly as value
     */
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
            String formattedClassName = OrganiseHelpers.processClassName(className.replace("/", "."), dependenciesTable);
            JsonNode dependencies = root.get(className);

            // analyse parent
            JsonNode parentNode = dependencies.get("parent");
            if (parentNode != null && parentNode.isArray() && !parentNode.isEmpty()) { // there should be only one parent per class
                String parent = OrganiseHelpers.processClassName(parentNode.get(0).asText(), dependenciesTable);
                OrganiseHelpers.insertADependency(dependenciesTable, formattedClassName, parent);
            }

            // analyse providers
            JsonNode providers = dependencies.get("providers");
            if (providers != null && providers.isArray()) {
                for (JsonNode providerNode : providers) {
                    // analyse provider
                    String provider = providerNode.get("provider").asText();
                    List<String> dependency = OrganiseHelpers.extractDependencies(provider, dependenciesTable);
                    String childClass;
                    String parentClass;
                    if (dependency.size() == 2) {
                        childClass = dependency.get(0); // has been processed in extractDependencies
                        parentClass = dependency.get(1); // has been processed in extractDependencies
                        OrganiseHelpers.insertADependency(dependenciesTable, childClass, parentClass);
                    } else {
                        childClass = dependency.get(0); // has been processed in extractDependencies
                    }

                    // analyse parameters if exists
                    if (providerNode.has("parameters")) {
                        JsonNode parameters = providerNode.get("parameters");
                        // parameters of each provider if exists is always a string[] according to observation, not an Object[]
                        for (JsonNode parameterNode : parameters) {
                            String parameter = OrganiseHelpers.processClassName(parameterNode.asText(),dependenciesTable);
                            OrganiseHelpers.insertADependency(dependenciesTable, childClass, parameter);
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
                    String composite = OrganiseHelpers.processClassName(compositeNode.get(compositeName).asText(), dependenciesTable);
                    OrganiseHelpers.insertADependency(dependenciesTable, formattedClassName, composite);
                }
            }

            // analyse injections
            JsonNode injectionsNode = dependencies.get("injections");
            if (injectionsNode != null) {
                Iterator<String> injectionNames = injectionsNode.fieldNames();
                while (injectionNames.hasNext()) {
                    String injectionName = injectionNames.next();
                    JsonNode injection = injectionsNode.get(injectionName);
                    OrganiseHelpers.recursivelyCheckDependencies(formattedClassName, injection, dependenciesTable);
                }
            }
        }
        return dependenciesTable;
    }
}
