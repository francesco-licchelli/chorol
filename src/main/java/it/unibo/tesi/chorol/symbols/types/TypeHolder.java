package it.unibo.tesi.chorol.symbols.types;

import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeInlineDefinition;

import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TypeHolder {
	private final HashMap<String, Type> types = new HashMap<>();

	public static boolean isComposite(TypeDefinition typeDefinition) {
		return typeDefinition instanceof TypeInlineDefinition && ((TypeInlineDefinition) typeDefinition).hasSubTypes();
	}

	public static Type getType(TypeDefinition typeDefinition) {
		if (isComposite(typeDefinition))
			return new ComType((TypeInlineDefinition) typeDefinition);
		else
			return new Type(typeDefinition);
	}

	public void add(TypeDefinition typeDefinition) {
		if (isComposite(typeDefinition))
			types.put(typeDefinition.name(), new ComType((TypeInlineDefinition) typeDefinition));
		else
			types.put(typeDefinition.name(), new Type(typeDefinition));
	}

	public Type get(String typeName) {
		return types.get(typeName);
	}

	public Collection<Type> get() {
		return types.values();
	}

	@Override
	public String toString() {
		return types.values().stream()
				       .map(Type::toString).collect(Collectors.joining("\n"));
	}

}
