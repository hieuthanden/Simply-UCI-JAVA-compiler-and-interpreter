package parser.tree;

import java.util.Objects;

import common.Location;

public class ArrayTypeNode extends TypeNode {
	private final TypeNode elementType;

	public ArrayTypeNode(Location location, TypeNode elementType) {
		super(location);
		Objects.requireNonNull(elementType);
		this.elementType = elementType;
	}

	public TypeNode getElementType() {
		return elementType;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return elementType + "[]";
	}
}
