package checker.symbols;

import java.util.ArrayList;
import java.util.List;

public class ClassSymbol extends TypeSymbol {
	private TypeSymbol baseClass;
	private final List<FieldSymbol> fields = new ArrayList<>();
	private final List<MethodSymbol> methods = new ArrayList<>();
	
	public ClassSymbol(GlobalScope scope, String identifier) {
		super(scope, identifier);
	}

	public TypeSymbol getBaseClass() {
		return baseClass;
	}

	public void setBaseClass(TypeSymbol baseClass) {
		this.baseClass = baseClass;
	}

	public List<FieldSymbol> getFields() {
		return fields;
	}

	public List<MethodSymbol> getMethods() {
		return methods;
	}

	@Override
	public boolean isReferenceType() {
		return true;
	}

	@Override
	public boolean isCompatibleTo(TypeSymbol target) {
		return super.isCompatibleTo(target)
				|| target instanceof ClassSymbol && transitiveBaseClasses().contains(target);
	}

	@Override
	public List<Symbol> allDeclarations() {
		var declarations = new ArrayList<Symbol>();
		declarations.addAll(fields);
		declarations.addAll(methods);
		if (baseClass != null) {
			for (var baseDeclaration : baseClass.allDeclarations()) {
				if (!declarations.stream()
						.anyMatch(symbol -> symbol.getIdentifier().equals(baseDeclaration.getIdentifier()))) {
					declarations.add(baseDeclaration);
				}
			}
		}
		return declarations;
	}

	public List<ClassSymbol> transitiveBaseClasses() {
		var result = new ArrayList<ClassSymbol>();
		if (baseClass instanceof ClassSymbol) {
			var direct = (ClassSymbol) baseClass;
			result.add(direct);
			for (var indirect : direct.transitiveBaseClasses()) {
				result.add(indirect);
			}
		}
		return result;
	}
}
