package lexer.tokens;

import java.util.Objects;

import common.Location;

public class IdentifierToken extends Token {
	private final String value;
	
	public IdentifierToken(Location location, String value) {
		super(location);
		Objects.requireNonNull(value);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "IDENTIFIER " + value + " " + getLocation(); 
	}
}
