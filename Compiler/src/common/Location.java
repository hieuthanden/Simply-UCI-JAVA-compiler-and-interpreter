package common;

import java.util.Objects;

public class Location {
	private final int start;
	private final int end;
	
	public Location(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		var other = (Location)obj;
		return start == other.start && end == other.end;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(start, end);
	}

	@Override
	public String toString() {
		return "(" + start + ", " + end + ")";
	}
}
