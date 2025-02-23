package it.unibo.tesi.chorol.visitor.flow;

import it.unibo.tesi.chorol.symbols.SymbolManager;
import it.unibo.tesi.chorol.symbols.interfaces.operations.Operation;
import it.unibo.tesi.chorol.symbols.ports.Port;
import it.unibo.tesi.chorol.utils.GraphUtils;
import it.unibo.tesi.chorol.visitor.expression.ExprVisitor;
import it.unibo.tesi.chorol.visitor.flow.graph.FlowGraph;
import it.unibo.tesi.chorol.visitor.flow.graph.RequestEdge;
import it.unibo.tesi.chorol.visitor.flow.graph.State;
import it.unibo.tesi.chorol.visitor.flow.graph.StateType;
import jolie.lang.parse.ast.*;
import jolie.lang.parse.ast.expression.OrConditionNode;
import jolie.lang.parse.util.impl.ProgramInspectorCreatorVisitor;

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
	public FlowGraph visit(Program program, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		ServiceNode serviceNode = new ProgramInspectorCreatorVisitor(program).createInspector()
				                          .getServiceNodes()[0];

		result.setStartNode(State.createState());
		result.getStartNode().setStateType(StateType.SERVICE);
		serviceNode.program().children().stream()
				.filter(DefinitionNode.class::isInstance)
				.map(DefinitionNode.class::cast)
				.forEach(definitionNode -> {
					FlowGraph subGraph = this.visit(definitionNode,
							flowContext != null
									? flowContext
									: new FlowContext(
									this.symbolManager.getServiceHolder().get(serviceNode.name()))
					);
					if (definitionNode.id().equals("main")) subGraph.getStartNode().setMain();
					result.joinAfter(subGraph);
				});

		String executionMode = this.symbolManager.getServiceHolder().get(serviceNode.name()).getExecutionMode().name();
		switch (executionMode) {
			case "SINGLE":
				break;
			case "CONCURRENT":
			case "SEQUENTIAL":
				State main = result.vertexSet().stream().filter(State::isMain).findFirst().orElse(null);
				result.addEdge(result.getEndNode(), main);
				result.vertexSet().stream()
						.filter(state -> state.getStateType().equals(StateType.END))
						.forEach(state -> result.addEdge(state, main));
				break;
		}

		GraphUtils.clearGraph(result);
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
		sequenceStatement.children().stream()
				.map(child -> child.accept(this, flowContext))
				.filter(Objects::nonNull)
				.forEach(result::joinAfter);
		return result;
	}


	@Override
	public FlowGraph visit(NDChoiceStatement ndChoiceStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState());
		result.setEndNode(State.createState());
		ndChoiceStatement.children()
				.forEach(child -> {
					FlowGraph key = child.key().accept(this, flowContext);
					FlowGraph value = child.value().accept(this, flowContext);
					key.joinAfter(value);
					result.joinBetween(key.joinAfter(value), null);
				});
		return result;
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
		String functionName = notificationOperationStatement.id();
		Operation op = flowContext.service().getOutputPortHolder().getOperation(functionName);
		String serviceName = notificationOperationStatement.context().enclosingCode().get(0)
				                     .split("@")[1].split("\\(")[0];
		return new FlowGraph(serviceName, op, "Output", null);
	}

	@Override
	public FlowGraph visit(SolicitResponseOperationStatement solicitResponseOperationStatement, FlowContext flowContext) {
		Port<OutputPortInfo> p = flowContext.service().getOutputPortHolder().get(solicitResponseOperationStatement.outputPortId());
		String serviceName = solicitResponseOperationStatement.context().enclosingCode().get(0)
				                     .split("@")[1].split("\\(")[0];
		return new FlowGraph(serviceName, p.getOperation(solicitResponseOperationStatement.id()), "Output", null);
	}

	@Override
	public FlowGraph visit(IfStatement ifStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		State startNode = State.createState();
		State endNode = State.createState();
		result.setStartNode(startNode);
		result.setEndNode(endNode);

		if (ifStatement.children().size() > 1) {
			AtomicInteger counter = new AtomicInteger(1);
			ifStatement.children().forEach(entry ->
					                               result.joinBetween(
							                               entry.value().accept(this, flowContext),
							                               String.format("IF#%d[%s]",
									                               counter.getAndIncrement(),
									                               new ExprVisitor().visit((OrConditionNode) entry.key(), null))
					                               )
			);
		} else ifStatement.children().forEach(entry ->
				                                      result.joinBetween(
						                                      entry.value().accept(this, flowContext),
						                                      String.format("IF[%s]",
								                                      new ExprVisitor().visit((OrConditionNode) entry.key(), null))
				                                      )
		);

		ifStatement.children().forEach(child -> child.value().accept(this, flowContext));

		if (ifStatement.elseProcess() != null) {
			FlowGraph elseGraph = ifStatement.elseProcess().accept(this, flowContext);
			result.joinBetween(elseGraph, "ELSE");
		} else {
			result.removeEdge(startNode, endNode);
			result.addEdge(startNode, endNode, new RequestEdge("ELSE"));
		}
		return result;
	}

	@Override
	public FlowGraph visit(WhileStatement whileStatement, FlowContext flowContext) {
		//TODO puo' avvenire una richiesta nella condizione del while?
		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState());
		FlowGraph body = whileStatement.body().accept(this, flowContext);
		result.copyGraph(body);
		result.addEdge(result.getStartNode(), body.getStartNode());
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
		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState());
		FlowGraph body = forEachArrayItemStatement.body().accept(this, flowContext);
		result.copyGraph(body);
		result.addEdge(result.getStartNode(), body.getStartNode());
		result.addEdge(body.getEndNode(), result.getEndNode());
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
		FlowGraph result = scope.body().accept(this, flowContext);
		flowContext.removeFaults();
		return result;
	}

	@Override
	public FlowGraph visit(InstallStatement installStatement, FlowContext flowContext) {
		flowContext.setInInstall(true);
		Arrays.stream(installStatement.handlersFunction().pairs())
				.forEach(entry ->
						         flowContext.addFault(entry.key(), entry.value().accept(this, flowContext)));
		flowContext.setInInstall(false);
		return null;
	}

	@Override
	public FlowGraph visit(ThrowStatement throwStatement, FlowContext flowContext) {
		FlowGraph result = null;
		if (flowContext.inInstall() && throwStatement.expression() != null)
			flowContext.addFault(throwStatement.id(), throwStatement.expression().accept(this, flowContext));
		else if (!flowContext.inInstall()) {
			result = flowContext.getFault(throwStatement.id());
			if (result == null) result = flowContext.getFault("default");
			result.vertexSet().stream().filter(state -> state.getStateType().equals(StateType.NORMAL)).forEach(state -> state.setStateType(StateType.FAULT));
		}
		return result;
	}

	@Override
	public FlowGraph visit(SpawnStatement spawnStatement, FlowContext flowContext) {
		spawnStatement.upperBoundExpression().accept(this, flowContext);
		return null;
	}


}
