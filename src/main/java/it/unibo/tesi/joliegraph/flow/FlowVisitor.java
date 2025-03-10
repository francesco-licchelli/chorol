package it.unibo.tesi.joliegraph.flow;

import it.unibo.tesi.joliegraph.flow.context.FlowContext;
import it.unibo.tesi.joliegraph.flow.graph.FlowGraph;
import it.unibo.tesi.joliegraph.flow.graph.State;
import it.unibo.tesi.joliegraph.flow.graph.StateType;
import it.unibo.tesi.joliegraph.symbols.SymbolManager;
import it.unibo.tesi.joliegraph.symbols.interfaces.operations.OneWayOperation;
import it.unibo.tesi.joliegraph.symbols.interfaces.operations.ReqResOperation;
import it.unibo.tesi.joliegraph.symbols.ports.Port;
import it.unibo.tesi.joliegraph.utils.GraphUtils;
import jolie.lang.Constants;
import jolie.lang.parse.ast.*;

import java.util.Arrays;
import java.util.stream.Collectors;


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
				GraphUtils.clearGraph(result, executionMode);
				if (result.vertexSet().stream()
						    .filter(state -> state.getStateType().equals(StateType.END))
						    .collect(Collectors.toSet()).isEmpty()) result.getEndNode().setStateType(StateType.EXIT);

				result.vertexSet().stream()
						.filter(state -> result.outgoingEdgesOf(state).isEmpty())
						.forEach(state -> state.setStateType(StateType.EXIT));

				if (result.vertexSet().stream()
						    .filter(state -> state.getStateType().equals(StateType.EXIT))
						    .collect(Collectors.toSet()).isEmpty()) result.getEndNode().setStateType(StateType.EXIT);
				break;
			case CONCURRENT:
			case SEQUENTIAL:
				State main = result.vertexSet().stream().filter(State::isMain).findFirst().orElse(null);
				result.addEdge(result.getEndNode(), main);
				result.vertexSet().stream()
						.filter(state -> state.getStateType().equals(StateType.END))
						.forEach(state -> result.addEdge(state, main));
				GraphUtils.clearGraph(result, executionMode);
				result.vertexSet().stream()
						.filter(state -> state.getStateType().equals(StateType.END))
						.forEach(state -> state.setStateType(StateType.NORMAL));
				break;
		}
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
		for (OLSyntaxNode child : sequenceStatement.children()) {
			FlowGraph childResult = child.accept(this, flowContext);
			if (childResult != null) {
				result.joinAfter(childResult);
				if (result.getEndNode().getStateType().equals(StateType.EXIT))
					break;
			}
		}
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
				flowContext.service().getInputPortHolder().getPortByOperation(oneWayOperationStatement.id()).getName(),
				true,
				flowContext.service().getInputPortHolder().getOperation(oneWayOperationStatement.id()),
				this.symbolManager.getTypeHolder(),
				null
		);
	}

	@Override
	public FlowGraph visit(RequestResponseOperationStatement requestResponseOperationStatement, FlowContext flowContext) {
		return new FlowGraph(
				flowContext.service().getInputPortHolder().getPortByOperation(requestResponseOperationStatement.id()).getName(),
				true,
				flowContext.service().getInputPortHolder().getOperation(requestResponseOperationStatement.id()),
				this.symbolManager.getTypeHolder(),
				requestResponseOperationStatement.process().accept(this, flowContext)
		);
	}

	@Override
	public FlowGraph visit(NotificationOperationStatement notificationOperationStatement, FlowContext flowContext) {
		String serviceName = notificationOperationStatement.context().enclosingCode().get(0).trim()
				                     .split("@")[1].split("\\(")[0];
		Port<OutputPortInfo> port = flowContext.service().getOutputPortHolder().get(serviceName); // TODO: alias?
		OneWayOperation op = (OneWayOperation) port.getOperation(notificationOperationStatement.id());
		return new FlowGraph(port.getName(), false, op, this.symbolManager.getTypeHolder(), null);
	}

	@Override
	public FlowGraph visit(SolicitResponseOperationStatement solicitResponseOperationStatement, FlowContext flowContext) {
		String serviceName = solicitResponseOperationStatement.context().enclosingCode().get(0).trim()
				                     .split("@")[1].split("\\(")[0];
		Port<OutputPortInfo> port = flowContext.service().getOutputPortHolder().get(serviceName); //TODO alias?
		ReqResOperation op = (ReqResOperation) port.getOperation(solicitResponseOperationStatement.id());
		return new FlowGraph(port.getName(), false, op, this.symbolManager.getTypeHolder(), null);
	}

	@Override
	public FlowGraph visit(IfStatement ifStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		State startNode = State.createState();
		State endNode = State.createState();
		result.setStartNode(startNode);
		result.setEndNode(endNode);
		flowContext.faultManager().addLayer();

		ifStatement.children().forEach(entry -> {
			flowContext.faultManager().addFaultMap();
			FlowGraph value = entry.value().accept(this, flowContext);
			if (value != null && value.containsInformation()) result.joinBetween(value, null);
		});

		if (ifStatement.elseProcess() != null) {
			flowContext.faultManager().addFaultMap();
			FlowGraph value = ifStatement.elseProcess().accept(this, flowContext);
			if (value != null && value.containsInformation()) result.joinBetween(value, null);
		} else if (!startNode.equals(endNode)) result.addEdge(startNode, endNode);

		flowContext.faultManager().mergeFaults();

		return result.containsInformation() ? result : null;
	}

	private FlowGraph handleLoop(FlowGraph child) {
		FlowGraph inner = new FlowGraph();
		inner.setStartNode(State.createState());
		inner.setEndNode(State.createState());

		inner.joinBetween(child, null);
		inner.addEdge(inner.getEndNode(), inner.getStartNode());

		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState());
		result.setEndNode(State.createState());
		result.joinBetween(inner, null);
		result.addEdge(result.getStartNode(), result.getEndNode());
		return result;
	}

	@Override
	public FlowGraph visit(WhileStatement whileStatement, FlowContext flowContext) {
		return this.handleLoop(whileStatement.body().accept(this, flowContext));
	}

	@Override
	public FlowGraph visit(ForEachArrayItemStatement forEachArrayItemStatement, FlowContext flowContext) {
		return this.handleLoop(forEachArrayItemStatement.body().accept(this, flowContext));
	}

	@Override
	public FlowGraph visit(ForStatement forStatement, FlowContext flowContext) {
		return this.handleLoop(forStatement.body().accept(this, flowContext));
	}

	@Override
	public FlowGraph visit(ForEachSubNodeStatement forEachSubNodeStatement, FlowContext flowContext) {
		return this.handleLoop(forEachSubNodeStatement.body().accept(this, flowContext));
	}

	@Override
	public FlowGraph visit(NullProcessStatement nullProcessStatement, FlowContext flowContext) {
		return null;
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
		FlowGraph result = scope.body().accept(this, flowContext);
		flowContext.faultManager().clearFaults();
		return result;
	}

	@Override
	public FlowGraph visit(InstallStatement installStatement, FlowContext flowContext) {
		Arrays.stream(installStatement.handlersFunction().pairs())
				.forEach(entry ->
						         flowContext.faultManager().addFault(entry.key(), entry.value().accept(this, flowContext)));
		return null;
	}

	@Override
	public FlowGraph visit(ThrowStatement throwStatement, FlowContext flowContext) {
		FlowGraph result;
		result = flowContext.faultManager().getFault(throwStatement.id());
		if (result == null) result = flowContext.faultManager().getFault("default");
		return result;
	}

	@Override
	public FlowGraph visit(DefinitionCallStatement definitionCallStatement, FlowContext flowContext) {
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
			result.joinBetween(until, null);
			result.setEndNode(until.getEndNode());
		} else
			result.addEdge(result.getStartNode(), result.getEndNode());
		return result;
	}
}
