package it.unibo.tesi.chorol.symbols.ports;

import it.unibo.tesi.chorol.symbols.interfaces.InterfaceHolder;
import it.unibo.tesi.chorol.symbols.services.Service;
import jolie.lang.parse.ast.PortInfo;

import java.util.HashMap;
import java.util.stream.Collectors;

public class PortHolder<T extends PortInfo> {
	private final HashMap<String, Port<T>> ports = new HashMap<>();

	public PortHolder() {
	}

	public void add(T portInfo) {
		Port<T> port = new Port<>(portInfo);
		this.ports.put(portInfo.id(), port);
	}

	public void add(T portInfo, Service service) {
		EmbedPort<T> port = new EmbedPort<>(portInfo, service);
		this.ports.put(portInfo.id(), port);
	}

	public void bindInterfaces(InterfaceHolder interfaceHolder1) {
		ports.values()
				.forEach(port -> port.bindInterfaces(interfaceHolder1));
	}

	@Override
	public String toString() {
		return ports.values().stream().map(Port::toString).collect(Collectors.joining("\n"));
	}

}
