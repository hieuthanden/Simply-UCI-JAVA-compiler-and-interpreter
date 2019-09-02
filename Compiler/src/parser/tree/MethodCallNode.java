package parser.tree;

import java.util.List;
import java.util.Objects;

import common.Location;

public class MethodCallNode extends ExpressionNode {
	private final DesignatorNode designator;
	private final List<ExpressionNode> arguments;

	public MethodCallNode(Location location, DesignatorNode designator, List<ExpressionNode> arguments) {
		super(location);
		Objects.requireNonNull(designator);
		this.designator = designator;
		Objects.requireNonNull(arguments);
		this.arguments = arguments;
	}

	public DesignatorNode getDesignator() {
		return designator;
	}

	public List<ExpressionNode> getArguments() {
		return arguments;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		String text = designator + "(";
		for (int index = 0; index < arguments.size(); index++) {
			text += arguments.get(index);
			if (index < arguments.size() - 1) {
				text += ", ";
			}
		}
		text += ")";
		return text;
	}
}
