package checker.symbols;

import java.util.List;

public abstract class Symbol {
	private final Symbol scope;
	private final String identifier;

	public Symbol(Symbol scope, String identifier) {
		this.scope = scope;
		this.identifier = identifier;
	}

	public Symbol getScope() {
		return scope;
	}

	public String getIdentifier() {
		return identifier;
	}

	public List<Symbol> allDeclarations() {
		return List.of();
	}

	@Override
	public String toString() {
		return identifier;
	}
}
