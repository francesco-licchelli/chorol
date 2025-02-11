package it.unibo.tesi.chorol.symbols.services;

import it.unibo.tesi.chorol.symbols.interfaces.InterfaceHolder;
import it.unibo.tesi.chorol.symbols.ports.PortHolder;
import jolie.lang.parse.ast.EmbedServiceNode;
import jolie.lang.parse.ast.InputPortInfo;
import jolie.lang.parse.ast.OutputPortInfo;
import jolie.lang.parse.ast.ServiceNode;
import jolie.lang.parse.util.impl.ProgramInspectorCreatorVisitor;
import jolie.lang.parse.util.impl.ProgramInspectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Service {
	private static final Logger logger = LoggerFactory.getLogger(Service.class);
	private final String name;
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
	}

	private void loadEmbeddedServices(List<EmbedServiceNode> nodes, ServiceHolder serviceHolder) {
		nodes.stream()
				.filter(EmbedServiceNode::hasBindingPort)
				.forEach(esn -> outputPortHolder.add(esn.bindingPort(), serviceHolder.get(esn.serviceName())));
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
