package it.unibo.tesi.chorol.visitor.flow.graph;

import it.unibo.tesi.chorol.symbols.types.Type;
import it.unibo.tesi.chorol.utils.OutputSettings;
import org.jgrapht.graph.DefaultEdge;

import static it.unibo.tesi.chorol.utils.Constants.STRING_FORMAT_OPERATION;

public class RequestEdge extends DefaultEdge {
	private String label;

	public RequestEdge() {
	}

	public RequestEdge(String label) {
		this.setLabel(label);
	}

	public String getLabel() {
		return this.label;
	}

	void setLabel(String label) {
		this.label = label;
	}

	void setLabel(String serviceName, String functionName, Type operationType, String className) {
		this.label = String.format(
				STRING_FORMAT_OPERATION,
				className,
				functionName,
				serviceName,
				OutputSettings.getFullType() ? operationType.toString() : operationType.name()
		);
	}

	@Override
	public String toString() {
		return this.label == null ? "" : this.label;
	}
}