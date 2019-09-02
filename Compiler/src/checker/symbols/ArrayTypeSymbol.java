package checker.symbols;

public class ArrayTypeSymbol extends TypeSymbol {
	private final TypeSymbol elementType;

	public ArrayTypeSymbol(Symbol scope, TypeSymbol elementType) {
		super(scope, elementType.getIdentifier() + "[]");
		this.elementType = elementType;
	}

	public TypeSymbol getElementType() {
		return elementType;
	}
	
	@Override
	public boolean isReferenceType() {
		return true;
	}
}
