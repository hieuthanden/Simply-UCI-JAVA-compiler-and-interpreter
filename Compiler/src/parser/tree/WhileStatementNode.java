package parser.tree;

import java.util.Objects;

import common.Location;

public class WhileStatementNode extends StatementNode {
	private final ExpressionNode condition;
	private final StatementBlockNode body;

	public WhileStatementNode(Location location, ExpressionNode condition, StatementBlockNode body) {
		super(location);
		Objects.requireNonNull(condition);
		this.condition = condition;
		Objects.requireNonNull(body);
		this.body = body;
	}

	public ExpressionNode getCondition() {
		return condition;
	}

	public StatementBlockNode getBody() {
		return body;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "while (" + condition + ") " + body;
	}
}
