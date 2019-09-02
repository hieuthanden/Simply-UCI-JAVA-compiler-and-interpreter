package parser.tree;

import common.Location;

public class IntegerLiteralNode extends ExpressionNode {
	private final int value;

	public IntegerLiteralNode(Location location, int value) {
		super(location);
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}
}
