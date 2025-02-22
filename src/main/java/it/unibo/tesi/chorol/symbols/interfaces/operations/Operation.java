package it.unibo.tesi.chorol.symbols.interfaces.operations;

import it.unibo.tesi.chorol.symbols.types.Type;
import it.unibo.tesi.chorol.symbols.types.TypeHolder;
import jolie.lang.parse.ast.OneWayOperationDeclaration;
import jolie.lang.parse.ast.OperationDeclaration;
import jolie.lang.parse.ast.RequestResponseOperationDeclaration;
import jolie.lang.parse.ast.types.TypeDefinition;

public abstract class Operation {
	protected String name;
	private Type requestType;

	Operation(OperationDeclaration operationDeclaration) {
		this.name = operationDeclaration.id();
		this.setRequestType(operationDeclaration);
	}

	public String getName() {
		return this.name;
	}

	public Type getRequestType() {
		return this.requestType;
	}

	private void setRequestType(OperationDeclaration operationDeclaration) {
		TypeDefinition requestType = null;
		if (operationDeclaration instanceof OneWayOperationDeclaration)
			requestType = ((OneWayOperationDeclaration) operationDeclaration).requestType();
		else if (operationDeclaration instanceof RequestResponseOperationDeclaration)
			requestType = ((RequestResponseOperationDeclaration) operationDeclaration).requestType();
		this.requestType = TypeHolder.getType(requestType);
	}

	@Override
	public String toString() {
		return this.name + ": " + this.requestType.name();
	}
}
