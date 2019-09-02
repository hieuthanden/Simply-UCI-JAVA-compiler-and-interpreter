package parser.tree;

import java.util.Objects;

import common.Location;

public abstract class Node {
	private final Location location;
	
	public Node(Location location) {
		Objects.requireNonNull(location);
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}
	
	public abstract void accept(Visitor visitor);
		
	@Override
	public abstract String toString();
}
