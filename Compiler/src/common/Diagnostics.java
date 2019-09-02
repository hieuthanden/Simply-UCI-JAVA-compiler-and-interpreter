package common;

import java.util.ArrayList;
import java.util.List;

public class Diagnostics {
	private final List<String> errors = new ArrayList<>();

	public void reportError(String message) {
		errors.add(message);
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	public List<String> getErrors() {
		return errors;
	}
}
