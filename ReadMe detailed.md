# Team

Coffee and Code

# Task

5\. Visualising Architecture with Knit

# Problem Statement: 

Modern software projects often grow into large, complex systems with thousands of dependencies.   
Knit, TikTok’s open-source dependency injection framework, provides high-performance DI but lacks visibility into the structure of these dependencies. Without proper tooling:

- Cyclic dependencies may go unnoticed  
- Redundant dependencies increase complexity  
- Developers waste time onboarding and debugging

Our project builds an IntelliJ IDEA Plugin that visualises Knit dependencies as a directed graph, highlights potential issues such as cyclic and transitive dependencies, and improves developer productivity by making dependency relationships clear and actionable. 

# APIs used

- Collections (java.util)  
  - List, ArrayList, Map, HashMap, Set, Deque, IOExceptions, StandardCharsets,   
- Jackson Databind  
  - ObjectMapper, JsonNode  
- IntelliJ SDK  
  - Project, VirtualFile, ProjectUtil  
- JGraphT Core   
  - DefaultDirectedGraph, DefaultEdge  
- JGraphT Ext  
  - JGraphXAdapter  
- JGraphX  
  - mxGraphComponent, mxGraph  
- JGraphXAdaptor  
  - getVertexToCellMap(), getEdgeToCellMap()

# Features

IDE Plugin Integration

- Graph Visualisation  
- Builds a directed dependency graph from source code  
- Uses colors to represent different dependency types   
- Find and display transitive dependencies  
  - Indirect dependencies reachable through intermediate nodes  
- Find and display cyclic dependencies  
  - Cycles that cause tightly coupled code  
- Flags edges that can be removed without breaking reachability (Redundancy)

# Development Tools

- Language: Java  
- Complier: javac  
- IDE: IntelliJ IDEA  
- Build Tool: Gradle (for plugin packaging)  
- Version Control: GitHub, Git

# Libraries and Assets

| Purpose | Library |
| ----- | ----- |
| Testing | `junit` (via `libs.junit`) |
|  | `opentest4j` (via `libs.opentest4j`) |
| JSON Processing | `jackson.databind`, `jackson.core`, `jackson.annotations` |
| Kotlin Support | `org.jetbrains.kotlin` (via `libs.plugins.kotlin`) |
| IntelliJ Plugin Dev | `intelliJPlatform` plugin and related IDE modules/plugins |
| Changelog Management | JetBrains Gradle Changelog Plugin (`libs.plugins.changelog`) |
| Static Analysis | Qodana plugin (`libs.plugins.qodana`) |
| Code Coverage | Kover plugin (`libs.plugins.kover`) |
| Graph drawing | Jgrapht and Jgraphx (org.jgrapht:jgrapht-core, org.jgrapht:jgrapht-ext, com.github.vlsi.mxgraph:jgraphx) |
| Json file processing | Jackson Databind (com.fasterxml.jackson.databind) |

# Usage

1. Install the plugin in IntelliJ IDEA:  
   1. Clone this repo  
   2. Build the plugin using gradle buildPlugin  
   3. Install via Settings \> Plugins \> Install Plugin from Disk  
2. Open a Knit-based project in Intellij  
3. Check that whether knit.json is under relative path of "/demo-jvm/build" from your project's root directory  
4. Use the “DependenciesVisualisation” menu item or toolbar icon  
5. A dependency graph will be generated:  
   1. Legend will be given at the side where cyclic dependencies are coloured in orange, redundant dependencies are coloured in blue and lastly, normal dependencies are in black  
   2. Each node is coloured in red

Demo Video: [https://youtu.be/RzvzdWPQcMU](https://youtu.be/RzvzdWPQcMU)

Source Code: [https://github.com/wafflescloud/coffeencode](https://github.com/wafflescloud/coffeencode) 