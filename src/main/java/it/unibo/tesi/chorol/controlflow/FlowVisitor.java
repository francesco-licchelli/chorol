package it.unibo.tesi.chorol.controlflow;

import it.unibo.tesi.chorol.controlflow.graph.FlowGraph;
import it.unibo.tesi.chorol.controlflow.graph.RequestEdge;
import it.unibo.tesi.chorol.controlflow.graph.State;
import it.unibo.tesi.chorol.controlflow.graph.StateType;
import it.unibo.tesi.chorol.symbols.SymbolManager;
import it.unibo.tesi.chorol.symbols.interfaces.operations.Operation;
import it.unibo.tesi.chorol.symbols.ports.EmbedPort;
import it.unibo.tesi.chorol.symbols.ports.Port;
import it.unibo.tesi.chorol.symbols.services.Service;
import jolie.lang.parse.OLVisitor;
import jolie.lang.parse.ast.*;
import jolie.lang.parse.ast.courier.CourierChoiceStatement;
import jolie.lang.parse.ast.courier.CourierDefinitionNode;
import jolie.lang.parse.ast.courier.NotificationForwardStatement;
import jolie.lang.parse.ast.courier.SolicitResponseForwardStatement;
import jolie.lang.parse.ast.expression.*;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.lang.parse.util.impl.ProgramInspectorCreatorVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class FlowVisitor implements OLVisitor<FlowContext, FlowGraph> {
	private static final Logger logger = LoggerFactory.getLogger(FlowVisitor.class);
	private SymbolManager symbolManager = null;

	private static Operation getOperation(Service service, String operationId, boolean isInput) {
		Operation op = null;
		if (isInput) {
			op = service.getInputPortHolder().getOperation(operationId);
			op = op != null ? op : service.getOutputPortHolder().getOperation(operationId);
		}
		return op;
	}

	void setSymbolManager(SymbolManager symbolManager) {
		this.symbolManager = symbolManager;
	}

	@Override
	public FlowGraph visit(Program program, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		ServiceNode serviceNode = new ProgramInspectorCreatorVisitor(program).createInspector()
				                          .getServiceNodes()[0];

		AtomicReference<State> main = new AtomicReference<>();
		result.setStartNode(State.createState(serviceNode.name()));
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
					if (definitionNode.id().equals("main")) main.set(subGraph.getStartNode());
					result.joinAfter(subGraph);
				});

		String executionMode = this.symbolManager.getServiceHolder().get(serviceNode.name()).getExecutionMode().name();
		switch (executionMode) {
			case "SINGLE":
				FlowGraph.clearGraph(result);
				break;
			case "CONCURRENT":
				// TODO capire come gestire il caso
//				break;
			case "SEQUENTIAL":
				result.addEdge(result.getEndNode(), main.get());
				FlowGraph.clearGraph(result);
				result.vertexSet().stream()
						.filter(state -> state.getStateType().equals(StateType.END))
						.forEach(state -> result.addEdge(state, main.get()));
				break;
		}
		System.out.println(executionMode);

		result.relabelNodesBFS();
		return result;
	}

	@Override
	public FlowGraph visit(OneWayOperationDeclaration oneWayOperationDeclaration, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO oneWayOperationDeclaration");
		return null;
	}

	@Override
	public FlowGraph visit(RequestResponseOperationDeclaration requestResponseOperationDeclaration, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO requestResponseOperationDeclaration");
		return null;
	}

	@Override
	public FlowGraph visit(DefinitionNode definitionNode, FlowContext flowContext) {
		return definitionNode.body().accept(this, flowContext);
	}

	@Override
	public FlowGraph visit(ParallelStatement parallelStatement, FlowContext flowContext) {
		FlowGraph result;
		if (parallelStatement.children().size() == 1)
			result = parallelStatement.children().get(0).accept(this, flowContext);
		else {
			result = new FlowGraph();
			result.setStartNode(State.createState(null));
			parallelStatement.children().stream()
					.map(child -> child.accept(this, flowContext))
					.forEach(flowGraph -> result.joinBetween(flowGraph, null));
		}
		return result;
	}

	@Override
	public FlowGraph visit(SequenceStatement sequenceStatement, FlowContext flowContext) {
		return sequenceStatement.children().stream()
				       .map(child -> child.accept(this, flowContext))
				       .filter(Objects::nonNull)
				       .reduce((acc, subGraph) -> {
					       acc.joinAfter(subGraph);
					       return acc;
				       }).orElse(null);
	}

	@Override
	public FlowGraph visit(NDChoiceStatement ndChoiceStatement, FlowContext flowContext) {
		FlowGraph result;
		if (ndChoiceStatement.children().size() == 1) {
			FlowGraph key = ndChoiceStatement.children().get(0).key().accept(this, flowContext);
			FlowGraph value = ndChoiceStatement.children().get(0).value().accept(this, flowContext);
			result = key == null ? value : key.joinAfter(value);
		} else {
			result = new FlowGraph();
			result.setStartNode(State.createState(null));
			ndChoiceStatement.children()
					.forEach(entry -> {
						FlowGraph key = entry.key().accept(this, flowContext);
						if (key == null) return;
						RequestEdge r = key.outgoingEdgesOf(key.getStartNode()).stream().findFirst().orElse(null);
						if (r == null) return;
						FlowGraph value = entry.value().accept(this, flowContext);
						result.joinBetween(value, r.getLabel());
					});
		}
		return result;
	}

	@Override
	public FlowGraph visit(OneWayOperationStatement oneWayOperationStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph(
				oneWayOperationStatement.id(),
				flowContext.service().name(),
				FlowVisitor.getOperation(flowContext.service(), oneWayOperationStatement.id(), true)
		);
		result.getStartNode().setLabel(null);
		return result;
	}

	@Override
	public FlowGraph visit(RequestResponseOperationStatement requestResponseOperationStatement, FlowContext flowContext) {
		return new FlowGraph(
				flowContext.service().name(),
				requestResponseOperationStatement.id(),
				FlowVisitor.getOperation(flowContext.service(), requestResponseOperationStatement.id(), true)
		);
	}

	@Override
	public FlowGraph visit(NotificationOperationStatement notificationOperationStatement, FlowContext flowContext) {
		String serviceName;
		String functionName;
		Operation operation;
		Port<OutputPortInfo> p = flowContext.service().getOutputPortHolder().get(notificationOperationStatement.outputPortId());
		if (p instanceof EmbedPort) {
			serviceName = ((EmbedPort<?>) p).getService().name();
			functionName = notificationOperationStatement.id();
			operation = ((EmbedPort<?>) p).getService().getInputPortHolder().getOperation(notificationOperationStatement.id());
		} else {
			serviceName = notificationOperationStatement.outputPortId();
			functionName = notificationOperationStatement.id();
			operation = flowContext.service().getOutputPortHolder().getOperation(notificationOperationStatement.id());
		}
		return new FlowGraph(serviceName, functionName, operation);
	}

	@Override
	public FlowGraph visit(SolicitResponseOperationStatement solicitResponseOperationStatement, FlowContext flowContext) {
		String serviceName;
		String functionName;
		Operation operation;
		Port<OutputPortInfo> p = flowContext.service().getOutputPortHolder().get(solicitResponseOperationStatement.outputPortId());
		if (p instanceof EmbedPort) {
			serviceName = ((EmbedPort<?>) p).getService().name();
			functionName = solicitResponseOperationStatement.id();
			operation = ((EmbedPort<?>) p).getService().getInputPortHolder().getOperation(solicitResponseOperationStatement.id());
		} else {
			serviceName = flowContext.service().name();
			functionName = solicitResponseOperationStatement.id();
			operation = this.symbolManager.getServiceHolder().get(serviceName).getOutputPortHolder().getOperation(functionName);
		}
		return new FlowGraph(serviceName, functionName, operation);
	}

	@Override
	public FlowGraph visit(LinkInStatement linkInStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO linkInStatement");
		return null;
	}

	@Override
	public FlowGraph visit(LinkOutStatement linkOutStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO linkOutStatement");
		return null;
	}

	@Override
	public FlowGraph visit(AssignStatement assignStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO assignStatement");
		return null;
	}

	@Override
	public FlowGraph visit(AddAssignStatement addAssignStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO addAssignStatement");
		return null;
	}

	@Override
	public FlowGraph visit(SubtractAssignStatement subtractAssignStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO subtractAssignStatement");
		return null;
	}

	@Override
	public FlowGraph visit(MultiplyAssignStatement multiplyAssignStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO multiplyAssignStatement");
		return null;
	}

	@Override
	public FlowGraph visit(DivideAssignStatement divideAssignStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO divideAssignStatement");
		return null;
	}

	@Override
	public FlowGraph visit(IfStatement ifStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		State startNode = State.createState(null);
		State endNode = State.createState(null);
		result.setStartNode(startNode);
		result.setEndNode(endNode);

		FlowGraph elseGraph = ifStatement.elseProcess().accept(this, flowContext);


		ifStatement.children().forEach(a ->
				                               result.joinBetween(a.value().accept(this, flowContext), "TODO condizioni"));

		result.joinBetween(elseGraph, "ELSE");

		return result;
	}

	@Override
	public FlowGraph visit(DefinitionCallStatement definitionCallStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO definitionCallStatement");
		return null;
	}

	@Override
	public FlowGraph visit(WhileStatement whileStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO whileStatement");
		return null;
	}

	@Override
	public FlowGraph visit(OrConditionNode orConditionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO orConditionNode");
		return null;
	}

	@Override
	public FlowGraph visit(AndConditionNode andConditionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO andConditionNode");
		return null;
	}

	@Override
	public FlowGraph visit(NotExpressionNode notExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO notExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(CompareConditionNode compareConditionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO compareConditionNode");
		return null;
	}

	@Override
	public FlowGraph visit(ConstantIntegerExpression constantIntegerExpression, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO constantIntegerExpression");
		return null;
	}

	@Override
	public FlowGraph visit(ConstantDoubleExpression constantDoubleExpression, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO constantDoubleExpression");
		return null;
	}

	@Override
	public FlowGraph visit(ConstantBoolExpression constantBoolExpression, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO constantBoolExpression");
		return null;
	}

	@Override
	public FlowGraph visit(ConstantLongExpression constantLongExpression, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO constantLongExpression");
		return null;
	}

	@Override
	public FlowGraph visit(ConstantStringExpression constantStringExpression, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO constantStringExpression");
		return null;
	}

	@Override
	public FlowGraph visit(ProductExpressionNode productExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO productExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(SumExpressionNode sumExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO sumExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(VariableExpressionNode variableExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO variableExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(NullProcessStatement nullProcessStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		State startNode = State.createState("NULL STATEMENT");
		startNode.setStateType(StateType.END);
		result.setStartNode(startNode);
		return result;
	}

	@Override
	public FlowGraph visit(Scope scope, FlowContext flowContext) {
		return scope.body().accept(this, flowContext);
	}

	@Override
	public FlowGraph visit(InstallStatement installStatement, FlowContext flowContext) {
		FlowGraph result = new FlowGraph();
		result.setStartNode(State.createState(null));
		result.setEndNode(State.createState(null));
		Arrays.stream(installStatement.handlersFunction().pairs()).forEach(pair -> {
			result.joinBetween(pair.value().accept(this, flowContext), pair.key());
		});
		return result;
	}

	@Override
	public FlowGraph visit(CompensateStatement compensateStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO compensateStatement");
		return null;
	}

	@Override
	public FlowGraph visit(ThrowStatement throwStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO throwStatement");
		return null;
	}

	@Override
	public FlowGraph visit(ExitStatement exitStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO exitStatement");
		return null;
	}

	@Override
	public FlowGraph visit(ExecutionInfo executionInfo, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO executionInfo");
		return null;
	}

	@Override
	public FlowGraph visit(CorrelationSetInfo correlationSetInfo, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO correlationSetInfo");
		return null;
	}

	@Override
	public FlowGraph visit(InputPortInfo inputPortInfo, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO inputPortInfo");
		return null;
	}

	@Override
	public FlowGraph visit(OutputPortInfo outputPortInfo, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO outputPortInfo");
		return null;
	}

	@Override
	public FlowGraph visit(PointerStatement pointerStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO pointerStatement");
		return null;
	}

	@Override
	public FlowGraph visit(DeepCopyStatement deepCopyStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO deepCopyStatement");
		return null;
	}

	@Override
	public FlowGraph visit(RunStatement runStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO runStatement");
		return null;
	}

	@Override
	public FlowGraph visit(UndefStatement undefStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO undefStatement");
		return null;
	}

	@Override
	public FlowGraph visit(ValueVectorSizeExpressionNode valueVectorSizeExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO valueVectorSizeExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(PreIncrementStatement preIncrementStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO preIncrementStatement");
		return null;
	}

	@Override
	public FlowGraph visit(PostIncrementStatement postIncrementStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO postIncrementStatement");
		return null;
	}

	@Override
	public FlowGraph visit(PreDecrementStatement preDecrementStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO preDecrementStatement");
		return null;
	}

	@Override
	public FlowGraph visit(PostDecrementStatement postDecrementStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO postDecrementStatement");
		return null;
	}

	@Override
	public FlowGraph visit(ForStatement forStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO forStatement");
		return null;
	}

	@Override
	public FlowGraph visit(ForEachSubNodeStatement forEachSubNodeStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO forEachSubNodeStatement");
		return null;
	}

	@Override
	public FlowGraph visit(ForEachArrayItemStatement forEachArrayItemStatement, FlowContext flowContext) {
		//TODO come dovrei gestire i for ?
		return forEachArrayItemStatement.body().accept(this, flowContext)
				       .joinAfter(forEachArrayItemStatement.body().accept(this, flowContext))
				       .joinAfter(forEachArrayItemStatement.body().accept(this, flowContext));
	}

	@Override
	public FlowGraph visit(SpawnStatement spawnStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO spawnStatement");
		return null;
	}

	@Override
	public FlowGraph visit(IsTypeExpressionNode isTypeExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO isTypeExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(InstanceOfExpressionNode instanceOfExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO instanceOfExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(TypeCastExpressionNode typeCastExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO typeCastExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(SynchronizedStatement synchronizedStatement, FlowContext flowContext) {
		//TODO come faccio a rappresentare l'accesso esclusivo alla risorsa (synchronizedStatement.id())?
		return synchronizedStatement.body().accept(this, flowContext);
	}

	@Override
	public FlowGraph visit(CurrentHandlerStatement currentHandlerStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO currentHandlerStatement");
		return null;
	}

	@Override
	public FlowGraph visit(EmbeddedServiceNode embeddedServiceNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO embeddedServiceNode");
		return null;
	}

	@Override
	public FlowGraph visit(InstallFixedVariableExpressionNode installFixedVariableExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO installFixedVariableExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(VariablePathNode variablePathNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO variablePathNode");
		return null;
	}

	@Override
	public FlowGraph visit(TypeInlineDefinition typeInlineDefinition, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO typeInlineDefinition");
		return null;
	}

	@Override
	public FlowGraph visit(TypeDefinitionLink typeDefinitionLink, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO typeDefinitionLink");
		return null;
	}

	@Override
	public FlowGraph visit(InterfaceDefinition interfaceDefinition, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO interfaceDefinition");
		return null;
	}

	@Override
	public FlowGraph visit(DocumentationComment documentationComment, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO documentationComment");
		return null;
	}

	@Override
	public FlowGraph visit(FreshValueExpressionNode freshValueExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO freshValueExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(CourierDefinitionNode courierDefinitionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO courierDefinitionNode");
		return null;
	}

	@Override
	public FlowGraph visit(CourierChoiceStatement courierChoiceStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO courierChoiceStatement");
		return null;
	}

	@Override
	public FlowGraph visit(NotificationForwardStatement notificationForwardStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO notificationForwardStatement");
		return null;
	}

	@Override
	public FlowGraph visit(SolicitResponseForwardStatement solicitResponseForwardStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO solicitResponseForwardStatement");
		return null;
	}

	@Override
	public FlowGraph visit(InterfaceExtenderDefinition interfaceExtenderDefinition, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO interfaceExtenderDefinition");
		return null;
	}

	@Override
	public FlowGraph visit(InlineTreeExpressionNode inlineTreeExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO inlineTreeExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(VoidExpressionNode voidExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO voidExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(ProvideUntilStatement provideUntilStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO provideUntilStatement");
		return null;
	}

	@Override
	public FlowGraph visit(TypeChoiceDefinition typeChoiceDefinition, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO typeChoiceDefinition");
		return null;
	}

	@Override
	public FlowGraph visit(ImportStatement importStatement, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO importStatement");
		return null;
	}

	@Override
	public FlowGraph visit(ServiceNode serviceNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO serviceNode");
		return null;
	}

	@Override
	public FlowGraph visit(EmbedServiceNode embedServiceNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO embedServiceNode");
		return null;
	}

	@Override
	public FlowGraph visit(SolicitResponseExpressionNode solicitResponseExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO solicitResponseExpressionNode");
		return null;
	}

	@Override
	public FlowGraph visit(IfExpressionNode ifExpressionNode, FlowContext flowContext) {
		FlowVisitor.logger.info("TODO ifExpressionNode");
		return null;
	}

}
