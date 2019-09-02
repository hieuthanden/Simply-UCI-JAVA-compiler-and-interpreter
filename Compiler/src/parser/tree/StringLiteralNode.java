package parser.tree;

import java.util.Objects;

import common.Location;

public class StringLiteralNode extends ExpressionNode {
	private final String value;

	public StringLiteralNode(Location location, String value) {
		super(location);
		Objects.requireNonNull(value);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "\"" + value + "\"";
	}
}
