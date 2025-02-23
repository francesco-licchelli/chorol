package it.unibo.tesi.chorol.symbols.ports;

import it.unibo.tesi.chorol.symbols.interfaces.InterfaceHolder;
import it.unibo.tesi.chorol.symbols.interfaces.operations.Operation;
import it.unibo.tesi.chorol.symbols.services.Service;
import jolie.lang.parse.ast.PortInfo;

public class EmbedPort<T extends PortInfo> extends Port<T> {
	private final Service ref;

	EmbedPort(T portInfo, Service ref) {
		super(portInfo);
		this.ref = ref;
	}

	@Override
	public void bindInterfaces(InterfaceHolder interfaceHolder) {
		super.bindInterfaces(interfaceHolder);
		this.ref.bindInterfaces(interfaceHolder);
	}

	@Override
	public Operation getOperation(String operationId) {
		Operation op = super.getOperation(operationId);
		if (op != null) return op;
		return this.getService().getInputPortHolder().getOperation(operationId);
	}

	public Service getService() {
		return this.ref;
	}

	@Override
	public String toString() {
		return String.format("%s\n--> %s", super.toString(), this.ref.toString());
	}
}
