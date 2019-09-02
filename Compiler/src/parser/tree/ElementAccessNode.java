package parser.tree;

import java.util.Objects;

import common.Location;

public class ElementAccessNode extends DesignatorNode {
	private final DesignatorNode designator;
	private final ExpressionNode expression;

	public ElementAccessNode(Location location, DesignatorNode designator, ExpressionNode expression) {
		super(location);
		Objects.requireNonNull(designator);
		this.designator = designator;
		Objects.requireNonNull(expression);
		this.expression = expression;
	}

	public DesignatorNode getDesignator() {
		return designator;
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
		return designator + "[" + expression +"]";
	}
}
