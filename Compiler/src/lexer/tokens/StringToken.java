package lexer.tokens;

import java.util.Objects;

import common.Location;

public class StringToken extends Token {
	private final String value;

	public StringToken(Location location, String value) {
		super(location);
		Objects.requireNonNull(value);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "STRING \"" + value + "\" " + getLocation();
	}
}
