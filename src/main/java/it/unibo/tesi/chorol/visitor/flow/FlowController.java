package it.unibo.tesi.chorol.visitor.flow;

import it.unibo.tesi.chorol.symbols.SymbolManager;
import it.unibo.tesi.chorol.utils.OutputSettings;
import it.unibo.tesi.chorol.visitor.flow.graph.FlowGraph;
import it.unibo.tesi.chorol.visitor.flow.graph.RequestEdge;
import it.unibo.tesi.chorol.visitor.flow.graph.State;
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
	private final HashMap<Path, FlowGraph> flowGraphs = new HashMap<>();

	public FlowController(Path root, Path output) throws IOException {
		SymbolManager symManager = new SymbolManager(root);
		Files.createDirectories(output);
		symManager.getServices().forEach((path, serviceNodePair) -> {
			ServiceNode serviceNode = serviceNodePair.key();
			String newFileName = path.getFileName().toString().replaceFirst("\\.i?ol$", ".dot");
			Path newPath = output.resolve(newFileName);
			FlowVisitor flowVisitor = new FlowVisitor(symManager);
			FlowGraph fg = flowVisitor.visit(serviceNode, new FlowContext(symManager.getServiceHolder().get(serviceNode.name())));
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
