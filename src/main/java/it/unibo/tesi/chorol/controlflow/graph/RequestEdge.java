package it.unibo.tesi.chorol.controlflow.graph;

import org.jgrapht.graph.DefaultEdge;

public class RequestEdge extends DefaultEdge {
	private String label;

	public RequestEdge() {
	}

	RequestEdge(String label) {
		setLabel(label);
	}

	public String getLabel() {
		return label;
	}

	void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label == null ? "" : label;
	}
}