# Dependencies Visualisation Plugin -- Team Coffee and Code
📄 [Here's the detailed README](README_detailed.md)
## Project Inspiration
Our project was inspired by the need to simplify dependency management in software development. While working with large projects, we noticed that dependencies often form complex **directed graphs**, which can lead to:
- **Redundancy**: Unnecessary edges that are implied by transitive paths.
- **Cyclic dependencies**: These make builds unreliable and harder to maintain.
- **Visualization difficulties**: Understanding the structure of large projects becomes increasingly challenging.
This sparked the idea of building a **plugin tool** that not only **generates dependency graphs**, but also **automatically detects redundant and cyclic dependencies**.

## What It Does
We model software dependencies as a directed graph:
G = (V, E)
- **V**: Set of nodes (modules or classes)  
- **E**: Set of directed edges (dependencies)  

In large projects, this graph can become complex, leading to:
- **Redundant dependencies**: e.g., if A → B and B → C, then A → C is implied and redundant.  
- **Cyclic dependencies**: cycles can make builds unreliable.  
- **Difficulty in understanding structure**: especially when dependencies grow across multiple levels.
---
## Our Tool
To address these issues, we implemented two Java utilities:
- **`Cyclic` class**: Detects cyclic dependencies using DFS traversal.  
- **`Transitive` class**:  
  - Finds all transitively reachable nodes from each node.  
  - Identifies redundant edges based on whether a node is reachable without a direct dependency.
---
## Example
Given a graph:
A → B → C
↓ ↑
└──→ C ──┘
The tool detects:
- A cycle: A → B → C → A  
- A redundant edge: A → C (since A can already reach C via B)
---
## Sample Output
Transitives: {A=[D, E], B=[E], C=[], D=[], E=[]}
Redundant edges: {A=[C]}
This means:
- A can reach D and E through transitive paths.  
- The direct edge A → C is redundant because C is already reachable via B.
---
## How We Built It
We divided the work across our team members to balance efficiency and specialization:
- **Graph Generation** – One teammate worked on scanning the project and creating the directed graph representation of dependencies.
- **Redundancy Detection** – Another teammate implemented an algorithm to check for transitive edges, flagging redundant dependencies.
- **Cycle Detection** – Another teammate focused on detecting cycles in the dependency graph.
- **Plugin Integration** – Finally, we integrated all components into a plugin that developers can run on their projects.

The modular approach allowed us to focus on different challenges while ensuring the pieces fit together.
---
## Challenges We Faced

Like any real project, we faced multiple challenges:
- **Integrating algorithms into the plugin** was not straightforward, as we had to ensure performance and compatibility.
- **Graph complexity**: handling larger graphs made us optimize the redundancy detection algorithm.
- **Team coordination**: splitting the work meant we had to carefully define interfaces between components so that everything could be stitched together smoothly.
---
## Conclusion
This project not only gave us a tool to detect redundant and cyclic dependencies, but also taught us valuable lessons about graph algorithms, plugin development, and team collaboration. It was both a technical and collaborative learning journey!

---
![Build](https://github.com/wafflescloud/coffeencode/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [ ] Get familiar with the [template documentation][template].
- [ ] Adjust the [pluginGroup](./gradle.properties) and [pluginName](./gradle.properties), as well as the [id](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [ ] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [ ] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to JetBrains Marketplace.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.
- [ ] Configure the [CODECOV_TOKEN](https://docs.codecov.com/docs/quick-start) secret for automated test coverage reports on PRs

<!-- Plugin description -->
This Fancy IntelliJ Platform Plugin is going to be your implementation of the brilliant ideas that you have.

This specific section is a source for the [plugin.xml](/src/main/resources/META-INF/plugin.xml) file which will be extracted by the [Gradle](/build.gradle.kts) during the build process.

To keep everything working, do not remove `<!-- ... -->` sections. 
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "coffeencode"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/wafflescloud/coffeencode/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
