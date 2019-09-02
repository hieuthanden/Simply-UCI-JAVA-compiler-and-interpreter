package checker.symbols;

public class InbuiltTypeSymbol extends TypeSymbol {
	public InbuiltTypeSymbol(Symbol scope, String identifier) {
		super(scope, identifier);
	}

	@Override
	public boolean isReferenceType() {
		return this == ((GlobalScope) getScope()).getStringType();
	}

	public boolean isCompatibleTo(TypeSymbol target) {
		return super.isCompatibleTo(target) || 
				this == ((GlobalScope) getScope()).getNullType() && target != null && target.isReferenceType();
	}
}
