package it.unibo.tesi.chorol.controlflow;

import it.unibo.tesi.chorol.symbols.services.Service;

public class FlowContext {
	private final Service service;
	String functionName;

	FlowContext(final Service service) {
		this.service = service;
	}

	public Service service() {
		return this.service;
	}
}
