package it.unibo.tesi.chorol.visitor.flow.graph;

import it.unibo.tesi.chorol.symbols.interfaces.operations.OneWayOperation;
import it.unibo.tesi.chorol.symbols.interfaces.operations.Operation;
import it.unibo.tesi.chorol.symbols.interfaces.operations.ReqResOperation;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static it.unibo.tesi.chorol.visitor.flow.graph.State.createState;

public class FlowGraph extends DefaultDirectedGraph<State, RequestEdge> {
	private State startNode;
	private State endNode;

	public FlowGraph() {
		super(RequestEdge.class);
	}

	/**
	 * Costruttore che crea un FlowGraph in base alle operazioni.
	 *
	 * @param serviceName il nome del servizio
	 * @param operation   l'operazione corrente
	 * @param opType      il tipo di operazione (Input/Output)
	 * @param process     eventuale grafo di processo da collegare
	 */
	public FlowGraph(String serviceName, Operation operation, String opType, FlowGraph process) {
		super(RequestEdge.class);
		State start = createState();
		State end = createState();
		this.setStartNode(start);
		this.setEndNode(end);

		// Operazione OneWay
		if (operation instanceof OneWayOperation)
			this.addEdge(start, end).setLabel(serviceName, operation.getName(), operation.getRequestType(), opType + " ONE-WAY");
		else if (operation instanceof ReqResOperation) {
			// Operazione Request-Response
			State middle = createState();
			this.addVertex(middle);
			this.addEdge(start, middle).setLabel(
					serviceName,
					operation.getName(),
					operation.getRequestType(),
					opType + " REQUEST"
			);
			this.addEdge(middle, end).setLabel(
					serviceName,
					operation.getName(),
					((ReqResOperation) operation).getResponseType(),
					opType + " RESPONSE"
			);
		}

		// Se esiste un processo non vuoto, uniscilo al grafo
		if (process != null && process.containsInformation()) this.joinAfter(process);
	}

	/**
	 * Verifica se il grafo contiene informazioni (edge non epsilon).
	 */
	public boolean containsInformation() {
		return this.edgeSet().stream().anyMatch(edge -> !edge.isEpsilon());
	}

	public State getStartNode() {
		return this.startNode;
	}

	public void setStartNode(State startNode) {
		this.startNode = startNode;
		if (!this.vertexSet().contains(startNode)) this.addVertex(startNode);
	}

	/**
	 * Se endNode è nullo, restituisce lo startNode.
	 */
	public State getEndNode() {
		return this.endNode == null ? this.startNode : this.endNode;
	}

	public void setEndNode(State endNode) {
		this.endNode = endNode;
		if (!this.vertexSet().contains(endNode)) this.addVertex(endNode);
	}

	/**
	 * Copia i nodi e gli archi di un altro FlowGraph in quello corrente.
	 */
	public void copyGraph(FlowGraph o) {
		// Copia tutti i nodi
		o.vertexSet().forEach(v -> {
			if (!this.vertexSet().contains(v)) this.addVertex(v);
		});
		// Copia tutti gli archi
		o.edgeSet().forEach(edge -> {
			State source = o.getEdgeSource(edge);
			State target = o.getEdgeTarget(edge);
			if (!this.vertexSet().contains(source)) this.addVertex(source);
			if (!this.vertexSet().contains(target)) this.addVertex(target);
			if (!this.containsEdge(source, target)) this.addEdge(source, target).setLabel(edge.getLabel());
		});
	}

	/**
	 * Collega il grafo 'o' al termine di quello corrente, spostando l'endNode.
	 */
	public FlowGraph joinAfter(FlowGraph o) {
		if (o == null || !o.containsInformation()) return this;
		if (this.startNode == null) {
			this.startNode = createState();
			this.addVertex(this.startNode);
		}
		if (this.endNode == null) this.endNode = this.startNode;
		this.copyGraph(o);
		this.addEdge(this.endNode, o.getStartNode());
		this.setEndNode(o.getEndNode());
		return this;
	}

	/**
	 * Collega un grafo 'o' tra lo startNode e l'endNode di quello corrente, rimuovendo l'arco diretto se già esiste.
	 */
	public void joinBetween(FlowGraph o, String label) {
		if (o == null || !o.containsInformation()) return;
		if (this.startNode == null) {
			this.startNode = createState();
			this.addVertex(this.startNode);
		}
		if (this.endNode == null) {
			this.endNode = createState();
			this.addVertex(this.endNode);
		}
		// Rimuove l'arco diretto dallo startNode all'endNode, se esiste
		if (this.containsEdge(this.startNode, this.endNode)) this.removeEdge(this.startNode, this.endNode);
		// Copia il grafo 'o'
		this.copyGraph(o);
		// Crea l'arco (startNode -> o.startNode), con eventuale label
		if (!this.containsEdge(this.startNode, o.getStartNode()))
			this.addEdge(this.startNode, o.getStartNode()).setLabel(label);
		// Crea l'arco (o.endNode -> endNode), se il nodo finale di 'o' non è di tipo END/EXIT
		if (!this.containsEdge(o.getEndNode(), this.endNode)
				    && !o.getEndNode().getStateType().equals(StateType.END)
				    && !o.getEndNode().getStateType().equals(StateType.EXIT))
			this.addEdge(o.getEndNode(), this.endNode);
	}

	/**
	 * Esegue una BFS per rietichettare i nodi, saltando i nodi di tipo SERVICE.
	 */
	public void relabelNodesBFS() {
		if (this.getStartNode() == null) return;
		Set<State> visited = new HashSet<>();
		Queue<State> queue = new LinkedList<>();
		queue.add(this.getStartNode());
		visited.add(this.getStartNode());

		int counter = 1;
		while (!queue.isEmpty()) {
			State current = queue.poll();
			// Cambia etichetta solo se non è un SERVICE
			if (!current.getStateType().equals(StateType.SERVICE)) current.setLabel(String.valueOf(counter++));
			// Aggiunge i vicini non ancora visitati
			this.outgoingEdgesOf(current).forEach(edge -> {
				State neighbor = this.getEdgeTarget(edge);
				if (!visited.contains(neighbor)) {
					visited.add(neighbor);
					queue.add(neighbor);
				}
			});
		}
	}

	/**
	 * Sostituisce completamente il contenuto del grafo corrente con quello di 'o'.
	 */
	public void replace(FlowGraph o) {
		// Rimuove tutti gli archi
		this.edgeSet().stream().toList().forEach(this::removeEdge);
		// Rimuove tutti i nodi
		this.vertexSet().stream().toList().forEach(this::removeVertex);
		// Copia nodi e archi da 'o'
		o.vertexSet().forEach(this::addVertex);
		o.edgeSet().forEach(edge -> this.addEdge(o.getEdgeSource(edge), o.getEdgeTarget(edge), edge));
		this.setStartNode(o.getStartNode());
		this.setEndNode(o.getEndNode());
	}


}