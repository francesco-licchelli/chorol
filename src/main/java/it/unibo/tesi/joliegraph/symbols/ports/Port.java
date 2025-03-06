package it.unibo.tesi.joliegraph.symbols.ports;

import it.unibo.tesi.joliegraph.symbols.interfaces.InterfaceHolder;
import it.unibo.tesi.joliegraph.symbols.interfaces.operations.Operation;
import it.unibo.tesi.joliegraph.symbols.interfaces.operations.OperationHolder;
import it.unibo.tesi.joliegraph.utils.Misc;
import jolie.lang.parse.ast.*;
import jolie.lang.parse.ast.expression.*;
import jolie.lang.parse.ast.expression.InlineTreeExpressionNode.AssignmentOperation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Port<T extends PortInfo> {
	private final HashMap<String, String> protocolInfo = new HashMap<>();
	private final InterfaceHolder interfaceHolder = new InterfaceHolder();
	private final OperationHolder operationHolder = new OperationHolder();
	private final String name;
	private String location;
	private String protocol;

	Port(T portInfo) {
		this.name = portInfo.id();
		this.setLocation(portInfo);
		this.setProtocol(portInfo);
		this.setProtocolInfo(portInfo);
		this.setInterfaces(portInfo.getInterfaceList());
		this.setOperations(portInfo.operationsMap());
	}

	public void setLocation(PortInfo portInfo) {
		SumExpressionNode location = null;
		if (portInfo instanceof InputPortInfo)
			location = (SumExpressionNode) ((InputPortInfo) portInfo).location();
		else if (portInfo instanceof OutputPortInfo)
			location = (SumExpressionNode) ((OutputPortInfo) portInfo).location();
		if (location != null) {
			ProductExpressionNode pen = (ProductExpressionNode) location.operands().get(0).value();
			this.location = pen.operands().get(0).value().toString();
		} else
			this.location = "UNKNOWN";
	}

	public void setProtocol(PortInfo portInfo) {
		if (portInfo instanceof InputPortInfo)
			this.protocol = ((InputPortInfo) portInfo).protocolId();
		else if (portInfo instanceof OutputPortInfo)
			this.protocol = ((OutputPortInfo) portInfo).protocolId();
		else
			this.protocol = "UNKNOWN";
	}


	private void setProtocolInfo(PortInfo portInfo) {
		SumExpressionNode protocolInfoNode = null;
		if (portInfo instanceof InputPortInfo)
			protocolInfoNode = (SumExpressionNode) ((InputPortInfo) portInfo).protocol();
		else if (portInfo instanceof OutputPortInfo)
			protocolInfoNode = (SumExpressionNode) ((OutputPortInfo) portInfo).protocol();
		if (protocolInfoNode != null) {
			ProductExpressionNode pen = (ProductExpressionNode) protocolInfoNode.operands().get(0).value();
			OLSyntaxNode olSyntaxNode = pen.operands().get(0).value();
			if (olSyntaxNode instanceof InlineTreeExpressionNode inlineTreeNode)
				Arrays.stream(inlineTreeNode.operations())
						.map(op -> (AssignmentOperation) op)
						.forEach(aop -> {
							String key = Misc.getProtocolInfoKey(aop);
							OrConditionNode ocn = (OrConditionNode) aop.expression();
							AndConditionNode acn = (AndConditionNode) ocn.children().get(0);
							SumExpressionNode sumExpression = (SumExpressionNode) acn.children().get(0);
							String value = Misc.getProtocolInfoValue(sumExpression);
							this.protocolInfo.put(key, value);
						});
		}
	}

	public void setInterfaces(List<InterfaceDefinition> interfaces) {
		interfaces.forEach(this.interfaceHolder::add);
	}

	public void setOperations(Map<String, OperationDeclaration> operationsMap) {
		operationsMap.forEach(this.operationHolder::add);
	}


	public String getName() {
		return this.name;
	}

	InterfaceHolder getInterfaceHolder() {
		return this.interfaceHolder;
	}

	OperationHolder getOperationHolder() {
		return this.operationHolder;
	}


	public void bindInterfaces(InterfaceHolder interfaceHolder) {
		this.interfaceHolder.replace(interfaceHolder);
	}

	public Operation getOperation(String operationId) {
		Operation op = this.operationHolder.get(operationId);
		if (op != null) return op;
		return this.interfaceHolder.getOperation(operationId);
	}

	@Override
	public String toString() {
		return String.format(
				"[Port[%s]:\nProtocol: %s\nLocation: %s%s%s]",
				this.name,
				this.protocol,
				this.location,
				this.protocolInfo.isEmpty() ? "" : (": " + this.protocolInfo),
				this.interfaceHolder.get().isEmpty() ? "" : ("\nInterfaces:\n" + this.interfaceHolder)
		);
	}
}
