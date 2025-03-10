package it.unibo.tesi.joliegraph.symbols.types;

import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.util.Range;

import static it.unibo.tesi.joliegraph.utils.Constants.LINK_TYPE;
import static it.unibo.tesi.joliegraph.utils.Misc.rangeToString;

public class Type {
	protected String name;
	protected String type;
	private Range cardinality;

	public Type(TypeDefinition definition) {
		this.setName(definition);
		this.setType(definition);
		this.setCardinality(definition);
	}

	private void setName(TypeDefinition definition) {
		this.name = definition.name();
	}

	private void setType(TypeDefinition definition) {
		this.type = this.setTypeHelper(definition);
	}

	private void setCardinality(TypeDefinition definition) {
		this.cardinality = definition.cardinality();
	}

	private String setTypeHelper(TypeDefinition definition) {
		if (definition instanceof TypeChoiceDefinition choice)
			return String.format("%s | %s", this.setTypeHelper(choice.left()), this.setTypeHelper(choice.right()));
		else if (definition instanceof TypeInlineDefinition)
			return ((TypeInlineDefinition) definition).basicType().nativeType().name();
		else if (definition instanceof TypeDefinitionLink link) return String.format(
				"%s -> %s",
				LINK_TYPE,
				link.linkedType() != null ? link.linkedType().name() : link.linkedTypeName()
		);
		else
			return "";
	}

	public String name() {
		return this.name;
	}

	@Override
	public String toString() {
		return String.format("%s ::= %s (%s)", this.name, this.type, rangeToString(this.cardinality));
	}
}
