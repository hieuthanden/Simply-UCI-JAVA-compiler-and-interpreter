package parser.tree;

import java.util.Objects;

import common.Location;

public class ObjectCreationNode extends ExpressionNode {
	private final BasicTypeNode type;

	public ObjectCreationNode(Location location, BasicTypeNode type) {
		super(location);
		Objects.requireNonNull(type);
		this.type = type;
	}

	public BasicTypeNode getType() {
		return type;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public String toString() {
		return "new " + type + "()";
	}
}
