package it.unibo.tesi.chorol.visitor.flow.context;


import it.unibo.tesi.chorol.visitor.flow.graph.FlowGraph;

import java.util.*;

public class FaultManager {

	private final Deque<List<HashMap<String, FlowGraph>>> faultScopes = new ArrayDeque<>();
	private final boolean inInstall = false;

	public void addLayer() {
		this.faultScopes.push(new ArrayList<>());
	}

	public void addFault(String key, FlowGraph fault) {
		List<HashMap<String, FlowGraph>> currentScope = this.faultScopes.peek();
		// Inserisce nella mappa più recente (ultimo elemento della lista)
		if (currentScope.isEmpty()) {
			HashMap<String, FlowGraph> newMap = new HashMap<>();
			newMap.put(key, fault);
			currentScope.add(newMap);
		} else currentScope.get(currentScope.size() - 1).put(key, fault);
	}

	public void addFaultMap() {
		if (this.faultScopes.peek() == null)
			this.faultScopes.push(new ArrayList<>());
		List<HashMap<String, FlowGraph>> currentScope = this.faultScopes.peek();
		currentScope.add(new HashMap<>());
	}


	public void mergeFaults() {
		// Unisce le mappe all'interno dello scope corrente
		this.mergeTopLayer();

		// Ottieni l'hashmap "T" del top scope (considerando solo l'elemento in posizione 0)
		List<HashMap<String, FlowGraph>> topScopeList = this.faultScopes.peek();
		if (topScopeList == null || topScopeList.isEmpty()) return;
		HashMap<String, FlowGraph> T = topScopeList.get(0);

		Iterator<List<HashMap<String, FlowGraph>>> iterator = this.faultScopes.iterator();
		if (iterator.hasNext()) iterator.next(); // Salta il top scope
		while (iterator.hasNext()) {
			List<HashMap<String, FlowGraph>> scopeList = iterator.next();
			if (scopeList.isEmpty()) continue;
			HashMap<String, FlowGraph> C = scopeList.get(0);

			List<String> keysToRemove = new ArrayList<>();
			for (String key : T.keySet())
				if (C.containsKey(key)) {
					FlowGraph G = new FlowGraph();
					G.joinBetween(T.get(key), null);
					G.joinBetween(C.get(key), null);
					C.put(key, G);
					keysToRemove.add(key);
				}
			for (String key : keysToRemove) T.remove(key);
		}
	}


	private void mergeTopLayer() {
		List<HashMap<String, FlowGraph>> currentScope = this.faultScopes.peek();
		HashMap<String, FlowGraph> accumulator = new HashMap<>();

		for (HashMap<String, FlowGraph> map : currentScope)
			for (Map.Entry<String, FlowGraph> entry : map.entrySet()) {
				String key = entry.getKey();
				FlowGraph graph = entry.getValue();
				if (!accumulator.containsKey(key)) accumulator.put(key, graph);
				else {
					FlowGraph H = accumulator.get(key);
					FlowGraph F = new FlowGraph();
					F.joinBetween(H, null);
					F.joinBetween(graph, null);
					accumulator.put(key, F);
				}
			}
		// Sostituisce lo "strato" corrente con una lista contenente solo l'accumulatore
		currentScope.clear();
		currentScope.add(accumulator);
	}

	public FlowGraph getFault(String key) {
		for (List<HashMap<String, FlowGraph>> scope : this.faultScopes)
			if (!scope.isEmpty()) {
				HashMap<String, FlowGraph> map = scope.get(0);
				if (map.containsKey(key)) return map.get(key);
			}
		return null;
	}

	public void clearFaults() {
		this.faultScopes.clear();
	}

}