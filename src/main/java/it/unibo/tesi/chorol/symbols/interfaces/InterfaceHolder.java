package it.unibo.tesi.chorol.symbols.interfaces;

import it.unibo.tesi.chorol.symbols.interfaces.operations.Operation;
import jolie.lang.parse.ast.InterfaceDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class InterfaceHolder {
	private final HashMap<String, Interface> interfaces = new HashMap<>();

	public void add(InterfaceDefinition interfaceDefinition) {
		if (interfaces.get(interfaceDefinition.name()) == null)
			interfaces.put(interfaceDefinition.name(), new Interface(interfaceDefinition));
	}

	public HashMap<String, Interface> get() {
		return interfaces;
	}

	public Interface get(String name) {
		return interfaces.get(name);
	}

	public void replace(InterfaceHolder interfaceHolder) {
		interfaces.keySet().stream().filter(interfaceHolder.get().keySet()::contains)
				.forEach(key -> interfaces.replace(key, interfaceHolder.get(key)));
	}

	public Operation getOperation(String operationId) {
		return interfaces.values().stream()
				       .flatMap(iface -> iface.getOperationHolder().get().entrySet().stream())
				       .filter(entry -> entry.getKey().equals(operationId))
				       .map(Map.Entry::getValue)
				       .findFirst().orElse(null);
	}

	@Override
	public String toString() {
		return interfaces.values().stream().map(Interface::toString).collect(Collectors.joining("\n"));
	}

}
