package it.unibo.tesi.chorol.symbols.ports;

import it.unibo.tesi.chorol.symbols.interfaces.InterfaceHolder;
import it.unibo.tesi.chorol.symbols.interfaces.operations.Operation;
import it.unibo.tesi.chorol.symbols.services.Service;
import jolie.lang.parse.ast.PortInfo;

import java.util.HashMap;
import java.util.Map;
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
		this.ports.values()
				.forEach(port -> port.bindInterfaces(interfaceHolder1));
	}

	public Port<T> get(String id) {
		return this.ports.get(id);
	}

	public Operation getOperation(String operationId) {
		return this.ports.values().stream()
				       .flatMap(port -> port.getInterfaceHolder().get().entrySet().stream())
				       .flatMap(entry -> entry.getValue().getOperationHolder().get().entrySet().stream())
				       .filter(entry -> entry.getKey().equals(operationId))
				       .map(Map.Entry::getValue).findFirst().orElse(null);
	}

	@Override
	public String toString() {
		return this.ports.values().stream().map(Port::toString).collect(Collectors.joining("\n"));
	}

}
