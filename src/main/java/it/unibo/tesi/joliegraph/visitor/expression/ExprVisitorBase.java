package it.unibo.tesi.joliegraph.visitor.expression;

import jolie.lang.Constants;
import jolie.lang.parse.OLVisitor;
import jolie.lang.parse.Scanner;
import jolie.lang.parse.ast.*;
import jolie.lang.parse.ast.courier.CourierChoiceStatement;
import jolie.lang.parse.ast.courier.CourierDefinitionNode;
import jolie.lang.parse.ast.courier.NotificationForwardStatement;
import jolie.lang.parse.ast.courier.SolicitResponseForwardStatement;
import jolie.lang.parse.ast.expression.*;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;

public abstract class ExprVisitorBase implements OLVisitor<Void, String> {

	static String compare2String(Scanner.TokenType compareType) {
		return switch (compareType) {
			case EQUAL -> "==";
			case NOT_EQUAL -> "!=";
			case RANGLE -> ">";
			case LANGLE -> "<";
			case MAJOR_OR_EQUAL -> ">=";
			case MINOR_OR_EQUAL -> "<=";
			default -> compareType.toString();
		};
	}

	static String operand2String(Constants.OperandType operandType) {
		return switch (operandType) {
			case ADD -> "+";
			case SUBTRACT -> "-";
			case MULTIPLY -> "*";
			case DIVIDE -> "/";
			case MODULUS -> "%";
		};
	}

	@Override
	public String visit(Program program, Void unused) {
		return "Program";
	}

	@Override
	public String visit(OneWayOperationDeclaration oneWayOperationDeclaration, Void unused) {
		return "OneWayOperationDeclaration";
	}

	@Override
	public String visit(RequestResponseOperationDeclaration requestResponseOperationDeclaration, Void unused) {
		return "RequestResponseOperationDeclaration";
	}

	@Override
	public String visit(DefinitionNode definitionNode, Void unused) {
		return "DefinitionNode";
	}

	@Override
	public String visit(ParallelStatement parallelStatement, Void unused) {
		return "ParallelStatement";
	}

	@Override
	public String visit(SequenceStatement sequenceStatement, Void unused) {
		return "SequenceStatement";
	}

	@Override
	public String visit(NDChoiceStatement ndChoiceStatement, Void unused) {
		return "NDChoiceStatement";
	}

	@Override
	public String visit(OneWayOperationStatement oneWayOperationStatement, Void unused) {
		return "OneWayOperationStatement";
	}

	@Override
	public String visit(RequestResponseOperationStatement requestResponseOperationStatement, Void unused) {
		return "RequestResponseOperationStatement";
	}

	@Override
	public String visit(NotificationOperationStatement notificationOperationStatement, Void unused) {
		return "NotificationOperationStatement";
	}

	@Override
	public String visit(SolicitResponseOperationStatement solicitResponseOperationStatement, Void unused) {
		return "SolicitResponseOperationStatement";
	}

	@Override
	public String visit(LinkInStatement linkInStatement, Void unused) {
		return "LinkInStatement";
	}

	@Override
	public String visit(LinkOutStatement linkOutStatement, Void unused) {
		return "LinkOutStatement";
	}

	@Override
	public String visit(AssignStatement assignStatement, Void unused) {
		return "AssignStatement";
	}

	@Override
	public String visit(AddAssignStatement addAssignStatement, Void unused) {
		return "AddAssignStatement";
	}

	@Override
	public String visit(SubtractAssignStatement subtractAssignStatement, Void unused) {
		return "SubtractAssignStatement";
	}

	@Override
	public String visit(MultiplyAssignStatement multiplyAssignStatement, Void unused) {
		return "MultiplyAssignStatement";
	}

	@Override
	public String visit(DivideAssignStatement divideAssignStatement, Void unused) {
		return "DivideAssignStatement";
	}

	@Override
	public String visit(IfStatement ifStatement, Void unused) {
		return "IfStatement";
	}

	@Override
	public String visit(DefinitionCallStatement definitionCallStatement, Void unused) {
		return "DefinitionCallStatement";
	}

	@Override
	public String visit(WhileStatement whileStatement, Void unused) {
		return "WhileStatement";
	}

	@Override
	public String visit(NullProcessStatement nullProcessStatement, Void unused) {
		return "NullProcessStatement";
	}

	@Override
	public String visit(Scope scope, Void unused) {
		return "Scope";
	}

	@Override
	public String visit(InstallStatement installStatement, Void unused) {
		return "InstallStatement";
	}

	@Override
	public String visit(CompensateStatement compensateStatement, Void unused) {
		return "CompensateStatement";
	}

	@Override
	public String visit(ThrowStatement throwStatement, Void unused) {
		return "ThrowStatement";
	}

	@Override
	public String visit(ExitStatement exitStatement, Void unused) {
		return "ExitStatement";
	}

	@Override
	public String visit(ExecutionInfo executionInfo, Void unused) {
		return "ExecutionInfo";
	}

	@Override
	public String visit(CorrelationSetInfo correlationSetInfo, Void unused) {
		return "CorrelationSetInfo";
	}

	@Override
	public String visit(InputPortInfo inputPortInfo, Void unused) {
		return "InputPortInfo";
	}

