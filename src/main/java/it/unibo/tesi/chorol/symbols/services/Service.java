package it.unibo.tesi.chorol.symbols.services;

import it.unibo.tesi.chorol.symbols.interfaces.InterfaceHolder;
import it.unibo.tesi.chorol.symbols.ports.PortHolder;
import jolie.lang.Constants.ExecutionMode;
import jolie.lang.parse.ast.*;
import jolie.lang.parse.util.impl.ProgramInspectorCreatorVisitor;
import jolie.lang.parse.util.impl.ProgramInspectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class Service {
	private static final Logger logger = LoggerFactory.getLogger(Service.class);
	private final String name;
	private ExecutionMode executionMode;
	private final PortHolder<InputPortInfo> inputPortHolder = new PortHolder<>();
	private final PortHolder<OutputPortInfo> outputPortHolder = new PortHolder<>();

	public Service(ServiceNode serviceNode, ServiceHolder serviceHolder) {
		name = serviceNode.name();
		ProgramInspectorImpl inspector = (ProgramInspectorImpl) new ProgramInspectorCreatorVisitor(serviceNode.program()).createInspector();
		Arrays.stream(inspector.getInputPorts()).forEach(inputPortHolder::add);
		Arrays.stream(inspector.getOutputPorts()).forEach(outputPortHolder::add);

		loadEmbeddedServices(
				serviceNode.program().children().stream()
						.filter(EmbedServiceNode.class::isInstance)
						.map(EmbedServiceNode.class::cast)
						.collect(Collectors.toList()),
				serviceHolder
		);
		loadExecutionMode(inspector);
	}

	private void loadEmbeddedServices(List<EmbedServiceNode> nodes, ServiceHolder serviceHolder) {
		nodes.stream()
				.filter(EmbedServiceNode::hasBindingPort)
				.forEach(esn -> outputPortHolder.add(esn.bindingPort(), serviceHolder.get(esn.serviceName())));
	}

	private void loadExecutionMode(ProgramInspectorImpl inspector) {
		Queue<ServiceNode> q = new LinkedList<>(List.of(inspector.getServiceNodes()));
		while (!q.isEmpty() && executionMode == null) {
			Program p = q.remove().program();
			executionMode = p.children().stream()
					                .filter(a -> a instanceof ExecutionInfo)
					                .map(a -> ((ExecutionInfo) a).mode())
					                .findFirst().orElse(null);

			p.children().stream()
					.filter(a -> a instanceof ServiceNode)
					.map(a -> (ServiceNode) a)
					.forEach(q::add);
		}
		if (executionMode == null) {
			executionMode = ExecutionMode.SINGLE;
		}
	}

	public void bindInterfaces(InterfaceHolder interfaceHolder) {
		inputPortHolder.bindInterfaces(interfaceHolder);
		outputPortHolder.bindInterfaces(interfaceHolder);
	}


	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return String.format(
				"Service[%s]:\nInput ports:\n%s\nOutput ports:\n%s",
				name,
				inputPortHolder,
				outputPortHolder
		);
	}

}
