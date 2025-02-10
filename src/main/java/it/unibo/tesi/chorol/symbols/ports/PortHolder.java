package it.unibo.tesi.chorol.symbols.ports;

import it.unibo.tesi.chorol.symbols.interfaces.InterfaceHolder;
import jolie.lang.parse.ast.PortInfo;

import java.util.HashMap;
import java.util.stream.Collectors;

public class PortHolder<T extends PortInfo> {
	private final HashMap<String, Port<T>> ports = new HashMap<>();

	public PortHolder() {
	}

	public void add(T inputPortInfo) {
		Port<T> port = new Port<>(inputPortInfo);
		this.ports.put(inputPortInfo.id(), port);
	}

	public void bindInterfaces(InterfaceHolder interfaceHolder) {
		ports.values().forEach(port -> port.bindInterfaces(interfaceHolder));
	}

	@Override
	public String toString() {
		return ports.values().stream().map(Port::toString).collect(Collectors.joining("\n"));
	}

}
