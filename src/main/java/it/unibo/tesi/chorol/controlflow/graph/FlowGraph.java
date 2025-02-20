package it.unibo.tesi.chorol.controlflow.graph;

import it.unibo.tesi.chorol.controlflow.FlowVisitor;
import it.unibo.tesi.chorol.symbols.interfaces.operations.Operation;
import it.unibo.tesi.chorol.symbols.interfaces.operations.ReqResOperation;
import it.unibo.tesi.chorol.symbols.services.Service;
import jolie.util.Pair;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.*;

import static it.unibo.tesi.chorol.controlflow.graph.State.createState;

public class FlowGraph extends DefaultDirectedGraph<State, RequestEdge> {
	private State startNode;
	private State endNode;

	public FlowGraph() {
		super(RequestEdge.class);
	}

	public FlowGraph(Service service, String functionName, String opName) {
		super(RequestEdge.class);
		State start = createState(null);
		State end = createState(functionName);
		State middle = createState(null);
		this.setStartNode(start);
		this.setEndNode(end);
		Operation operation = FlowVisitor.getOperation(service, functionName);
		if (operation instanceof ReqResOperation) this.addVertex(middle);
		this.addEdge(start, operation instanceof ReqResOperation ? middle : end)
				.setLabel(
						service.name(),
						functionName,
						operation.getRequestType(),
						opName + (operation instanceof ReqResOperation ? ": REQUEST" : "")
				);
		if (operation instanceof ReqResOperation) this.addEdge(middle, end).setLabel(
				service.name(),
				functionName,
				((ReqResOperation) operation).getResponseType(),
				opName + ": RESPONSE"
		);
	}

	public static void clearGraph(FlowGraph graph) {
		boolean modified;
		do modified = new ArrayList<>(graph.vertexSet()).stream()
				              .filter(node -> !node.equals(graph.getStartNode()))
				              .filter(node -> node.getLabel() != null)
				              .filter(node -> graph.inDegreeOf(node) == 1 || graph.outDegreeOf(node) == 1)
				              .anyMatch(node -> FlowGraph.collapseIfNeeded(graph, node)); while (modified);
	}

	private static Pair<Boolean, String> foo(Set<RequestEdge> edges, RequestEdge otherEdge) {
		boolean allUseless = edges.stream().allMatch(FlowGraph::isUseless);
		boolean noneUseless = edges.stream().noneMatch(FlowGraph::isUseless);
		if (allUseless) return new Pair<>(true, otherEdge.getLabel());
		else if (noneUseless && FlowGraph.isUseless(otherEdge))
			return new Pair<>(true, edges.iterator().next().getLabel());
		return new Pair<>(false, null);
	}

	private static void collapsePair(FlowGraph graph, State nodeA, State nodeB, String newLabel, boolean updateTargetLabel) {
		State defaultPreserved = updateTargetLabel ? nodeA : nodeB;
		State defaultRemoved = updateTargetLabel ? nodeB : nodeA;

		if (defaultPreserved.getStateType() == StateType.NORMAL && defaultRemoved.getStateType() != StateType.NORMAL) {
			State temp = defaultPreserved;
			defaultPreserved = defaultRemoved;
			defaultRemoved = temp;
		}
		State preserved = defaultPreserved;
		State removed = defaultRemoved;

		for (RequestEdge edge : new ArrayList<>(graph.incomingEdgesOf(removed))) {
			State src = graph.getEdgeSource(edge);
			if (!src.equals(preserved)) {
				RequestEdge newEdge = new RequestEdge(newLabel);
				graph.addEdge(src, preserved, newEdge);
			}
			graph.removeEdge(edge);
		}

		for (RequestEdge edge : new ArrayList<>(graph.outgoingEdgesOf(removed))) {
			State tgt = graph.getEdgeTarget(edge);
			if (!tgt.equals(preserved)) {
				if (updateTargetLabel && tgt.getLabel() == null) tgt.setLabel(removed.getLabel());
				RequestEdge newEdge = new RequestEdge(newLabel);
				graph.addEdge(preserved, tgt, newEdge);
			}
			graph.removeEdge(edge);
		}

		graph.removeVertex(removed);
	}

