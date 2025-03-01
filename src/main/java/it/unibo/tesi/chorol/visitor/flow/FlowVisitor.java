package it.unibo.tesi.chorol.visitor.flow;

import it.unibo.tesi.chorol.symbols.SymbolManager;
import it.unibo.tesi.chorol.symbols.ports.Port;
import it.unibo.tesi.chorol.utils.GraphUtils;
import it.unibo.tesi.chorol.visitor.expression.ExprVisitor;
import it.unibo.tesi.chorol.visitor.flow.context.FlowContext;
import it.unibo.tesi.chorol.visitor.flow.graph.FlowGraph;
import it.unibo.tesi.chorol.visitor.flow.graph.RequestEdge;
import it.unibo.tesi.chorol.visitor.flow.graph.State;
import it.unibo.tesi.chorol.visitor.flow.graph.StateType;
import jolie.lang.Constants;
import jolie.lang.parse.ast.*;
import jolie.lang.parse.ast.expression.OrConditionNode;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


public class FlowVisitor extends FlowVisitorBase {

	private final SymbolManager symbolManager;

	FlowVisitor(SymbolManager symbolManager) {
		super();
		this.symbolManager = symbolManager;
	}

	@Override
	public FlowGraph visit(ServiceNode serviceNode, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState());
		// Tipo SERVICE per lo start node
		result.getStartNode().setStateType(StateType.SERVICE);

		// Visita i DefinitionNode (init, main)
		serviceNode.program().children().stream()
				.filter(DefinitionNode.class::isInstance)
				.map(DefinitionNode.class::cast)
				.filter(dn -> dn.id().equals("init") || dn.id().equals("main"))
				.forEach(definitionNode -> {
					FlowGraph subGraph = this.visit(
							definitionNode,
							flowContext != null
									? flowContext
									: new FlowContext(
									this.symbolManager.getServiceHolder().get(serviceNode.name()))
					);
					if (definitionNode.id().equals("main")) subGraph.getStartNode().setMain();
					result.joinAfter(subGraph);
				});

