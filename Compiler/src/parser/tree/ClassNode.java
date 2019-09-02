package parser.tree;

import java.util.List;
import java.util.Objects;

import common.Location;

public class ClassNode extends Node {
	private final String identifier;
	private final BasicTypeNode baseClass; // null if none
	private final List<VariableNode> variables;
	private final List<MethodNode> methods;

	public ClassNode(Location location, String identifier, BasicTypeNode baseClass, List<VariableNode> variables,
			List<MethodNode> methods) {
		super(location);
		Objects.requireNonNull(identifier);
		this.identifier = identifier;
		this.baseClass = baseClass;
		Objects.requireNonNull(variables);
		this.variables = variables;
		Objects.requireNonNull(methods);
		this.methods = methods;
	}

	public String getIdentifier() {
		return identifier;
	}

	public BasicTypeNode getBaseClass() {
		return baseClass;
	}

	public List<VariableNode> getVariables() {
		return variables;
	}

	public List<MethodNode> getMethods() {
		return methods;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		var text = "class " + identifier;
		if (baseClass != null) {
			text += " extends " + baseClass;
		}
		text += "{\r\n";
		for (var variable : variables) {
			text += variable + ";\r\n";
		}
		for (var method : methods) {
			text += method + "\r\n";
		}
		text += "}";
		return text;
	}
}
