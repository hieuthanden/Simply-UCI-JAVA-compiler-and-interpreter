package checker.symbols;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MethodSymbol extends Symbol {
	private TypeSymbol returnType;
	private final ConstantSymbol thisConstant;
	private final List<ParameterSymbol> parameters = new ArrayList<>();
	private final List<LocalSymbol> locals = new ArrayList<>();

	public MethodSymbol(Symbol scope, String identifier) {
		super(scope, identifier);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(identifier);
		if (scope instanceof ClassSymbol) {
			thisConstant = new ConstantSymbol(this, GlobalScope.THIS_CONSTANT_NAME, (ClassSymbol) scope);
		} else {
			thisConstant = null;
		}
	}

	public TypeSymbol getReturnType() {
		return returnType;
	}

	public void setReturnType(TypeSymbol returnType) {
		this.returnType = returnType;
	}

	public ConstantSymbol getThisConstant() {
		return thisConstant;
	}

	public List<ParameterSymbol> getParameters() {
		return parameters;
	}

	public List<LocalSymbol> getLocals() {
		return locals;
	}

	@Override
	public List<Symbol> allDeclarations() {
		var declarations = new ArrayList<Symbol>();
		if (thisConstant != null) {
			declarations.add(thisConstant);
		}
		declarations.addAll(parameters);
		declarations.addAll(locals);
		return declarations;
	}
}
