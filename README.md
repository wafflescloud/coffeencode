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
