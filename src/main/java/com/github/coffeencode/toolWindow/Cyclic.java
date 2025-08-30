package com.github.coffeencode.toolWindow;

import java.util.*;

public class Cyclic {
    private Map<String, List<String>> graph;

    public Cyclic(Map<String, List<String>> graph) {
        this.graph = graph;
    }

    public Map<String, List<String>> findCycles() {
        List<List<String>> cycles = new ArrayList<>();
        Map<String, List<String>> map = new HashMap<>();

        for (String node: graph.keySet()) {
            Set<String> visited = new HashSet<>();
            Deque<String> path = new ArrayDeque<>();
            dfs(node, node, visited, path, cycles);
        }

        for (int i = 0; i < cycles.size(); i++) {
            List<String> cycle = cycles.get(i);
            for (int j = 0; j < cycle.size(); j++) {
                List<String> list;
                list = map.getOrDefault(cycle.get(j), new ArrayList<>());

                if (j + 1 < cycle.size()) {
                    list.add(cycle.get(j + 1));
                    map.put(cycle.get(j), list);
                } else {
                    list.add(cycle.get(0));
                    map.put(cycle.get(j), list);
                }
            }
        }
        return map;
    }

    private void dfs(String curr, String start, Set<String> visited,
                     Deque<String> path, List<List<String>> cycles) {
        visited.add(curr);
        path.push(curr);
        List<String> children = graph.getOrDefault(curr, Collections.emptyList());
        for (int i = 0; i < children.size(); i++) {
            String child = children.get(i);
            if (child.equals(start) && path.size() > 1) {
                List<String> cycle = new ArrayList<>(path);
                Collections.reverse(cycle);
                cycle.add(start);
                cycles.add(cycle);
            }

            if (!visited.contains(child)) {
                dfs(child, start, visited, path, cycles);
            }
        }

        path.pop();
        visited.remove(curr);
    }

    public static void main(String[] args) {
        Map<String, List<String>> origin = new HashMap<>();
        List<String> aList = new ArrayList<>();
        aList.addAll(List.of("B", "D"));
        List<String> bList = new ArrayList<>();
        bList.addAll(List.of("C"));
        List<String> cList = new ArrayList<>();
        cList.addAll(List.of("A"));
        List<String> dList = new ArrayList<>();
        dList.addAll(List.of("A"));
        origin.put("A", aList);
        origin.put("B", bList);
        origin.put("C", cList);
        origin.put("D", dList);

        Cyclic originalCyclic = new Cyclic(origin);
        Map<String, List<String>> cycles = originalCyclic.findCycles();
        System.out.println(cycles);
    }
}
