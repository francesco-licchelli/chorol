package it.unibo.tesi.joliegraph.visitor.flow.graph;

import it.unibo.tesi.joliegraph.symbols.types.Type;
import it.unibo.tesi.joliegraph.utils.OutputSettings;
import org.jgrapht.graph.DefaultEdge;

import static it.unibo.tesi.joliegraph.utils.Constants.STRING_FORMAT_OPERATION;

public class RequestEdge extends DefaultEdge {
	private String label;

	public RequestEdge() {
	}

	public RequestEdge(String label) {
		this.setLabel(label);
	}

	boolean isEpsilon() {
		return this.getLabel() == null || this.getLabel().isEmpty();
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	void setLabel(String portName, String operationName, Type operationType, String className) {
		this.label = String.format(
				STRING_FORMAT_OPERATION,
				className,
				operationName,
				portName,
				OutputSettings.getFullType() ? operationType.toString() : operationType.name()
		);
	}

	@Override
	public String toString() {
		return this.label == null ? "" : this.label;
	}
}