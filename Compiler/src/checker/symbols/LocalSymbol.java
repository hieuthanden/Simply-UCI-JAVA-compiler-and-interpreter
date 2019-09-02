package checker.symbols;

import java.util.HashSet;
import java.util.Set;

import parser.tree.StatementNode;

public class LocalSymbol extends VariableSymbol {
	private final Set<StatementNode> visibleIn = new HashSet<>();

	public LocalSymbol(Symbol scope, String identifier) {
		super(scope, identifier);
	}

	public Set<StatementNode> getVisibleIn() {
		return visibleIn;
	}
}
