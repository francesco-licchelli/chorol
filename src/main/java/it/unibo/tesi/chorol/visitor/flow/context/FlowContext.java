package it.unibo.tesi.chorol.visitor.flow.context;

import it.unibo.tesi.chorol.symbols.services.Service;

public class FlowContext {
	private final Service service;
	private final FaultManager faultManager = new FaultManager();

	public FlowContext(final Service service) {
		this.service = service;
	}

	public Service service() {
		return this.service;
	}

	public FaultManager faultManager() {
		return this.faultManager;
	}

}
