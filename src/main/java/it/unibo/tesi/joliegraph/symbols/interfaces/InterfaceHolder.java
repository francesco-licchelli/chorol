package it.unibo.tesi.joliegraph.symbols.interfaces;

import it.unibo.tesi.joliegraph.symbols.interfaces.operations.Operation;
import jolie.lang.parse.ast.InterfaceDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class InterfaceHolder {
	private final HashMap<String, Interface> interfaces = new HashMap<>();

	public void add(InterfaceDefinition interfaceDefinition) {
		if (this.interfaces.get(interfaceDefinition.name()) == null)
			this.interfaces.put(interfaceDefinition.name(), new Interface(interfaceDefinition));
	}

	public HashMap<String, Interface> get() {
		return this.interfaces;
	}

	public Interface get(String name) {
		return this.interfaces.get(name);
	}

	public void replace(InterfaceHolder interfaceHolder) {
		this.interfaces.keySet().stream().filter(interfaceHolder.get().keySet()::contains)
				.forEach(key -> this.interfaces.replace(key, interfaceHolder.get(key)));
	}

	public Operation getOperation(String operationId) {
		return this.interfaces.values().stream()
				       .flatMap(iface -> iface.getOperationHolder().get().entrySet().stream())
				       .filter(entry -> entry.getKey().equals(operationId))
				       .map(Map.Entry::getValue)
				       .findFirst().orElse(null);
	}

	@Override
	public String toString() {
		return this.interfaces.values().stream().map(Interface::toString).collect(Collectors.joining("\n"));
	}

}
