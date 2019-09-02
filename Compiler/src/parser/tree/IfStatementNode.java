package parser.tree;

import java.util.Objects;

import common.Location;

public class IfStatementNode extends StatementNode {
	private final ExpressionNode condition;
	private final StatementBlockNode thenBlock;
	private final StatementBlockNode elseBlock; // null if none

	public IfStatementNode(Location location, ExpressionNode condition, StatementBlockNode thenBlock,
			StatementBlockNode elseBlock) {
		super(location);
		Objects.requireNonNull(condition);
		this.condition = condition;
		Objects.requireNonNull(thenBlock);
		this.thenBlock = thenBlock;
		this.elseBlock = elseBlock;
	}

	public ExpressionNode getCondition() {
		return condition;
	}

	public StatementBlockNode getThenBlock() {
		return thenBlock;
	}

	public StatementBlockNode getElseBlock() {
		return elseBlock;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		var text = "if (" + condition + ")" + thenBlock;
		if (elseBlock != null) {
			text += "else " + elseBlock;
		}
		return text;
	}
}
