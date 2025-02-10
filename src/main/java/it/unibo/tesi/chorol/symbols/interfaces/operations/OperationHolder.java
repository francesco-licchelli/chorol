package it.unibo.tesi.chorol.symbols.interfaces.operations;

import jolie.lang.parse.ast.OneWayOperationDeclaration;
import jolie.lang.parse.ast.OperationDeclaration;
import jolie.lang.parse.ast.RequestResponseOperationDeclaration;

import java.util.HashMap;
import java.util.stream.Collectors;

public class OperationHolder {
	private final HashMap<String, Operation> requests = new HashMap<>();

	public OperationHolder() {
	}

	public void add(String operationName, OperationDeclaration operationDeclaration) {
		if (operationDeclaration instanceof OneWayOperationDeclaration)
			requests.put(operationName, new OneWayOperation((OneWayOperationDeclaration) operationDeclaration));
		else
			requests.put(operationName, new ReqResOperation((RequestResponseOperationDeclaration) operationDeclaration));
	}

	public Operation get(String operationName) {
		return requests.get(operationName);
	}

	@Override
	public String toString() {
		return requests.entrySet().stream()
				       .map(entry -> {
					       String key = entry.getKey();
					       Operation request = entry.getValue();
					       return String.format("%s: %s", key, request);
				       }).collect(Collectors.joining("\n"));
	}

}
