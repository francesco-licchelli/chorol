package it.unibo.tesi.chorol.symbols.interfaces.operations;

import it.unibo.tesi.chorol.symbols.types.Type;
import it.unibo.tesi.chorol.symbols.types.TypeHolder;
import jolie.lang.parse.ast.OneWayOperationDeclaration;
import jolie.lang.parse.ast.OperationDeclaration;
import jolie.lang.parse.ast.RequestResponseOperationDeclaration;
import jolie.lang.parse.ast.types.TypeDefinition;

public abstract class Operation {
	protected String name;
	protected Type requestType;

	public Operation(OperationDeclaration operationDeclaration) {
		this.name = operationDeclaration.id();
		setRequestType(operationDeclaration);
	}

	public void setRequestType(OperationDeclaration operationDeclaration) {
		TypeDefinition requestType = null;
		if (operationDeclaration instanceof OneWayOperationDeclaration)
			requestType = ((OneWayOperationDeclaration) operationDeclaration).requestType();
		else if (operationDeclaration instanceof RequestResponseOperationDeclaration)
			requestType = ((RequestResponseOperationDeclaration) operationDeclaration).requestType();
		this.requestType = TypeHolder.getType(requestType);
	}

	@Override
	public String toString() {
		return requestType.name();
	}
}
