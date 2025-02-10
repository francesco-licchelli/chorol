package it.unibo.tesi.chorol.symbols.types;

import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.util.Range;

import static it.unibo.tesi.chorol.utils.Constants.LINK_TYPE;
import static it.unibo.tesi.chorol.utils.Misc.rangeToString;

public class Type {
	protected String name;
	protected String type;
	protected Range cardinality;

	public Type(TypeDefinition definition) {
		setName(definition);
		setType(definition);
		setCardinality(definition);
	}

	private void setName(TypeDefinition definition) {
		name = definition.name();
	}

	private void setType(TypeDefinition definition) {
		type = setTypeHelper(definition);
	}

	private void setCardinality(TypeDefinition definition) {
		cardinality = definition.cardinality();
	}

	private String setTypeHelper(TypeDefinition definition) {
		if (definition instanceof TypeChoiceDefinition) {
			TypeChoiceDefinition choice = (TypeChoiceDefinition) definition;
			return String.format("%s | %s", setTypeHelper(choice.left()), setTypeHelper(choice.right()));
		} else if (definition instanceof TypeInlineDefinition)
			return ((TypeInlineDefinition) definition).basicType().nativeType().name();
		else if (definition instanceof TypeDefinitionLink) {
			TypeDefinitionLink link = (TypeDefinitionLink) definition;
			return String.format(
					"%s -> %s",
					LINK_TYPE,
					link.linkedType() != null ? link.linkedType().name() : link.linkedTypeName()
			);
		} else
			return "";
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("%s ::= %s (%s)", name, type, rangeToString(cardinality));
	}
}
