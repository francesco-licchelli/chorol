package it.unibo.tesi.chorol.controlflow;

import it.unibo.tesi.chorol.symbols.SymbolManager;
import it.unibo.tesi.chorol.utils.Misc;
import jolie.lang.Constants.ExecutionMode;
import jolie.lang.parse.ast.*;
import jolie.lang.parse.util.ProgramInspector;
import jolie.lang.parse.util.impl.ProgramInspectorCreatorVisitor;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class FlowManager {
	private DefinitionNode main;

	public FlowManager(Path root) {
		SymbolManager symManager = new SymbolManager(root);
		ProgramInspector inspector = new ProgramInspectorCreatorVisitor(Objects.requireNonNull(Misc.loadProgram(root.toUri()))).createInspector();
		setExecMode(inspector);
		setMain(inspector);
		ParallelStatement ps = (ParallelStatement) main.body();
		SequenceStatement ss = (SequenceStatement) ps.children().get(0);
	}

	private void setExecMode(ProgramInspector inspector) {
		Queue<ServiceNode> q = new LinkedList<>(List.of(inspector.getServiceNodes()));
		while (!q.isEmpty()) {
			Program p = q.remove().program();
			ExecutionMode execMode = p.children().stream()
					                         .filter(a -> a instanceof ExecutionInfo)
					                         .map(a -> ((ExecutionInfo) a).mode())
					                         .findFirst().orElse(ExecutionMode.SINGLE);

			p.children().stream()
					.filter(a -> a instanceof ServiceNode)
					.map(a -> (ServiceNode) a)
					.forEach(q::add);
		}
	}

	private void setMain(ProgramInspector inspector) {
		this.main = inspector.getServiceNodes()[0].program().children().stream()
				            .filter(node -> node instanceof DefinitionNode)
				            .map(node -> (DefinitionNode) node)
				            .filter(node -> node.id().equals("main"))
				            .findFirst().orElseThrow(RuntimeException::new);

	}
}
