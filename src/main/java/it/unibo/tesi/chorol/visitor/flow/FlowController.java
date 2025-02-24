package it.unibo.tesi.chorol.visitor.flow;

import it.unibo.tesi.chorol.symbols.SymbolManager;
import it.unibo.tesi.chorol.visitor.flow.graph.FlowGraph;
import it.unibo.tesi.chorol.visitor.flow.graph.RequestEdge;
import it.unibo.tesi.chorol.visitor.flow.graph.State;
import jolie.lang.parse.ast.Program;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.ExportException;
import org.jgrapht.nio.dot.DOTExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static it.unibo.tesi.chorol.utils.Misc.loadProgram;
import static org.jgrapht.nio.DefaultAttribute.createAttribute;

public class FlowController {
	private static final Logger logger = LoggerFactory.getLogger(FlowController.class);

	public FlowController(Path root) {
		SymbolManager symManager = new SymbolManager(root);
		Program main = loadProgram(root.toUri());
		if (main == null) {
			FlowController.logger.error("Could not load program in {}", root.toUri());
			return;
		}
		FlowVisitorBase flowVisitorBase = new FlowVisitor(symManager);
		FlowGraph g = flowVisitorBase.visit(main, null);


		DOTExporter<State, RequestEdge> exporter = FlowController.getStateRequestEdgeDOTExporter();

		try (Writer writer = new FileWriter("flowgraph.dot")) {
			exporter.exportGraph(g, writer);
		} catch (IOException | ExportException e) {
			e.printStackTrace();
		}

	}

	private static DOTExporter<State, RequestEdge> getStateRequestEdgeDOTExporter() {
		DOTExporter<State, RequestEdge> exporter = new DOTExporter<>(State::getId);

		exporter.setVertexAttributeProvider(state -> {
			Map<String, Attribute> map = new LinkedHashMap<>();
			map.put("label", createAttribute(state.toPrettyString()));
			switch (state.getStateType()) {
				case SERVICE:
					map.put("shape", createAttribute("plaintext"));
					break;
				case EXIT:
					map.put("shape", createAttribute("doublecircle"));
					break;
			}
			return map;
		});

		exporter.setEdgeAttributeProvider(edge -> {
			Map<String, Attribute> map = new LinkedHashMap<>();
			map.put("label", createAttribute(edge.toString()));
			return map;
		});

		return exporter;
	}
}
