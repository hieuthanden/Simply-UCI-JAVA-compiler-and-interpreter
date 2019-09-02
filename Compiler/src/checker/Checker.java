package checker;

import java.util.Objects;
import checker.symbols.SymbolTable;
import common.Diagnostics;
import parser.tree.MethodNode;
import parser.tree.ProgramNode;

public class Checker {
	private final Diagnostics diagnostics;
	private final SymbolTable symbolTable;

	public Checker(ProgramNode syntaxTree, Diagnostics diagnostics) {
		Objects.requireNonNull(syntaxTree);
		Objects.requireNonNull(diagnostics);
		this.diagnostics = diagnostics;
		this.symbolTable = new TableConstructor(syntaxTree, diagnostics).getSymbolTable();
		if (diagnostics.hasErrors()) {
			return;
		}
		checkSyntaxTree();
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	private void checkSyntaxTree() {
		for (var classSymbol : symbolTable.getGlobalScope().getClasses()) {
			for (var method : classSymbol.getMethods()) {
				var methodNode = (MethodNode) symbolTable.getDeclarationNode(method);
				var body = methodNode.getBody();
				body.accept(new DesignatorVisitor(symbolTable, method, diagnostics));
				if (!diagnostics.hasErrors()) {
					body.accept(new TypeCheckVisitor(symbolTable, method, diagnostics));
				}
			}
		}
	}
}