	@Override
	public String visit(OutputPortInfo outputPortInfo, Void unused) {
		return "OutputPortInfo";
	}

	@Override
	public String visit(PointerStatement pointerStatement, Void unused) {
		return "PointerStatement";
	}

	@Override
	public String visit(DeepCopyStatement deepCopyStatement, Void unused) {
		return "DeepCopyStatement";
	}

	@Override
	public String visit(RunStatement runStatement, Void unused) {
		return "RunStatement";
	}

	@Override
	public String visit(UndefStatement undefStatement, Void unused) {
		return "UndefStatement";
	}

	@Override
	public String visit(ValueVectorSizeExpressionNode valueVectorSizeExpressionNode, Void unused) {
		return "ValueVectorSizeExpressionNode";
	}

	@Override
	public String visit(ForStatement forStatement, Void unused) {
		return "ForStatement";
	}

	@Override
	public String visit(ForEachSubNodeStatement forEachSubNodeStatement, Void unused) {
		return "ForEachSubNodeStatement";
	}

	@Override
	public String visit(ForEachArrayItemStatement forEachArrayItemStatement, Void unused) {
		return "ForEachArrayItemStatement";
	}

	@Override
	public String visit(SpawnStatement spawnStatement, Void unused) {
		return "SpawnStatement";
	}

	@Override
	public String visit(IsTypeExpressionNode isTypeExpressionNode, Void unused) {
		return "IsTypeExpressionNode";
	}

	@Override
	public String visit(InstanceOfExpressionNode instanceOfExpressionNode, Void unused) {
		return "InstanceOfExpressionNode";
	}

	@Override
	public String visit(TypeCastExpressionNode typeCastExpressionNode, Void unused) {
		return "TypeCastExpressionNode";
	}

	@Override
	public String visit(SynchronizedStatement synchronizedStatement, Void unused) {
		return "SynchronizedStatement";
	}

	@Override
	public String visit(CurrentHandlerStatement currentHandlerStatement, Void unused) {
		return "CurrentHandlerStatement";
	}

	@Override
	public String visit(EmbeddedServiceNode embeddedServiceNode, Void unused) {
		return "EmbeddedServiceNode";
	}

	@Override
	public String visit(InstallFixedVariableExpressionNode installFixedVariableExpressionNode, Void unused) {
		return "InstallFixedVariableExpressionNode";
	}

	@Override
	public String visit(VariablePathNode variablePathNode, Void unused) {
		return "VariablePathNode";
	}

	@Override
	public String visit(TypeInlineDefinition typeInlineDefinition, Void unused) {
		return "TypeInlineDefinition";
	}

	@Override
	public String visit(TypeDefinitionLink typeDefinitionLink, Void unused) {
		return "TypeDefinitionLink";
	}

	@Override
	public String visit(InterfaceDefinition interfaceDefinition, Void unused) {
		return "InterfaceDefinition";
	}

	@Override
	public String visit(DocumentationComment documentationComment, Void unused) {
		return "DocumentationComment";
	}

	@Override
	public String visit(FreshValueExpressionNode freshValueExpressionNode, Void unused) {
		return "FreshValueExpressionNode";
	}

	@Override
	public String visit(CourierDefinitionNode courierDefinitionNode, Void unused) {
		return "CourierDefinitionNode";
	}

	@Override
	public String visit(CourierChoiceStatement courierChoiceStatement, Void unused) {
		return "CourierChoiceStatement";
	}

	@Override
	public String visit(NotificationForwardStatement notificationForwardStatement, Void unused) {
		return "NotificationForwardStatement";
	}

	@Override
	public String visit(SolicitResponseForwardStatement solicitResponseForwardStatement, Void unused) {
		return "SolicitResponseForwardStatement";
	}

	@Override
	public String visit(InterfaceExtenderDefinition interfaceExtenderDefinition, Void unused) {
		return "InterfaceExtenderDefinition";
	}

	@Override
	public String visit(InlineTreeExpressionNode inlineTreeExpressionNode, Void unused) {
		return "InlineTreeExpressionNode";
	}

	@Override
	public String visit(VoidExpressionNode voidExpressionNode, Void unused) {
		return "VoidExpressionNode";
	}

	@Override
	public String visit(ProvideUntilStatement provideUntilStatement, Void unused) {
		return "ProvideUntilStatement";
	}

	@Override
	public String visit(TypeChoiceDefinition typeChoiceDefinition, Void unused) {
		return "TypeChoiceDefinition";
	}

	@Override
	public String visit(ImportStatement importStatement, Void unused) {
		return "ImportStatement";
	}

	@Override
	public String visit(ServiceNode serviceNode, Void unused) {
		return "ServiceNode";
	}

	@Override
	public String visit(EmbedServiceNode embedServiceNode, Void unused) {
		return "EmbedServiceNode";
	}

	@Override
	public String visit(SolicitResponseExpressionNode solicitResponseExpressionNode, Void unused) {
		return "SolicitResponseExpressionNode";
	}

	@Override
	public String visit(IfExpressionNode ifExpressionNode, Void unused) {
		return "IfExpressionNode";
	}
}