	private static boolean collapseIfNeeded(FlowGraph graph, State node) {
		int inDegree = graph.inDegreeOf(node);
		int outDegree = graph.outDegreeOf(node);
		boolean collapse = false;
		String newLabel;

		if (inDegree == 1 && outDegree == 1) {
			RequestEdge inEdge = graph.incomingEdgesOf(node).iterator().next();
			RequestEdge outEdge = graph.outgoingEdgesOf(node).iterator().next();
			boolean inEdgeIsUseless = FlowGraph.isUseless(inEdge);
			boolean outEdgeIsUseless = FlowGraph.isUseless(outEdge);
			if (inEdgeIsUseless || outEdgeIsUseless) {
				collapse = true;
				newLabel = outEdgeIsUseless ? inEdge.getLabel() : outEdge.getLabel();
				State predecessor = graph.getEdgeSource(inEdge);
				FlowGraph.collapsePair(graph, predecessor, node, newLabel, true);
			}
		} else if (inDegree == 1 && outDegree > 1) {
			RequestEdge inEdge = graph.incomingEdgesOf(node).iterator().next();
			Pair<Boolean, String> pair = FlowGraph.foo(graph.outgoingEdgesOf(node), inEdge);
			collapse = pair.key();
			newLabel = pair.value();
			if (collapse) {
				State predecessor = graph.getEdgeSource(inEdge);
				FlowGraph.collapsePair(graph, predecessor, node, newLabel, true);
			}
		} else if (inDegree > 1 && outDegree == 1) {
			RequestEdge outEdge = graph.outgoingEdgesOf(node).iterator().next();
			Pair<Boolean, String> pair = FlowGraph.foo(graph.incomingEdgesOf(node), outEdge);
			collapse = pair.key();
			newLabel = pair.value();
			if (collapse) {
				State successor = graph.getEdgeTarget(outEdge);
				FlowGraph.collapsePair(graph, node, successor, newLabel, false);
			}
		}

		return collapse;
	}

	private static boolean isUseless(RequestEdge edge) {
		return edge.getLabel() == null || edge.getLabel().isEmpty();
	}


	public State getStartNode() {
		return this.startNode;
	}

	public void setStartNode(State startNode) {
		this.startNode = startNode;
		if (!this.vertexSet().contains(startNode)) this.addVertex(startNode);
	}

	public State getEndNode() {
		return this.endNode == null ? this.startNode : this.endNode;
	}

	public void setEndNode(State endNode) {
		this.endNode = endNode;
		if (!this.vertexSet().contains(endNode)) this.addVertex(endNode);
	}

	public void copyGraph(FlowGraph o) {
		o.vertexSet().forEach(v -> {
			if (!this.vertexSet().contains(v)) this.addVertex(v);
		});
		o.edgeSet().forEach(edge -> {
			State source = o.getEdgeSource(edge);
			State target = o.getEdgeTarget(edge);
			if (!this.vertexSet().contains(source)) this.addVertex(source);
			if (!this.vertexSet().contains(target)) this.addVertex(target);
			if (!this.containsEdge(source, target)) this.addEdge(source, target).setLabel(edge.getLabel());
		});
	}

	public FlowGraph joinAfter(FlowGraph o) {
		if (o == null) return this;

		if (this.startNode == null) {
			this.startNode = createState("start");
			this.addVertex(this.startNode);
		}
		if (this.endNode == null) this.endNode = this.startNode;

		if (o.getStartNode() == null) o.setStartNode(createState("start"));
		FlowGraph.clearGraph(o);
		this.copyGraph(o);
		this.addEdge(this.endNode, o.getStartNode());
		this.endNode = o.getEndNode();
		return this;
	}

	public void joinBetween(FlowGraph o, String label) {
		if (o == null) return;
		if (this.startNode == null) {
			this.startNode = createState("start");
			this.addVertex(this.startNode);
		}
		if (this.endNode == null) {
			this.endNode = createState("end");
			this.addVertex(this.endNode);
		}

		if (o.getStartNode() == null) o.setStartNode(createState("start"));
		if (o.getEndNode() == null) o.setEndNode(createState("end"));
		if (this.containsEdge(this.startNode, this.endNode)) this.removeEdge(this.startNode, this.endNode);

		this.copyGraph(o);

		if (!this.containsEdge(this.startNode, o.getStartNode())) {
			this.addEdge(this.startNode, o.getStartNode());
			this.getEdge(this.startNode, o.getStartNode()).setLabel(label);
		}

		if (!this.containsEdge(o.getEndNode(), this.endNode) && !o.getEndNode().getStateType().equals(StateType.END))
			this.addEdge(o.getEndNode(), this.endNode);
	}

	public void relabelNodesBFS() {
		if (this.getStartNode() == null) return;
		Set<State> visited = new HashSet<>();
		Queue<State> queue = new LinkedList<>();
		queue.add(this.getStartNode());
		visited.add(this.getStartNode());

		int counter = 1;
		while (!queue.isEmpty()) {
			State current = queue.poll();
			if (!current.getStateType().equals(StateType.SERVICE)) current.setLabel(String.valueOf(counter++));
			this.outgoingEdgesOf(current).forEach(edge -> {
				State neighbor = this.getEdgeTarget(edge);
				if (!visited.contains(neighbor)) {
					visited.add(neighbor);
					queue.add(neighbor);
				}
			});
		}
	}


}
