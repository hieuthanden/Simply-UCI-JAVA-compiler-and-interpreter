package parser.tree;

import java.util.Objects;

import common.Location;

public class BinaryExpressionNode extends ExpressionNode {
	private final ExpressionNode left;
	private final Operator operator;
	private final ExpressionNode right;

	public BinaryExpressionNode(Location location, ExpressionNode left, Operator operator, ExpressionNode right) {
		super(location);
		Objects.requireNonNull(left);
		this.left = left;
		Objects.requireNonNull(operator);
		this.operator = operator;
		Objects.requireNonNull(right);
		this.right = right;
	}

	public ExpressionNode getLeft() {
		return left;
	}

	public Operator getOperator() {
		return operator;
	}

	public ExpressionNode getRight() {
		return right;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return left + " " + operator + " " + right;
	}
}
