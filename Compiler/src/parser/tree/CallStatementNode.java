package parser.tree;

import java.util.Objects;

import common.Location;

public class CallStatementNode extends StatementNode {
	private final MethodCallNode call;

	public CallStatementNode(Location location, MethodCallNode call) {
		super(location);
		Objects.requireNonNull(call);
		this.call = call;
	}

	public MethodCallNode getCall() {
		return call;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return call + ";";
	}
}
