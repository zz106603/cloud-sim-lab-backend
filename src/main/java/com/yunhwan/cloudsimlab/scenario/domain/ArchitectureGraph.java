package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.List;

public record ArchitectureGraph(
		List<ArchitectureNode> nodes,
		List<ArchitectureEdge> edges
) {
	public ArchitectureGraph {
		nodes = nodes == null ? List.of() : List.copyOf(nodes);
		edges = edges == null ? List.of() : List.copyOf(edges);
	}
}
