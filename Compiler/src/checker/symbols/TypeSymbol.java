package checker.symbols;

import java.util.Objects;

public abstract class TypeSymbol extends Symbol {
	public TypeSymbol(Symbol scope, String identifier) {
		super(scope, identifier);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(identifier);
	}

	public abstract boolean isReferenceType();

	public boolean isCompatibleTo(TypeSymbol target) {
		return this == target;
	}
}
