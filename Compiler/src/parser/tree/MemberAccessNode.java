package parser.tree;

import java.util.Objects;

import common.Location;

public class MemberAccessNode extends DesignatorNode {
	private final DesignatorNode designator;
	private final String identifier;

	public MemberAccessNode(Location location, DesignatorNode designator, String identifier) {
		super(location);
		Objects.requireNonNull(designator);
		this.designator = designator;
		Objects.requireNonNull(identifier);
		this.identifier = identifier;
	}

	public DesignatorNode getDesignator() {
		return designator;
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
		return designator + "." + identifier;
	}
}
