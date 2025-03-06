package it.unibo.tesi.joliegraph.symbols.interfaces.operations;

import jolie.lang.parse.ast.OneWayOperationDeclaration;
import jolie.lang.parse.ast.OperationDeclaration;
import jolie.lang.parse.ast.RequestResponseOperationDeclaration;

import java.util.HashMap;
import java.util.stream.Collectors;

public class OperationHolder {
	private final HashMap<String, Operation> operations = new HashMap<>();

	public OperationHolder() {
	}

	public void add(String operationName, OperationDeclaration operationDeclaration) {
		if (operationDeclaration instanceof OneWayOperationDeclaration)
			this.operations.put(operationName, new OneWayOperation((OneWayOperationDeclaration) operationDeclaration));
		else
			this.operations.put(operationName, new ReqResOperation((RequestResponseOperationDeclaration) operationDeclaration));
	}

	public Operation get(String operationName) {
		return this.operations.get(operationName);
	}

	public HashMap<String, Operation> get() {
		return this.operations;
	}

	@Override
	public String toString() {
		return this.operations.entrySet().stream()
				       .map(entry -> {
					       String key = entry.getKey();
					       Operation operation = entry.getValue();
					       return String.format("%s: %s", key, operation);
				       }).collect(Collectors.joining("\n"));
	}

}
