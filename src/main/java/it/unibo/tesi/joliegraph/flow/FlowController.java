package it.unibo.tesi.joliegraph.flow;

import it.unibo.tesi.joliegraph.flow.context.FlowContext;
import it.unibo.tesi.joliegraph.flow.graph.FlowGraph;
import it.unibo.tesi.joliegraph.flow.graph.RequestEdge;
import it.unibo.tesi.joliegraph.flow.graph.State;
import it.unibo.tesi.joliegraph.symbols.SymbolManager;
import it.unibo.tesi.joliegraph.utils.OutputSettings;
import jolie.lang.parse.ast.ServiceNode;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.ExportException;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jgrapht.nio.DefaultAttribute.createAttribute;

public class FlowController {
	private final SymbolManager symManager;
	private final HashMap<Path, FlowGraph> flowGraphs = new HashMap<>();

	public FlowController(Path root, Path output) throws IOException {
		this.symManager = new SymbolManager(root);
		Files.createDirectories(output);
		this.symManager.getServices().forEach((path, serviceNodePair) -> {
			ServiceNode serviceNode = serviceNodePair.key();
			String newFileName = path.getFileName().toString().replaceFirst("\\.i?ol$", ".dot");
			Path newPath = output.resolve(newFileName);
			FlowVisitor flowVisitor = new FlowVisitor(this.symManager);
			FlowGraph fg = flowVisitor.visit(serviceNode, new FlowContext(this.symManager.getServiceHolder().get(serviceNode.name())));
			if (OutputSettings.shouldSaveStdLib() || serviceNodePair.value())
				this.flowGraphs.put(newPath, fg);
		});
	}

	private static DOTExporter<State, RequestEdge> getStateRequestEdgeDOTExporter() {
		DOTExporter<State, RequestEdge> exporter = new DOTExporter<>(State::getId);

		exporter.setVertexAttributeProvider(state -> {
			Map<String, Attribute> map = new LinkedHashMap<>();
			map.put("label", createAttribute(state.toPrettyString()));
			switch (state.getStateType()) {
				case END:
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

	public void save() {
		DOTExporter<State, RequestEdge> exporter = FlowController.getStateRequestEdgeDOTExporter();
		this.flowGraphs.forEach((path, flowGraph) -> {
			try (Writer writer = new FileWriter(path.toFile())) {
				exporter.exportGraph(flowGraph, writer);
			} catch (IOException | ExportException e) {
				e.printStackTrace();
			}
		});
	}
}
