package parser.tree;

import common.Location;

public class ReturnStatementNode extends StatementNode {
	private final ExpressionNode expression; // null if none

	public ReturnStatementNode(Location location, ExpressionNode expression) {
		super(location);
		this.expression = expression;
	}

	public ExpressionNode getExpression() {
		return expression;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		String text = "return";
		if (expression != null) {
			text += expression;
		}
		text += ";";
		return text;
	}
}
