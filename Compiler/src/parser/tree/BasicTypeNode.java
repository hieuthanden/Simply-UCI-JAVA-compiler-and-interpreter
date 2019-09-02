package parser.tree;

import java.util.Objects;

import common.Location;

public class BasicTypeNode extends TypeNode {
	private final String identifier;
	
	public BasicTypeNode(Location location, String identifier) {
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
