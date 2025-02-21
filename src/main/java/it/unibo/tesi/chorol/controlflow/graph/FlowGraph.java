package it.unibo.tesi.chorol.controlflow.graph;

import it.unibo.tesi.chorol.controlflow.FlowVisitor;
import it.unibo.tesi.chorol.symbols.interfaces.operations.OneWayOperation;
import it.unibo.tesi.chorol.symbols.interfaces.operations.Operation;
import it.unibo.tesi.chorol.symbols.interfaces.operations.ReqResOperation;
import it.unibo.tesi.chorol.symbols.services.Service;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static it.unibo.tesi.chorol.controlflow.graph.State.createState;

public class FlowGraph extends DefaultDirectedGraph<State, RequestEdge> {
	private State startNode;
	private State endNode;

	public FlowGraph() {
		super(RequestEdge.class);
	}

	public FlowGraph(Service service, String functionName, String opType) {
		super(RequestEdge.class);
		State start = createState(null);
		State end = createState(null);
		this.setStartNode(start);
		this.setEndNode(end);
		Operation operation = FlowVisitor.getOperation(service, functionName);
		if (operation instanceof OneWayOperation)
			this.addEdge(start, end).setLabel(service.name(), functionName, operation.getRequestType(), opType + " ONE-WAY");
		else {
			State middle = createState(null);
			this.addVertex(middle);
			this.addEdge(start, middle).setLabel(service.name(), functionName, operation.getRequestType(), opType + " REQUEST");
			this.addEdge(middle, end).setLabel(service.name(), functionName, ((ReqResOperation) operation).getResponseType(), opType + " RESPONSE");
		}
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

	public void replace(FlowGraph o) {
		this.edgeSet().stream().toList().forEach(this::removeEdge);
		this.vertexSet().stream().toList().forEach(this::removeVertex);

		o.vertexSet().forEach(this::addVertex);
		o.edgeSet().forEach(edge -> this.addEdge(o.getEdgeSource(edge), o.getEdgeTarget(edge), edge));
		this.setStartNode(o.getStartNode());
		this.setEndNode(o.getEndNode());
	}


}
