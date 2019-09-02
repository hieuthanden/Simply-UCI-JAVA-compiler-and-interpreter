package parser.tree;

import java.util.Objects;

import common.Location;

public class LocalDeclarationNode extends StatementNode {
	private final VariableNode variable;

	public LocalDeclarationNode(Location location, VariableNode variable) {
		super(location);
		Objects.requireNonNull(variable);
		this.variable = variable;
	}

	public VariableNode getVariable() {
		return variable;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public String toString() {
		return variable + ";";
	}
}
