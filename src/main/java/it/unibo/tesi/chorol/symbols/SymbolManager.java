package it.unibo.tesi.chorol.symbols;


import it.unibo.tesi.chorol.symbols.interfaces.InterfaceHolder;
import it.unibo.tesi.chorol.symbols.ports.PortHolder;
import it.unibo.tesi.chorol.symbols.types.TypeHolder;
import it.unibo.tesi.chorol.utils.Misc;
import jolie.lang.parse.ast.InputPortInfo;
import jolie.lang.parse.ast.OutputPortInfo;
import jolie.lang.parse.util.ProgramInspector;
import jolie.lang.parse.util.impl.ProgramInspectorCreatorVisitor;
import jolie.lang.parse.util.impl.ProgramInspectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static it.unibo.tesi.chorol.utils.Misc.getFilesPaths;


public class SymbolManager {
	private static final Logger logger = LoggerFactory.getLogger(SymbolManager.class);
	private final TypeHolder typeHolder = new TypeHolder();
	private final InterfaceHolder interfaceHolder = new InterfaceHolder();
	private final PortHolder<InputPortInfo> inputPortHolder = new PortHolder<>();
	private final PortHolder<OutputPortInfo> outputPortHolder = new PortHolder<>();

	public SymbolManager(Path root) {
		getFilesPaths(root).stream()
				.map(Misc::loadProgram)
				.filter(Objects::nonNull)
				.peek(program -> logger.info("Program loaded: {}", program.context().source().toString()))
				.map(program ->
						     (ProgramInspectorImpl) new ProgramInspectorCreatorVisitor(program).createInspector()
				).forEach(this::loadInspectorSymbols);

		inputPortHolder.bindInterfaces(interfaceHolder);
		outputPortHolder.bindInterfaces(interfaceHolder);
		logger.info("LOADED TYPES:\n{}\n{}", typeHolder.toString().trim(), "-".repeat(10));
		logger.info("LOADED INTERFACES:\n{}\n{}", interfaceHolder.toString().trim(), "-".repeat(10));
		logger.info("LOADED INPUT PORTS:\n{}\n{}", inputPortHolder.toString().trim(), "-".repeat(10));
		logger.info("LOADED OUTPUT PORTS:\n{}\n{}", outputPortHolder.toString().trim(), "-".repeat(10));
	}

	private void loadInspectorSymbols(ProgramInspector inspector) {
		Arrays.stream(inspector.getTypes()).forEach(typeHolder::add);
		Arrays.stream(inspector.getInterfaces()).forEach(interfaceHolder::add);
		Arrays.stream(inspector.getInputPorts()).forEach(inputPortHolder::add);
		Arrays.stream(inspector.getOutputPorts()).forEach(outputPortHolder::add);
	}

}
