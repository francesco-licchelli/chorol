package it.unibo.tesi.chorol.visitor.flow;

import it.unibo.tesi.chorol.symbols.services.Service;
import it.unibo.tesi.chorol.visitor.flow.graph.FlowGraph;

import java.util.HashMap;

public class FlowContext {
	private final Service service;
	private final HashMap<String, FlowGraph> faults = new HashMap<>();
	private boolean inInstall = false;

	FlowContext(final Service service) {
		this.service = service;
	}

	public Service service() {
		return this.service;
	}

	void addFault(String key, FlowGraph fault) {
		this.faults.put(key, fault);
	}

	void removeFaults() {
		this.faults.clear();
	}

	FlowGraph getFault(String key) {
		return this.faults.get(key);
	}

	boolean inInstall() {
		return this.inInstall;
	}

	void setInInstall(boolean inInstall) {
		this.inInstall = inInstall;
	}

}
