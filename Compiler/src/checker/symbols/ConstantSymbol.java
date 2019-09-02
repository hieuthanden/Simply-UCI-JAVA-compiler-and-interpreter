package checker.symbols;

import java.util.Objects;

public class ConstantSymbol extends Symbol {
	private final TypeSymbol type;

	public ConstantSymbol(Symbol scope, String identifier, TypeSymbol type) {
		super(scope, identifier);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(identifier);
		Objects.requireNonNull(type);
		this.type = type;
	}

	public TypeSymbol getType() {
		return type;
	}
}
