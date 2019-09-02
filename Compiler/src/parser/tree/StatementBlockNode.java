package parser.tree;

import java.util.List;
import java.util.Objects;

import common.Location;

public class StatementBlockNode extends Node {
	private final List<StatementNode> statements;

	public StatementBlockNode(Location location, List<StatementNode> statements) {
		super(location);
		Objects.requireNonNull(statements);
		this.statements = statements;
	}

	public List<StatementNode> getStatements() {
		return statements;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		String text = "{\r\n";
		for (var statement : statements) {
			text += statement + "\r\n";
		}
		text += "}";
		return text;
	}
}
