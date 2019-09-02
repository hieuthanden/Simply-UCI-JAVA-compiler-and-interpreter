package parser.tree;

import java.util.Objects;

import common.Location;

public class UnaryExpressionNode extends ExpressionNode {
	private final Operator operator;
	private final ExpressionNode operand;

	public UnaryExpressionNode(Location location, Operator operator, ExpressionNode operand) {
		super(location);
		Objects.requireNonNull(operator);
		this.operator = operator;
		Objects.requireNonNull(operand);
		this.operand = operand;
	}

	public Operator getOperator() {
		return operator;
	}

	public ExpressionNode getOperand() {
		return operand;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return operator + " " + operand;
	}
}
