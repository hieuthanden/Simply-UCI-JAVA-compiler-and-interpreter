package parser.tree;

import java.util.List;
import java.util.Objects;

import common.Location;

public class MethodNode extends Node {
	private final TypeNode returnType;
	private final String identifier;
	private final List<VariableNode> parameters;
	private final StatementBlockNode body;

	public MethodNode(Location location, TypeNode returnType, String identifier, List<VariableNode> parameters,
			StatementBlockNode body) {
		super(location);
		Objects.requireNonNull(returnType);
		this.returnType = returnType;
		Objects.requireNonNull(identifier);
		this.identifier = identifier;
		Objects.requireNonNull(parameters);
		this.parameters = parameters;
		Objects.requireNonNull(body);
		this.body = body;
	}

	public TypeNode getReturnType() {
		return returnType;
	}

	public String getIdentifier() {
		return identifier;
	}

	public List<VariableNode> getParameters() {
		return parameters;
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
		String text = returnType + " " + identifier + "(";
		for (int index = 0; index < parameters.size(); index++) {
			text += parameters.get(index);
			if (index < parameters.size() - 1) {
				text += ", ";
			}
		}
		text += ") " + body;
		return text;
	}
}
