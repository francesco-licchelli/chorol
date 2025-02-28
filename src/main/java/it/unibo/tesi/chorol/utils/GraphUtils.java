package it.unibo.tesi.chorol.utils;

import it.unibo.tesi.chorol.visitor.flow.graph.FlowGraph;
import it.unibo.tesi.chorol.visitor.flow.graph.RequestEdge;
import it.unibo.tesi.chorol.visitor.flow.graph.State;
import it.unibo.tesi.chorol.visitor.flow.graph.StateType;
import jolie.lang.Constants;

import java.util.*;

public class GraphUtils {


	public static void clearGraph(FlowGraph flowGraph, Constants.ExecutionMode mode) {
		FlowGraph nfaNoEpsilon = GraphUtils.removeEpsilonTransitions(flowGraph);
		FlowGraph dfa = GraphUtils.convertToDFA(nfaNoEpsilon);
		Set<State> dfaFinalStates = new HashSet<>();
		switch (mode) {
			case SINGLE:
				dfa.vertexSet().stream()
						.filter(v -> v.getStateType().equals(StateType.EXIT))
						.forEach(dfaFinalStates::add);
			case CONCURRENT:
			case SEQUENTIAL:
				dfa.vertexSet().stream()
						.filter(v -> v.getStateType().equals(StateType.END))
						.forEach(dfaFinalStates::add);
				break;
		}
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

	private static Set<String> getAlphabet(FlowGraph graph) {
		Set<String> alphabet = new HashSet<>();
		for (State s : graph.vertexSet())
			for (RequestEdge edge : graph.outgoingEdgesOf(s)) {
				String label = edge.getLabel();
				if (label != null && !label.isEmpty()) alphabet.add(label);
			}
		return alphabet;
	}

/*	private static FlowGraph minimizeDFA(FlowGraph dfa, State dfaStart, Set<State> dfaFinalStates) {
		// Partizionamento iniziale: stati finali vs non finali
		Set<State> allStates = new HashSet<>(dfa.vertexSet());
		Set<State> finalStates = new HashSet<>(dfaFinalStates);
		Set<State> nonFinalStates = new HashSet<>(allStates);
		nonFinalStates.removeAll(finalStates);

		Set<Set<State>> P = new HashSet<>();
		if (!finalStates.isEmpty()) P.add(finalStates);
		if (!nonFinalStates.isEmpty()) P.add(nonFinalStates);

		// W contiene le partizioni candidate per il "split"
		Queue<Set<State>> W = new LinkedList<>();
		// Si sceglie la partizione più piccola per ottimizzare
		if (!finalStates.isEmpty() && finalStates.size() <= nonFinalStates.size())
			W.add(finalStates);
		else
			W.add(nonFinalStates);

		Set<String> alphabet = GraphUtils.getAlphabet(dfa);

		while (!W.isEmpty()) {
			Set<State> A = W.poll();
			for (String symbol : alphabet) {
				Set<State> X = new HashSet<>();
				for (State s : allStates)
					for (RequestEdge edge : dfa.outgoingEdgesOf(s))
						if (symbol.equals(edge.getLabel()) && A.contains(dfa.getEdgeTarget(edge))) {
							X.add(s);
							break;
						}
				List<Set<State>> toReplace = new ArrayList<>();
				List<Set<State>> replacements = new ArrayList<>();
				for (Set<State> Y : new HashSet<>(P)) { // utilizziamo una copia per evitare ConcurrentModificationException
					Set<State> intersection = new HashSet<>(Y);
					intersection.retainAll(X);
					if (intersection.isEmpty() || intersection.size() == Y.size())
						continue;
					Set<State> difference = new HashSet<>(Y);
					difference.removeAll(X);
					toReplace.add(Y);
					replacements.add(intersection);
					replacements.add(difference);
					// Aggiorniamo W: se Y era presente, lo sostituiamo con entrambe le nuove partizioni,
					// altrimenti aggiungiamo quella più piccola
					if (W.contains(Y)) {
						W.remove(Y);
						W.add(intersection);
						W.add(difference);
					} else if (intersection.size() <= difference.size())
						W.add(intersection);
					else
						W.add(difference);
				}
				P.removeAll(toReplace);
				P.addAll(replacements);
			}
		}

		// Costruiamo il DFA minimizzato a partire dalle partizioni P
		FlowGraph minimized = new FlowGraph();
		Map<Set<State>, State> partitionMapping = new HashMap<>();
		for (Set<State> part : P) {
			State newState = State.createState();
			// Se in una partizione compare uno stato di tipo END o EXIT, lo segnaliamo nel nuovo stato
			if (part.stream().anyMatch(s -> s.getStateType().equals(StateType.END)))
				newState.setStateType(StateType.END);
			if (part.stream().anyMatch(s -> s.getStateType().equals(StateType.EXIT)))
				newState.setStateType(StateType.EXIT);
			minimized.addVertex(newState);
			partitionMapping.put(part, newState);
		}

		State minStart = null;
		State minFinal = null;
		for (Set<State> part : P) {
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

		for (Set<State> part : P) {
			State sourceMin = partitionMapping.get(part);
			for (String symbol : alphabet)
				for (State s : part)
					for (RequestEdge edge : dfa.outgoingEdgesOf(s))
						if (symbol.equals(edge.getLabel())) {
							State target = dfa.getEdgeTarget(edge);
							for (Set<State> part2 : P)
								if (part2.contains(target)) {
									State targetMin = partitionMapping.get(part2);
									RequestEdge existingEdge = null;
									for (RequestEdge e : minimized.outgoingEdgesOf(sourceMin))
										if (minimized.getEdgeTarget(e).equals(targetMin)) {
											existingEdge = e;
											break;
										}
									if (existingEdge != null && !existingEdge.getLabel().equals(edge.getLabel())) {
										String newLabel = existingEdge.getLabel() + "\\n----\\n" + symbol;
										existingEdge.setLabel(newLabel);
									} else minimized.addEdge(sourceMin, targetMin, new RequestEdge(symbol));
									break;
								}
							break;
						}
		}
		minimized.setStartNode(minStart);
		minimized.setEndNode(minFinal);
		return minimized;
	}*/

	private static FlowGraph minimizeDFA(FlowGraph dfa, State dfaStart, Set<State> dfaFinalStates) {
		Set<State> allStates = new HashSet<>(dfa.vertexSet());
		Set<State> finalStates = new HashSet<>(dfaFinalStates);
		Set<State> nonFinalStates = new HashSet<>(allStates);
		nonFinalStates.removeAll(finalStates);

		// Partizionamento iniziale
		Set<Set<State>> P = new HashSet<>();
		if (!finalStates.isEmpty()) P.add(finalStates);
		if (!nonFinalStates.isEmpty()) P.add(nonFinalStates);

		Queue<Set<State>> W = new LinkedList<>();
		if (!finalStates.isEmpty() && finalStates.size() <= nonFinalStates.size())
			W.add(finalStates);
		else
			W.add(nonFinalStates);

		Set<String> alphabet = GraphUtils.getAlphabet(dfa);

		// Precompute transizioni inverse per ogni simbolo
		Map<String, Map<State, Set<State>>> reverseTransitions = new HashMap<>();
		for (String symbol : alphabet) reverseTransitions.put(symbol, new HashMap<>());
		for (State s : allStates)
			for (RequestEdge edge : dfa.outgoingEdgesOf(s)) {
				String symbol = edge.getLabel();
				State target = dfa.getEdgeTarget(edge);
				reverseTransitions.get(symbol)
						.computeIfAbsent(target, t -> new HashSet<>())
						.add(s);
			}

		// Mappa per associare ogni stato alla sua partizione
		Map<State, Set<State>> stateToPartition = new HashMap<>();
		for (Set<State> part : P) for (State s : part) stateToPartition.put(s, part);

		// Loop principale del partizionamento
		while (!W.isEmpty()) {
			Set<State> A = W.poll();
			for (String symbol : alphabet) {
				// Calcola X usando le transizioni inverse
				Set<State> X = new HashSet<>();
				for (State a : A) {
					Set<State> pre = reverseTransitions.get(symbol).get(a);
					if (pre != null) X.addAll(pre);
				}

				// Raggruppa X per partizione
				Map<Set<State>, Set<State>> affected = new HashMap<>();
				for (State s : X) {
					Set<State> part = stateToPartition.get(s);
					affected.computeIfAbsent(part, p -> new HashSet<>()).add(s);
				}

				// Per ogni partizione interessata, esegui lo split
				for (Map.Entry<Set<State>, Set<State>> entry : affected.entrySet()) {
					Set<State> Y = entry.getKey();
					Set<State> intersection = entry.getValue();
					if (intersection.size() < Y.size()) {
						Set<State> difference = new HashSet<>(Y);
						difference.removeAll(intersection);

						// Aggiorna la partizione P
						P.remove(Y);
						P.add(intersection);
						P.add(difference);

						// Aggiorna stateToPartition
						for (State s : intersection) stateToPartition.put(s, intersection);
						for (State s : difference) stateToPartition.put(s, difference);

						// Aggiorna W
						if (W.contains(Y)) {
							W.remove(Y);
							W.add(intersection);
							W.add(difference);
						} else if (intersection.size() <= difference.size())
							W.add(intersection);
						else
							W.add(difference);
					}
				}
			}
		}

		// Costruzione del DFA minimizzato
		FlowGraph minimized = new FlowGraph();
		Map<Set<State>, State> partitionMapping = new HashMap<>();
		for (Set<State> part : P) {
			State newState = State.createState();
			// Propaga le proprietà (es. END, EXIT) se presenti in uno degli stati della partizione
			if (part.stream().anyMatch(s -> s.getStateType().equals(StateType.END)))
				newState.setStateType(StateType.END);
			if (part.stream().anyMatch(s -> s.getStateType().equals(StateType.EXIT)))
				newState.setStateType(StateType.EXIT);
			minimized.addVertex(newState);
			partitionMapping.put(part, newState);
		}

		State minStart = null;
		State minFinal = null;
		for (Set<State> part : P) {
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

		// Costruzione degli archi: utilizza un rappresentante per ogni partizione
		for (Set<State> part : P) {
			State sourceMin = partitionMapping.get(part);
			State rep = part.iterator().next(); // Rappresentante della partizione
			for (RequestEdge edge : dfa.outgoingEdgesOf(rep)) {
				String symbol = edge.getLabel();
				State target = dfa.getEdgeTarget(edge);
				Set<State> targetPart = stateToPartition.get(target);
				State targetMin = partitionMapping.get(targetPart);
				// Aggiungi l’arco evitando duplicazioni
				RequestEdge existingEdge = null;
				for (RequestEdge e : minimized.outgoingEdgesOf(sourceMin))
					if (minimized.getEdgeTarget(e).equals(targetMin)) {
						existingEdge = e;
						break;
					}
				if (existingEdge != null && !existingEdge.getLabel().contains(symbol)) {
					String newLabel = existingEdge.getLabel() + "\\n----\\n" + symbol;
					existingEdge.setLabel(newLabel);
				} else if (existingEdge == null) minimized.addEdge(sourceMin, targetMin, new RequestEdge(symbol));
			}
		}
		minimized.setStartNode(minStart);
		minimized.setEndNode(minFinal);
		return minimized;
	}


}
