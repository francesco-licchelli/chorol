package it.unibo.tesi.joliegraph.symbols.interfaces.operations;

import it.unibo.tesi.joliegraph.symbols.types.Type;
import it.unibo.tesi.joliegraph.symbols.types.TypeHolder;
import jolie.lang.parse.ast.RequestResponseOperationDeclaration;
import jolie.lang.parse.ast.types.TypeDefinition;

import java.util.HashMap;
import java.util.Map;

public class ReqResOperation extends Operation {
	private final HashMap<String, Type> faults = new HashMap<>();
	private final Type responseType;

	ReqResOperation(RequestResponseOperationDeclaration operationDeclaration) {
		super(operationDeclaration);
		this.responseType = TypeHolder.getType(operationDeclaration.responseType());
		this.setFaults(operationDeclaration.faults());
	}

	private void setFaults(Map<String, TypeDefinition> faults) {
		faults.forEach((name, definition) -> this.faults.put(name, TypeHolder.getType(definition)));
	}

	public Type getResponseType() {
		return this.responseType;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!this.faults.isEmpty()) sb.append("throws: ");
		sb.append(String.join(", ", this.faults.keySet()));
		return String.format("%s -> %s %s", super.toString(), this.responseType.name(), sb);
	}
}
