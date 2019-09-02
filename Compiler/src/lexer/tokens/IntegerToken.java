package lexer.tokens;

import common.Location;

public class IntegerToken extends Token {
	private final int value;

	public IntegerToken(Location location, int value) {
		super(location);
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "INTEGER " + value + " " + getLocation();
	}
}
