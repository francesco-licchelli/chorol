package it.unibo.tesi.chorol.symbols.ports;

import it.unibo.tesi.chorol.symbols.interfaces.InterfaceHolder;
import it.unibo.tesi.chorol.symbols.services.Service;
import jolie.lang.parse.ast.PortInfo;

public class EmbedPort<T extends PortInfo> extends Port<T> {
	private final Service ref;

	public EmbedPort(T portInfo, Service ref) {
		super(portInfo);
		this.ref = ref;
	}

	@Override
	public void bindInterfaces(InterfaceHolder interfaceHolder) {
		super.bindInterfaces(interfaceHolder);
		ref.bindInterfaces(interfaceHolder);
	}

	public Service getService() {
		return ref;
	}

	@Override
	public String toString() {
		return String.format("%s\n--> %s", super.toString(), ref.toString());
	}
}
