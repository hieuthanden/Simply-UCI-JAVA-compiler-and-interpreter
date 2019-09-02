package lexer.tokens;

import java.util.Objects;

import common.Location;

public abstract class Token {
	private final Location location;

	public Token(Location location) {
		Objects.requireNonNull(location);
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}
	
	@Override
	public abstract String toString();
}
