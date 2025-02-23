package it.unibo.tesi.chorol.symbols.services;

import it.unibo.tesi.chorol.symbols.interfaces.InterfaceHolder;
import jolie.lang.parse.ast.ServiceNode;

import java.util.HashMap;
import java.util.stream.Collectors;

public class ServiceHolder {
	private final HashMap<String, Service> services = new HashMap<>();

	public void add(ServiceNode serviceNode) {
		this.services.put(serviceNode.name(), new Service(serviceNode, this));
	}

	public Service get(String serviceName) {
		return this.services.get(serviceName);
	}

	public void bindInterfaces(InterfaceHolder interfaces) {
		this.services.values().forEach(service -> service.bindInterfaces(interfaces));
	}


	@Override
	public String toString() {
		return this.services.values().stream().map(Service::toString).collect(Collectors.joining("\n"));
	}
}
