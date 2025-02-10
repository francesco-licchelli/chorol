package it.unibo.tesi.chorol.symbols.interfaces.operations;

import it.unibo.tesi.chorol.symbols.types.Type;
import it.unibo.tesi.chorol.symbols.types.TypeHolder;
import jolie.lang.parse.ast.RequestResponseOperationDeclaration;
import jolie.lang.parse.ast.types.TypeDefinition;

import java.util.HashMap;
import java.util.Map;

public class ReqResOperation extends Operation {
	protected final HashMap<String, Type> faults = new HashMap<>();
	protected Type responseType;

	public ReqResOperation(RequestResponseOperationDeclaration operationDeclaration) {
		super(operationDeclaration);
		responseType = new Type(operationDeclaration.responseType());
		setFaults(operationDeclaration.faults());
	}

	private void setFaults(Map<String, TypeDefinition> faults) {
		faults.forEach((name, definition) -> this.faults.put(name, TypeHolder.getType(definition)));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!faults.isEmpty()) sb.append("throws: ");
		sb.append(String.join(", ", faults.keySet()));
		return String.format("%s -> %s %s", super.toString(), responseType.name(), sb);
	}
}
