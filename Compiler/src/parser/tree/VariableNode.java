package parser.tree;

import java.util.Objects;

import common.Location;

public class VariableNode extends Node {
	private final TypeNode type;
	private final String identifier;

	public VariableNode(Location location, TypeNode type, String identifier) {
		super(location);
		Objects.requireNonNull(type);
		this.type = type;
		Objects.requireNonNull(identifier);
		this.identifier = identifier;
	}

	public TypeNode getType() {
		return type;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return type + " " + identifier;
	}
}
