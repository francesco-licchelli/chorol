package it.unibo.tesi.chorol.symbols.interfaces;

import it.unibo.tesi.chorol.symbols.interfaces.operations.OperationHolder;
import jolie.lang.parse.ast.InterfaceDefinition;
import jolie.lang.parse.ast.OperationDeclaration;

import java.util.Map.Entry;

public class Interface {
	private final String name;
	private final OperationHolder operationHolder = new OperationHolder();

	Interface(InterfaceDefinition interfaceDefinition) {
		this.name = interfaceDefinition.name();
		for (Entry<String, OperationDeclaration> entry : interfaceDefinition.operationsMap().entrySet()) {
			operationHolder.add(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public String toString() {
		return String.format("%s:\n%s", name, operationHolder);
	}

	public String name() {
		return name;
	}

}
