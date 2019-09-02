package lexer.tokens;

import java.util.Objects;

import common.Location;

public class StaticToken extends Token {
	private final Tag tag;

	public StaticToken(Location location, Tag tag) {
		super(location);
		Objects.requireNonNull(tag);
		this.tag = tag;
	}

	public Tag getTag() {
		return tag;
	}

	@Override
	public String toString() {
		return "TOKEN " + tag + " " + getLocation();
	}
}