		Constants.ExecutionMode executionMode = this.symbolManager.getServiceHolder().get(serviceNode.name()).getExecutionMode();
		switch (executionMode) {
			case SINGLE:
				break;
			case CONCURRENT:
			case SEQUENTIAL:
				State main = result.vertexSet().stream().filter(State::isMain).findFirst().orElse(null);
				result.addEdge(result.getEndNode(), main);
				result.vertexSet().stream()
						.filter(state -> state.getStateType().equals(StateType.END))
						.forEach(state -> result.addEdge(state, main));
				break;
		}
		GraphUtils.clearGraph(result, executionMode);
		result.relabelNodesBFS();
		return result;
	}

	@Override
	public FlowGraph visit(DefinitionNode definitionNode, FlowContext flowContext) {
		return definitionNode.body().accept(this, flowContext);
	}

	@Override
	public FlowGraph visit(ParallelStatement parallelStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState());
		result.setEndNode(State.createState());

		// Unisci i singoli flowGraph in parallelo
		parallelStatement.children().stream()
				.map(child -> child.accept(this, flowContext))
				.forEach(flowGraph -> result.joinBetween(flowGraph, null));
		return result;
	}

	@Override
	public FlowGraph visit(SequenceStatement sequenceStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState());
		result.setEndNode(result.getStartNode());
		sequenceStatement.children().stream()
				.map(child -> child.accept(this, flowContext))
				.filter(Objects::nonNull)
				.forEach(result::joinAfter);
		return result.containsInformation() ? result : null;
	}

	@Override
	public FlowGraph visit(NDChoiceStatement ndChoiceStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState());
		result.setEndNode(State.createState());

		ndChoiceStatement.children()
				.forEach(entry -> {
					FlowGraph key = entry.key().accept(this, flowContext);
					FlowGraph value = entry.value().accept(this, flowContext);
					key.joinAfter(value);
					result.joinBetween(key, null);
				});
		return result.containsInformation() ? result : null;
	}

	@Override
	public FlowGraph visit(OneWayOperationStatement oneWayOperationStatement, FlowContext flowContext) {
		return new FlowGraph(
				flowContext.service().name(),
				flowContext.service().getInputPortHolder().getOperation(oneWayOperationStatement.id()),
				"Input",
				null
		);
	}

	@Override
	public FlowGraph visit(RequestResponseOperationStatement requestResponseOperationStatement, FlowContext flowContext) {
		return new FlowGraph(
				flowContext.service().name(),
				flowContext.service().getInputPortHolder().getOperation(requestResponseOperationStatement.id()),
				"Input",
				requestResponseOperationStatement.process().accept(this, flowContext)
		);
	}

	@Override
	public FlowGraph visit(NotificationOperationStatement notificationOperationStatement, FlowContext flowContext) {
		String serviceName = notificationOperationStatement.context().enclosingCode().get(0)
				                     .split("@")[1].split("\\(")[0];
		Port<OutputPortInfo> port = flowContext.service().getOutputPortHolder().get(serviceName); // TODO: alias?
		return new FlowGraph(serviceName, port.getOperation(notificationOperationStatement.id()), "Output", null);
	}

	@Override
	public FlowGraph visit(SolicitResponseOperationStatement solicitResponseOperationStatement, FlowContext flowContext) {
		String serviceName = solicitResponseOperationStatement.context().enclosingCode().get(0)
				                     .split("@")[1].split("\\(")[0];
		Port<OutputPortInfo> port = flowContext.service().getOutputPortHolder().get(serviceName); //TODO alias?
		return new FlowGraph(serviceName, port.getOperation(solicitResponseOperationStatement.id()), "Output", null);
	}

	@Override
	public FlowGraph visit(IfStatement ifStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		State startNode = State.createState();
		State endNode = State.createState();
		result.setStartNode(startNode);
		result.setEndNode(endNode);
		flowContext.faultManager().addLayer();
		AtomicInteger counter = ifStatement.children().size() > 1 ? new AtomicInteger(1) : null;

		ifStatement.children().forEach(entry -> {
			String label = ((counter != null) ? String.format("IF#%d", counter.getAndIncrement()) : "IF")
					               + new ExprVisitor().visit((OrConditionNode) entry.key(), null);

			flowContext.faultManager().addFaultMap();
			FlowGraph value = entry.value().accept(this, flowContext);
			if (value != null && value.containsInformation()) result.joinBetween(value, label);
		});

		// ELSE
		String elseLabel = result.containsInformation() ? "ELSE" : null;
		if (ifStatement.elseProcess() != null) {
			flowContext.faultManager().addFaultMap();
			FlowGraph value = ifStatement.elseProcess().accept(this, flowContext);
			if (value != null && value.containsInformation()) result.joinBetween(value, elseLabel);
		} else if (!startNode.equals(endNode)) {
			// Se non c'è l'else, ma i nodi sono diversi, crea un edge con label "ELSE"
			result.removeEdge(startNode, endNode);
			result.addEdge(startNode, endNode, new RequestEdge(elseLabel));
		}

		// Merge dei fault
		flowContext.faultManager().mergeFaults();

		return result.containsInformation() ? result : null;
	}

	@Override
	public FlowGraph visit(WhileStatement whileStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState());
		FlowGraph body = whileStatement.body().accept(this, flowContext);
		result.copyGraph(body);
		// Arco startNode -> body.startNode
		result.addEdge(result.getStartNode(), body.getStartNode());
		// Arco body.endNode -> result.endNode
		result.addEdge(body.getEndNode(), result.getEndNode());
		return result;
	}

	@Override
	public FlowGraph visit(NullProcessStatement nullProcessStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		State startNode = State.createState();
		startNode.setStateType(StateType.END);
		result.setStartNode(startNode);
		return result;
	}

	@Override
	public FlowGraph visit(ForEachArrayItemStatement forEachArrayItemStatement, FlowContext flowContext) {
		// Esempio di loop: start -> body -> start
		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState());
		FlowGraph body = forEachArrayItemStatement.body().accept(this, flowContext);
		result.copyGraph(body);
		result.addEdge(result.getStartNode(), body.getStartNode());
		result.addEdge(body.getEndNode(), result.getStartNode());
		return result;
	}

	@Override
	public FlowGraph visit(ForStatement forStatement, FlowContext flowContext) {
		return forStatement.body().accept(this, flowContext);
	}

	@Override
	public FlowGraph visit(ForEachSubNodeStatement forEachSubNodeStatement, FlowContext flowContext) {
		return forEachSubNodeStatement.body().accept(this, flowContext);
	}

	@Override
	public FlowGraph visit(SynchronizedStatement synchronizedStatement, FlowContext flowContext) {
		return synchronizedStatement.body().accept(this, flowContext);
	}

	@Override
	public FlowGraph visit(ExitStatement exitStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState());
		result.getStartNode().setStateType(StateType.EXIT);
		return result;
	}

	@Override
	public FlowGraph visit(Scope scope, FlowContext flowContext) {
		// Semplicemente visita il body
		FlowGraph result = scope.body().accept(this, flowContext);
		// Azzera i fault accumulati
		flowContext.faultManager().clearFaults();
		return result;
	}

	@Override
	public FlowGraph visit(InstallStatement installStatement, FlowContext flowContext) {
		// Aggiunge i fault handler nel faultManager
		Arrays.stream(installStatement.handlersFunction().pairs())
				.forEach(entry ->
						         flowContext.faultManager().addFault(entry.key(), entry.value().accept(this, flowContext)));
		return null;
	}

	@Override
	public FlowGraph visit(ThrowStatement throwStatement, FlowContext flowContext) {
		FlowGraph result = null;
		if (throwStatement.expression() != null) {
			// Prova a recuperare un grafo di fault associato all'id, se non esiste prende default
			result = flowContext.faultManager().getFault(throwStatement.id());
			if (result == null) result = flowContext.faultManager().getFault("default");
		}
		return result;
	}

	@Override
	public FlowGraph visit(DefinitionCallStatement definitionCallStatement, FlowContext flowContext) {
		// Chiama la definizione memorizzata nel service
		return flowContext.service().getDefinition(definitionCallStatement.id()).accept(this, flowContext);
	}

	@Override
	public FlowGraph visit(ProvideUntilStatement provideUntilStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState());
		result.setEndNode(State.createState());

		FlowGraph provide = provideUntilStatement.provide().accept(this, flowContext);
		result.copyGraph(provide);
		result.addEdge(result.getStartNode(), provide.getStartNode());
		result.addEdge(provide.getEndNode(), result.getStartNode());

		FlowGraph until = provideUntilStatement.until().accept(this, flowContext);
		if (until != null && until.containsInformation()) {
			result.joinBetween(until, "UNTIL");
			result.setEndNode(until.getEndNode());
		} else
			result.addEdge(result.getStartNode(), result.getEndNode());

		return result;
	}

	@Override
	public FlowGraph visit(SpawnStatement spawnStatement, FlowContext flowContext) {
		// Visita l'espressione che stabilisce l'upper bound, ma non crea alcun FlowGraph
		spawnStatement.upperBoundExpression().accept(this, flowContext);
		return null;
	}
}
