package it.unibo.tesi.joliegraph.visitor.expression;

import jolie.lang.Constants;
import jolie.lang.parse.ast.*;
import jolie.lang.parse.ast.expression.*;
import jolie.util.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ExprVisitor extends ExprVisitorBase {

	private int getPrecedence(OLSyntaxNode node) {
		if (node instanceof OrConditionNode) return 1;
		else if (node instanceof AndConditionNode) return 2;
		else if (node instanceof CompareConditionNode) return 3;
		else if (node instanceof SumExpressionNode) return 4;
		else if (node instanceof ProductExpressionNode) return 5;
		return 6;
	}

	private String visitWithParenthesisIfNeeded(OLSyntaxNode node, int parentPrecedence, Void unused) {
		String result = node.accept(this, unused);
		if (this.getPrecedence(node) < parentPrecedence) return "(" + result + ")";
		return result;
	}

	@Override
	public String visit(OrConditionNode orConditionNode, Void unused) {
		int currentPrecedence = this.getPrecedence(orConditionNode);
		return orConditionNode.children().stream()
				       .map(child -> this.visitWithParenthesisIfNeeded(child, currentPrecedence, unused))
				       .collect(Collectors.joining(" || "));
	}

	@Override
	public String visit(AndConditionNode andConditionNode, Void unused) {
		int currentPrecedence = this.getPrecedence(andConditionNode);
		return andConditionNode.children().stream()
				       .map(child -> this.visitWithParenthesisIfNeeded(child, currentPrecedence, unused))
				       .collect(Collectors.joining(" && "));
	}

	@Override
	public String visit(CompareConditionNode compareConditionNode, Void unused) {
		int currentPrecedence = this.getPrecedence(compareConditionNode);
		String left = this.visitWithParenthesisIfNeeded(compareConditionNode.leftExpression(), currentPrecedence, unused);
		String right = this.visitWithParenthesisIfNeeded(compareConditionNode.rightExpression(), currentPrecedence, unused);
		return String.format("%s %s %s",
				left,
				ExprVisitorBase.compare2String(compareConditionNode.opType()),
				right);
	}

	@Override
	public String visit(SumExpressionNode sumExpressionNode, Void unused) {
		int currentPrecedence = this.getPrecedence(sumExpressionNode);
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sumExpressionNode.operands()
				.forEach(entry ->
						         this.visitWithParenthesisIfNeeded(entry.value(), currentPrecedence, unused));
		for (var entry : sumExpressionNode.operands()) {
			String operandString = this.visitWithParenthesisIfNeeded(entry.value(), currentPrecedence, unused);
			if (operandString == null || operandString.isBlank()) continue;
			String opStr = ExprVisitorBase.operand2String(entry.key());
			if (!first) sb.append(" ").append(opStr).append(" ").append(operandString);
			else {
				if (opStr.equals("+")) opStr = "";
				sb.append(opStr).append(operandString);
				first = false;
			}
		}
		return sb.toString();
	}

	@Override
	public String visit(ProductExpressionNode productExpressionNode, Void unused) {
		int currentPrecedence = this.getPrecedence(productExpressionNode);
		List<Pair<Constants.OperandType, OLSyntaxNode>> operands = productExpressionNode.operands();
		if (operands.isEmpty()) return "";
		Iterator<Pair<Constants.OperandType, OLSyntaxNode>> iter = operands.iterator();
		Pair<Constants.OperandType, OLSyntaxNode> firstEntry = iter.next();
		StringBuilder sb = new StringBuilder();
		sb.append(this.visitWithParenthesisIfNeeded(firstEntry.value(), currentPrecedence, unused));
		while (iter.hasNext()) {
			Pair<Constants.OperandType, OLSyntaxNode> entry = iter.next();
			String opStr = ExprVisitorBase.operand2String(entry.key());
			String operandString = this.visitWithParenthesisIfNeeded(entry.value(), currentPrecedence, unused);
			sb.append(" ").append(opStr).append(" ").append(operandString);
		}

		return sb.toString();
	}

	@Override
	public String visit(ConstantIntegerExpression constantIntegerExpression, Void unused) {
		return String.valueOf(constantIntegerExpression.value());
	}

	@Override
	public String visit(VariableExpressionNode variableExpressionNode, Void unused) {
		return variableExpressionNode.variablePath().toPrettyString();
	}

	@Override
	public String visit(NotExpressionNode notExpressionNode, Void unused) {
		return "!" + notExpressionNode.expression().accept(this, unused);
	}


	@Override
	public String visit(ConstantDoubleExpression constantDoubleExpression, Void unused) {
		return Double.toString(constantDoubleExpression.value());
	}

	@Override
	public String visit(ConstantBoolExpression constantBoolExpression, Void unused) {
		return Boolean.toString(constantBoolExpression.value());
	}

	@Override
	public String visit(ConstantLongExpression constantLongExpression, Void unused) {
		return Long.toString(constantLongExpression.value());
	}

	@Override
	public String visit(ConstantStringExpression constantStringExpression, Void unused) {
		return String.format("\"%s\"", constantStringExpression.value());
	}

	@Override
	public String visit(PreIncrementStatement preIncrementStatement, Void unused) {
		return "++";
	}

	@Override
	public String visit(PostIncrementStatement postIncrementStatement, Void unused) {
		return "++";
	}

	@Override
	public String visit(PreDecrementStatement preDecrementStatement, Void unused) {
		return "--";
	}

	@Override
	public String visit(PostDecrementStatement postDecrementStatement, Void unused) {
		return "--";
	}


}
