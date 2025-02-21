package it.unibo.tesi.chorol.symbols;


import it.unibo.tesi.chorol.symbols.interfaces.InterfaceHolder;
import it.unibo.tesi.chorol.symbols.services.ServiceHolder;
import it.unibo.tesi.chorol.symbols.types.TypeHolder;
import jolie.lang.parse.ast.InterfaceDefinition;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.ast.ServiceNode;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.module.ModuleException;
import jolie.lang.parse.module.ModuleFinderImpl;
import jolie.lang.parse.module.SymbolTable;
import jolie.lang.parse.module.SymbolTableGenerator;
import jolie.lang.parse.module.exceptions.ModuleNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static it.unibo.tesi.chorol.utils.Misc.loadProgram;


public class SymbolManager {
	private static final Logger logger = LoggerFactory.getLogger(SymbolManager.class);
	private final TypeHolder typeHolder = new TypeHolder();
	private final InterfaceHolder interfaceHolder = new InterfaceHolder();
	private final ServiceHolder serviceHolder = new ServiceHolder();

	public SymbolManager(Path root) {
		this.loadSymbols(root);
		this.serviceHolder.bindInterfaces(this.interfaceHolder);

		SymbolManager.logger.info("LOADED TYPES:\n{}\n{}", this.typeHolder.toString().trim(), "-".repeat(10));
		SymbolManager.logger.info("LOADED INTERFACES:\n{}\n{}", this.interfaceHolder.toString().trim(), "-".repeat(10));
		SymbolManager.logger.info("LOADED SERVICES:\n{}\n{}", this.serviceHolder.toString().trim(), "-".repeat(10));
	}

	private void loadSymbols(Path source) {
		this.loadSymbolsRec(source.toUri(), new HashSet<>());
	}

	private void loadSymbolsRec(URI source, Set<String> visited) {
		if (visited.contains(source.toString())) return;
		visited.add(source.toString());

		Program program = loadProgram(source);
		if (program == null) return;
		try {
			SymbolTable symbolTable = SymbolTableGenerator.generate(program);
			Arrays.stream(symbolTable.importedSymbolInfos())
					.forEach(symbol -> {
						try {
							this.loadSymbolsRec(
									new ModuleFinderImpl(
											Paths.get(System.getProperty("user.dir")).toUri(),
											new String[]{String.valueOf(Paths.get(Paths.get(source).getParent().toUri())), System.getenv("JOLIE_HOME") + "/packages"}
									).find(source, symbol.importPath()).uri(),
									visited
							);
						} catch (ModuleNotFoundException e) {
							throw new RuntimeException(e);
						}
					});
			Arrays.stream(symbolTable.localSymbols())
					.sorted(Comparator.comparing(symbol -> symbol.node() instanceof ServiceNode ? 1 : 0))
					.forEach(symbol -> {
						if (symbol.node() instanceof ServiceNode) this.serviceHolder.add((ServiceNode) symbol.node());
						else if (symbol.node() instanceof TypeDefinition)
							this.typeHolder.add((TypeDefinition) symbol.node());
						else if (symbol.node() instanceof InterfaceDefinition)
							this.interfaceHolder.add((InterfaceDefinition) symbol.node());
						else SymbolManager.logger.warn("TODO {} {} {}",
									symbol.name(),
									symbol.context().enclosingCodeWithLineNumbers(),
									symbol.node().getClass().getSimpleName()
							);
					});
		} catch (ModuleException e) {
			throw new RuntimeException(e);
		}

	}

	public TypeHolder getTypeHolder() {
		return this.typeHolder;
	}

	public InterfaceHolder getInterfaceHolder() {
		return this.interfaceHolder;
	}

	public ServiceHolder getServiceHolder() {
		return this.serviceHolder;
	}
}
