package checker.symbols;

import java.util.HashMap;
import java.util.Map;

import parser.tree.ArrayTypeNode;
import parser.tree.BasicTypeNode;
import parser.tree.DesignatorNode;
import parser.tree.ExpressionNode;
import parser.tree.Node;
import parser.tree.TypeNode;

public class SymbolTable {
	private final GlobalScope globalScope = new GlobalScope();

	private final Map<Symbol, Node> associatedNode = new HashMap<>();
	private final Map<DesignatorNode, Symbol> resolvedTargets = new HashMap<>();
	private final Map<ExpressionNode, TypeSymbol> resolvedTypes = new HashMap<>();
	
	public GlobalScope getGlobalScope() {
		return globalScope;
	}

	public TypeSymbol findType(String identifier) {
		return globalScope.getTypes().stream().filter(type -> type.getIdentifier().equals(identifier)).findFirst()
				.orElse(null);
	}

	public TypeSymbol findType(TypeNode node) {
		TypeSymbol result;
		if (node instanceof BasicTypeNode) {
			var identifier = ((BasicTypeNode) node).getIdentifier();
			result = findType(identifier);
		} else {
			var elementTypeNode = ((ArrayTypeNode) node).getElementType();
			var elementType = findType(elementTypeNode);
			if (elementType == null) {
				result = null;
			} else {
				result = findType(elementType.getIdentifier() + "[]");
				if (result == null) {
					result = new ArrayTypeSymbol(globalScope, elementType);
					globalScope.getTypes().add(result);
				}
			}
		}
		return result;
	}

	public Symbol find(Symbol scope, String identifier) {
		if (scope == null) {
			return null;
		}
		for (Symbol declaration : scope.allDeclarations()) {
			if (declaration.getIdentifier().equals(identifier)) {
				return declaration;
			}
		}
		return find(scope.getScope(), identifier);
	}

	public void linkDeclaration(Node node, Symbol symbol) {
		associatedNode.put(symbol, node);
	}

	public Node getDeclarationNode(Symbol symbol) {
		return associatedNode.get(symbol);
	}

	public void fixTarget(DesignatorNode node, Symbol symbol) {
		resolvedTargets.put(node, symbol);
	}

	public Symbol getTarget(DesignatorNode node) {
		return resolvedTargets.get(node);
	}
	
	public TypeSymbol getTargetType(DesignatorNode node) {
		var symbol = resolvedTargets.get(node);
		if (symbol instanceof TypeSymbol) {
			return (TypeSymbol)symbol;
		}
		if (symbol instanceof VariableSymbol) {
			return ((VariableSymbol) symbol).getType();
		}
		if (symbol instanceof ConstantSymbol) {
			return ((ConstantSymbol) symbol).getType();
		}
		return null;
	}

	public void fixType(ExpressionNode node, TypeSymbol symbol) {
		resolvedTypes.put(node, symbol);
	}

	public TypeSymbol findType(ExpressionNode node) {
		return resolvedTypes.get(node);
	}
	
	@Override
	public String toString() {
		return globalScope.toString();
	}
}
