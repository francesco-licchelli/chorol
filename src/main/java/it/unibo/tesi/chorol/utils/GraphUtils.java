package it.unibo.tesi.chorol.utils;

import it.unibo.tesi.chorol.visitor.flow.graph.FlowGraph;
import it.unibo.tesi.chorol.visitor.flow.graph.RequestEdge;
import it.unibo.tesi.chorol.visitor.flow.graph.State;
import it.unibo.tesi.chorol.visitor.flow.graph.StateType;
import jolie.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class GraphUtils {

	public static void clearGraph(FlowGraph flowGraph) {
		FlowGraph nfaNoEpsilon = GraphUtils.removeEpsilonTransitions(flowGraph);
		FlowGraph dfa = GraphUtils.convertToDFA(nfaNoEpsilon);
		Set<State> dfaFinalStates = new HashSet<>();
		dfaFinalStates.add(dfa.getEndNode());
		flowGraph.replace(GraphUtils.minimizeDFA(dfa, dfa.getStartNode(), dfaFinalStates));
	}

	private static FlowGraph removeEpsilonTransitions(FlowGraph flowGraph) {
		FlowGraph nfa = new FlowGraph();
		Map<State, State> stateMapping = new HashMap<>();
		for (State s : flowGraph.vertexSet()) {
			State nuovoStato = State.createState();
			nfa.addVertex(nuovoStato);
			stateMapping.put(s, nuovoStato);
		}
		Map<State, Set<State>> epsilonClosures = new HashMap<>();
		for (State s : flowGraph.vertexSet()) {
			Set<State> closure = GraphUtils.computeEpsilonClosure(flowGraph, s);
			epsilonClosures.put(s, closure);
			if (closure.stream().anyMatch(t -> t.getStateType().equals(StateType.FAULT)))
				stateMapping.get(s).setStateType(StateType.FAULT);
			if (closure.stream().anyMatch(t -> t.getStateType().equals(StateType.END)))
				stateMapping.get(s).setStateType(StateType.END);
			if (closure.stream().anyMatch(t -> t.getStateType().equals(StateType.EXIT)))
				stateMapping.get(s).setStateType(StateType.EXIT);
		}
		for (State s : flowGraph.vertexSet()) {
			Set<State> closure = epsilonClosures.get(s);
			for (State t : closure)
				for (RequestEdge edge : flowGraph.outgoingEdgesOf(t)) {
					String label = edge.getLabel();
					if (label != null && !label.isEmpty()) {
						State target = flowGraph.getEdgeTarget(edge);
						nfa.addEdge(stateMapping.get(s), stateMapping.get(target), new RequestEdge(label));
					}
				}
		}

		nfa.setStartNode(stateMapping.get(flowGraph.getStartNode()));
		nfa.setEndNode(stateMapping.get(flowGraph.getEndNode()));
		return nfa;
	}

	/**
	 * Converte l'NFA (senza epsilon) in un DFA usando la subset construction.
	 * Se in un insieme di stati è presente un nodo di tipo END,
	 * il corrispondente stato DFA viene marcato come END.
	 */
	private static FlowGraph convertToDFA(FlowGraph nfa) {
		FlowGraph dfa = new FlowGraph();
		Map<Set<State>, State> dfaStates = new HashMap<>();
		Queue<Set<State>> queue = new LinkedList<>();

		// Stato iniziale del DFA: insieme contenente solo il nodo iniziale dell'NFA
		Set<State> startSet = new HashSet<>();
		startSet.add(nfa.getStartNode());
		State dfaStart = State.createState();
		if (startSet.stream().anyMatch(t -> t.getStateType().equals(StateType.FAULT)))
			dfaStart.setStateType(StateType.FAULT);
		if (startSet.stream().anyMatch(s -> s.getStateType().equals(StateType.END)))
			dfaStart.setStateType(StateType.END);
		if (startSet.stream().anyMatch(s -> s.getStateType().equals(StateType.EXIT)))
			dfaStart.setStateType(StateType.EXIT);

		dfa.addVertex(dfaStart);
		dfaStates.put(startSet, dfaStart);
		queue.add(startSet);

		Set<State> dfaFinalStates = new HashSet<>();
		if (startSet.contains(nfa.getEndNode()))
			dfaFinalStates.add(dfaStart);

		// Costruzione del DFA tramite subset construction
		while (!queue.isEmpty()) {
			Set<State> currentSet = queue.poll();
			State currentDfaState = dfaStates.get(currentSet);

			Map<String, Set<State>> transitions = new HashMap<>();
			for (State s : currentSet)
				for (RequestEdge edge : nfa.outgoingEdgesOf(s)) {
					String symbol = edge.getLabel();
					if (symbol != null && !symbol.isEmpty()) {
						State target = nfa.getEdgeTarget(edge);
						transitions.putIfAbsent(symbol, new HashSet<>());
						transitions.get(symbol).add(target);
					}
				}

			for (Map.Entry<String, Set<State>> entry : transitions.entrySet()) {
				String symbol = entry.getKey();
				Set<State> destSet = entry.getValue();
				if (!dfaStates.containsKey(destSet)) {
					State newDfaState = State.createState();
					if (destSet.stream().anyMatch(s -> s.getStateType().equals(StateType.FAULT)))
						newDfaState.setStateType(StateType.FAULT);
					if (destSet.stream().anyMatch(s -> s.getStateType().equals(StateType.END)))
						newDfaState.setStateType(StateType.END);
					if (destSet.stream().anyMatch(s -> s.getStateType().equals(StateType.EXIT)))
						newDfaState.setStateType(StateType.EXIT);
					dfa.addVertex(newDfaState);
					dfaStates.put(destSet, newDfaState);
					queue.add(destSet);
					if (destSet.contains(nfa.getEndNode()))
						dfaFinalStates.add(newDfaState);
				}
				dfa.addEdge(currentDfaState, dfaStates.get(destSet), new RequestEdge(symbol));
			}
		}

		dfa.setStartNode(dfaStates.get(startSet));
		State dfaFinal = dfaFinalStates.isEmpty() ? dfa.getStartNode() : dfaFinalStates.iterator().next();
		dfa.setEndNode(dfaFinal);
		return dfa;
	}

	/**
	 * Calcola la epsilon closure per un dato stato.
	 */
	private static Set<State> computeEpsilonClosure(FlowGraph graph, State stato) {
		Set<State> closure = new HashSet<>();
		Stack<State> stack = new Stack<>();
		closure.add(stato);
		stack.push(stato);
		while (!stack.isEmpty()) {
			State s = stack.pop();
			for (RequestEdge edge : graph.outgoingEdgesOf(s)) {
				String label = edge.getLabel();
				if (label == null || label.isEmpty()) { // transizione epsilon
					State target = graph.getEdgeTarget(edge);
					if (!closure.contains(target)) {
						closure.add(target);
						stack.push(target);
					}
				}
			}
		}
		return closure;
	}

	private static FlowGraph minimizeDFA(FlowGraph dfa, State dfaStart, Set<State> dfaFinalStates) {
		Set<State> allStates = new HashSet<>(dfa.vertexSet());
		Set<State> finalStates = new HashSet<>(dfaFinalStates);
		Set<State> nonFinalStates = new HashSet<>(allStates);
		nonFinalStates.removeAll(finalStates);

		Set<Set<State>> partitions = new HashSet<>();
		if (!finalStates.isEmpty())
			partitions.add(finalStates);
		if (!nonFinalStates.isEmpty())
			partitions.add(nonFinalStates);

		boolean changed = true;
		while (changed) {
			changed = false;
			Set<Set<State>> newPartitions = new HashSet<>();
			for (Set<State> group : partitions) {
				// Utilizziamo una mappa per raccogliere gli stati in base alla loro "firma"
				Map<String, Set<State>> splitter = new HashMap<>();
				for (State s : group) {
					// Costruiamo la firma: per ogni etichetta, accumuliamo tutte le partizioni di destinazione
					Map<String, List<Set<State>>> signature = new HashMap<>();
					for (RequestEdge edge : dfa.outgoingEdgesOf(s)) {
						String symbol = edge.getLabel();
						State target = dfa.getEdgeTarget(edge);
						// Trova la partizione contenente il target
						for (Set<State> p : partitions)
							if (p.contains(target)) {
								signature.computeIfAbsent(symbol, k -> new ArrayList<>()).add(p);
								break;
							}
					}
					// Per poter usare la firma come chiave, la convertiamo in una stringa ordinata
					String signatureKey = signature.entrySet().stream()
							                      .sorted(Map.Entry.comparingByKey())
							                      .map(e -> e.getKey() + ":" +
									                                e.getValue().stream()
											                                .map(Object::hashCode)
											                                .sorted()
											                                .map(String::valueOf)
											                                .collect(Collectors.joining(",")))
							                      .collect(Collectors.joining(";"));
					splitter.computeIfAbsent(signatureKey, k -> new HashSet<>()).add(s);
				}
				if (splitter.size() > 1) {
					changed = true;
					newPartitions.addAll(splitter.values());
				} else newPartitions.add(group);
			}
			partitions = newPartitions;
		}

		FlowGraph minimizzato = new FlowGraph();
		Map<Set<State>, State> partitionMapping = new HashMap<>();
		for (Set<State> part : partitions) {
			State newState = State.createState();
			if (part.stream().anyMatch(s -> s.getStateType().equals(StateType.FAULT)))
				newState.setStateType(StateType.FAULT);
			if (part.stream().anyMatch(s -> s.getStateType().equals(StateType.END)))
				newState.setStateType(StateType.END);
			if (part.stream().anyMatch(s -> s.getStateType().equals(StateType.EXIT)))
				newState.setStateType(StateType.EXIT);
			minimizzato.addVertex(newState);
			partitionMapping.put(part, newState);
		}

		State minStart = null;
		State minFinal = null;
		for (Set<State> part : partitions) {
			if (part.contains(dfaStart))
				minStart = partitionMapping.get(part);
			for (State s : part)
				if (dfaFinalStates.contains(s)) {
					minFinal = partitionMapping.get(part);
					break;
				}
		}
		if (minFinal == null)
			minFinal = minStart;

		// Ricostruzione delle transizioni: accumuliamo TUTTE le transizioni (anche se per lo stesso simbolo)
		for (Set<State> part : partitions) {
			State statoMin = partitionMapping.get(part);
			// Utilizziamo una lista per mantenere tutte le coppie (etichetta, stato di destinazione)
			List<Pair<String, State>> transizioni = new ArrayList<>();
			for (State s : part)
				for (RequestEdge edge : dfa.outgoingEdgesOf(s)) {
					String symbol = edge.getLabel();
					State target = dfa.getEdgeTarget(edge);
					for (Set<State> p : partitions)
						if (p.contains(target)) {
							transizioni.add(new Pair<>(symbol, partitionMapping.get(p)));
							break;
						}
				}
			for (Pair<String, State> tr : transizioni)
				minimizzato.addEdge(statoMin, tr.value(), new RequestEdge(tr.key()));
		}

		minimizzato.setStartNode(minStart);
		minimizzato.setEndNode(minFinal);
		return minimizzato;
	}

}
