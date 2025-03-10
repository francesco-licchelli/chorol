package it.unibo.tesi.joliegraph.symbols.services;

import it.unibo.tesi.joliegraph.symbols.interfaces.InterfaceHolder;
import it.unibo.tesi.joliegraph.symbols.ports.PortHolder;
import jolie.lang.Constants.ExecutionMode;
import jolie.lang.parse.ast.*;
import jolie.lang.parse.util.impl.ProgramInspectorCreatorVisitor;
import jolie.lang.parse.util.impl.ProgramInspectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class Service {
	private static final Logger logger = LoggerFactory.getLogger(Service.class);
	private final String name;
	private final PortHolder<InputPortInfo> inputPortHolder = new PortHolder<>();
	private final PortHolder<OutputPortInfo> outputPortHolder = new PortHolder<>();
	private final HashMap<String, OLSyntaxNode> definitions = new HashMap<>();
	private ExecutionMode executionMode;

	Service(ServiceNode serviceNode, ServiceHolder serviceHolder) {
		//Da qualche parte sono salvati i definition node... li devo trovare
		this.name = serviceNode.name();
		ProgramInspectorImpl inspector = (ProgramInspectorImpl) new ProgramInspectorCreatorVisitor(serviceNode.program()).createInspector();
		Arrays.stream(inspector.getInputPorts()).forEach(this.inputPortHolder::add);
		Arrays.stream(inspector.getOutputPorts()).forEach(this.outputPortHolder::add);

		this.loadEmbeddedServices(
				serviceNode.program().children().stream()
						.filter(EmbedServiceNode.class::isInstance)
						.map(EmbedServiceNode.class::cast)
						.collect(Collectors.toList()),
				serviceHolder
		);
		this.loadExecutionMode(serviceNode);
		this.loadDefinitions(serviceNode);
	}

	private void loadEmbeddedServices(List<EmbedServiceNode> nodes, ServiceHolder serviceHolder) {
		nodes.stream()
				.filter(EmbedServiceNode::hasBindingPort)
				.forEach(esn -> this.outputPortHolder.add(esn.bindingPort(), serviceHolder.get(esn.serviceName())));
	}

	private void loadExecutionMode(ServiceNode serviceNode) {
		Queue<ServiceNode> q = new LinkedList<>(List.of(serviceNode));
		while (!q.isEmpty() && this.executionMode == null) {
			Program p = q.remove().program();
			this.executionMode = p.children().stream()
					                     .filter(ExecutionInfo.class::isInstance)
					                     .map(ExecutionInfo.class::cast)
					                     .map(ExecutionInfo::mode)
					                     .findFirst().orElse(null);

			p.children().stream()
					.filter(ServiceNode.class::isInstance)
					.map(ServiceNode.class::cast)
					.forEach(q::add);

		}
		if (this.executionMode == null) this.executionMode = ExecutionMode.SINGLE;
	}

	private void loadDefinitions(ServiceNode serviceNode) {
		serviceNode.program().children().stream()
				.filter(DefinitionNode.class::isInstance)
				.map(DefinitionNode.class::cast)
				.forEach(definitionNode -> this.definitions.put(definitionNode.id(), definitionNode.body()));
	}

	public OLSyntaxNode getDefinition(String id) {
		return this.definitions.get(id);
	}

	public void bindInterfaces(InterfaceHolder interfaceHolder) {
		this.inputPortHolder.bindInterfaces(interfaceHolder);
		this.outputPortHolder.bindInterfaces(interfaceHolder);
	}

	public PortHolder<InputPortInfo> getInputPortHolder() {
		return this.inputPortHolder;
	}

	public PortHolder<OutputPortInfo> getOutputPortHolder() {
		return this.outputPortHolder;
	}

	public ExecutionMode getExecutionMode() {
		return this.executionMode;
	}


	public String name() {
		return this.name;
	}


	@Override
	public String toString() {
		return String.format(
				"Service[%s]:\nExecutionMode: %s\nInput ports:\n%s\nOutput ports:\n%s",
				this.name,
				this.executionMode,
				this.inputPortHolder,
				this.outputPortHolder
		);
	}

}
