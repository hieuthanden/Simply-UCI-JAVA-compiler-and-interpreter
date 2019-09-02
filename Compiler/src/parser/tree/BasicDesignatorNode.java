package parser.tree;

import java.util.Objects;

import common.Location;

public class BasicDesignatorNode extends DesignatorNode {
	private final String identifier;

	public BasicDesignatorNode(Location location, String identifier) {
		super(location);
		Objects.requireNonNull(identifier);
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return identifier;
	}
}
