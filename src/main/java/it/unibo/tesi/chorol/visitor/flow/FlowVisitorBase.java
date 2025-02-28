package it.unibo.tesi.chorol.visitor.flow;

import it.unibo.tesi.chorol.visitor.flow.context.FlowContext;
import it.unibo.tesi.chorol.visitor.flow.graph.FlowGraph;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FlowVisitorBase implements OLVisitor<FlowContext, FlowGraph> {
	private static final Logger logger = LoggerFactory.getLogger(FlowVisitorBase.class);

	@Override
	public FlowGraph visit(LinkInStatement linkInStatement, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO linkInStatement");
		return null;
	}

	@Override
	public FlowGraph visit(LinkOutStatement linkOutStatement, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO linkOutStatement");
		return null;
	}

	@Override
	public FlowGraph visit(CompensateStatement compensateStatement, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO compensateStatement");
		return null;
	}

	@Override
	public FlowGraph visit(CorrelationSetInfo correlationSetInfo, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO correlationSetInfo");
		return null;
	}

	@Override
	public FlowGraph visit(PointerStatement pointerStatement, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO pointerStatement");
		return null;
	}

	@Override
	public FlowGraph visit(RunStatement runStatement, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO runStatement");
		return null;
	}

	@Override
	public FlowGraph visit(CurrentHandlerStatement currentHandlerStatement, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO currentHandlerStatement");
		return null;
	}


	@Override
	public FlowGraph visit(VariablePathNode variablePathNode, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO variablePathNode");
		return null;
	}


	@Override
	public FlowGraph visit(CourierDefinitionNode courierDefinitionNode, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO courierDefinitionNode");
		return null;
	}

	@Override
	public FlowGraph visit(CourierChoiceStatement courierChoiceStatement, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO courierChoiceStatement");
		return null;
	}

	@Override
	public FlowGraph visit(NotificationForwardStatement notificationForwardStatement, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO notificationForwardStatement");
		return null;
	}

	@Override
	public FlowGraph visit(SolicitResponseForwardStatement solicitResponseForwardStatement, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO solicitResponseForwardStatement");
		return null;
	}

	@Override
	public FlowGraph visit(ProvideUntilStatement provideUntilStatement, FlowContext flowContext) {
		FlowVisitorBase.logger.info("TODO provideUntilStatement");
		return null;
	}


	/*
	 * OPERAZIONI CHE NON POSSONO GENERARE CHIAMATE
	 * */

	@Override
	public FlowGraph visit(Program program, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(OneWayOperationDeclaration oneWayOperationDeclaration, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(RequestResponseOperationDeclaration requestResponseOperationDeclaration, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(AssignStatement assignStatement, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(AddAssignStatement addAssignStatement, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(SubtractAssignStatement subtractAssignStatement, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(MultiplyAssignStatement multiplyAssignStatement, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(DivideAssignStatement divideAssignStatement, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(ConstantBoolExpression constantBoolExpression, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(ConstantLongExpression constantLongExpression, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(ConstantStringExpression constantStringExpression, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(DeepCopyStatement deepCopyStatement, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(UndefStatement undefStatement, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(EmbedServiceNode embedServiceNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(EmbeddedServiceNode embeddedServiceNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(SolicitResponseExpressionNode solicitResponseExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(IfExpressionNode ifExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(TypeChoiceDefinition typeChoiceDefinition, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(OrConditionNode orConditionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(AndConditionNode andConditionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(NotExpressionNode notExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(CompareConditionNode compareConditionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(ConstantIntegerExpression constantIntegerExpression, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(ConstantDoubleExpression constantDoubleExpression, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(ProductExpressionNode productExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(SumExpressionNode sumExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(VariableExpressionNode variableExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(ExecutionInfo executionInfo, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(InputPortInfo inputPortInfo, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(OutputPortInfo outputPortInfo, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(ValueVectorSizeExpressionNode valueVectorSizeExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(PreIncrementStatement preIncrementStatement, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(PostIncrementStatement postIncrementStatement, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(PreDecrementStatement preDecrementStatement, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(PostDecrementStatement postDecrementStatement, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(IsTypeExpressionNode isTypeExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(InstanceOfExpressionNode instanceOfExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(TypeCastExpressionNode typeCastExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(InstallFixedVariableExpressionNode installFixedVariableExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(InterfaceExtenderDefinition interfaceExtenderDefinition, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(InlineTreeExpressionNode inlineTreeExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(VoidExpressionNode voidExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(TypeDefinitionLink typeDefinitionLink, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(InterfaceDefinition interfaceDefinition, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(TypeInlineDefinition typeInlineDefinition, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(FreshValueExpressionNode freshValueExpressionNode, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(DocumentationComment documentationComment, FlowContext flowContext) {
		return null;
	}

	@Override
	public FlowGraph visit(ImportStatement importStatement, FlowContext flowContext) {
		return null;
	}

}
