package parser.tree;

import java.util.Objects;

import common.Location;

public class ArrayCreationNode extends ExpressionNode {
	private final TypeNode elementType;
	private final ExpressionNode expression;

	public ArrayCreationNode(Location location, TypeNode elementType, ExpressionNode expression) {
		super(location);
		Objects.requireNonNull(elementType);
		this.elementType = elementType;
		Objects.requireNonNull(expression);
		this.expression = expression;
	}

	public TypeNode getElementType() {
		return elementType;
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
		return "new " + elementType + "[" + expression + "]";
	}
}
