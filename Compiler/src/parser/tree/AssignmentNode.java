package parser.tree;

import java.util.Objects;

import common.Location;

public class AssignmentNode extends StatementNode {
	private final DesignatorNode left;
	private final ExpressionNode right;

	public AssignmentNode(Location location, DesignatorNode left, ExpressionNode right) {
		super(location);
		Objects.requireNonNull(left);
		this.left = left;
		Objects.requireNonNull(right);
		this.right = right;
	}

	public DesignatorNode getLeft() {
		return left;
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
		return left + " = " + right + ";";
	}
}
