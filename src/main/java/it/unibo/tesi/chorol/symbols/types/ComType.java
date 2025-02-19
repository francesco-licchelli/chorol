package it.unibo.tesi.chorol.symbols.types;

import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeInlineDefinition;

import java.util.stream.Collectors;

public class ComType extends Type {
	private final TypeHolder typeHolder = new TypeHolder();

	ComType(TypeInlineDefinition definition) {
		super(definition);
		definition.subTypes().forEach(entry -> {
			TypeDefinition typeDefinition = entry.getValue();
			typeHolder.add(typeDefinition);
		});
	}

	@Override
	public String toString() {
		return String.format(
				"%s\n%s",
				super.toString(),
				typeHolder.get().stream()
						.map(Type::toString)
						.collect(Collectors.joining("\n\t"))
		);
	}


}
