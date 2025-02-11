package it.unibo.tesi.chorol.controlflow;

import it.unibo.tesi.chorol.symbols.SymbolManager;

import java.nio.file.Path;

public class FlowManager {
	private final SymbolManager symManager;

	public FlowManager(Path root) {
		symManager = new SymbolManager(root);
	}

}
