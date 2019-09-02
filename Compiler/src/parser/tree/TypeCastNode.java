package parser.tree;

import java.util.Objects;

import common.Location;

public class TypeCastNode extends ExpressionNode {
	private final BasicTypeNode type;
	private final DesignatorNode designator;

	public TypeCastNode(Location location, BasicTypeNode type, DesignatorNode designator) {
		super(location);
		Objects.requireNonNull(type);
		this.type = type;
		Objects.requireNonNull(designator);
		this.designator = designator;
	}

	public BasicTypeNode getType() {
		return type;
	}

	public DesignatorNode getDesignator() {
		return designator;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "(" + type + ")" + designator;
	}
}
