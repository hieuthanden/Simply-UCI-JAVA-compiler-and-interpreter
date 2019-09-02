package checker.symbols;

import java.util.Objects;

public abstract class VariableSymbol extends Symbol {
	private TypeSymbol type;

	public VariableSymbol(Symbol scope, String identifier) {
		super(scope, identifier);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(identifier);
	}

	public TypeSymbol getType() {
		return type;
	}

	public void setType(TypeSymbol type) {
		this.type = type;
	}
}
