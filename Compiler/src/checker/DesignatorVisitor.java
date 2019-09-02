package checker;

import java.util.Objects;

import checker.symbols.ArrayTypeSymbol;
import checker.symbols.ClassSymbol;
import checker.symbols.GlobalScope;
import checker.symbols.LocalSymbol;
import checker.symbols.MethodSymbol;
import checker.symbols.Symbol;
import checker.symbols.SymbolTable;
import common.Diagnostics;
import common.Location;
import parser.tree.AssignmentNode;
import parser.tree.BasicDesignatorNode;
import parser.tree.CallStatementNode;
import parser.tree.ElementAccessNode;
import parser.tree.IfStatementNode;
import parser.tree.LocalDeclarationNode;
import parser.tree.MemberAccessNode;
import parser.tree.ReturnStatementNode;
import parser.tree.StatementNode;
import parser.tree.Visitor;
import parser.tree.WhileStatementNode;

public class DesignatorVisitor implements Visitor {
	private final SymbolTable symbolTable;
	private final MethodSymbol method;
	private final Diagnostics diagnostics;
	private StatementNode statement;

	public DesignatorVisitor(SymbolTable symbolTable, MethodSymbol method, Diagnostics diagnostics) {
		Objects.requireNonNull(symbolTable);
		Objects.requireNonNull(method);
		Objects.requireNonNull(diagnostics);
		this.symbolTable = symbolTable;
		this.method = method;
		this.diagnostics = diagnostics;
	}

	@Override
	public void visit(BasicDesignatorNode node) {
		var symbol = symbolTable.find(method, node.getIdentifier());
		if (symbol == null) {
			error(node.getLocation(), "Designator " + node.getIdentifier() + " is not defined");
		} else if (symbol instanceof LocalSymbol) {
			var local = (LocalSymbol) symbol;
			if (!local.getVisibleIn().contains(statement)) {
				error(node.getLocation(), "Local variable " + symbol + " is not visible");
			}
		}
		symbolTable.fixTarget(node, symbol);
	}

	@Override
	public void visit(ElementAccessNode node) {
		Visitor.super.visit(node);
		var type = symbolTable.getTargetType(node.getDesignator());
		if (type instanceof ArrayTypeSymbol) {
			var elementType = ((ArrayTypeSymbol) type).getElementType();
			symbolTable.fixTarget(node, elementType);
		} else {
			error(node.getLocation(), "Designator " + node + " does not refer to an array type");
		}
	}

	@Override
	public void visit(MemberAccessNode node) {
		Visitor.super.visit(node);
		var type = symbolTable.getTargetType(node.getDesignator());
		Symbol target = null;
		if (type instanceof ClassSymbol) {
			var member = type.allDeclarations().stream()
					.filter(declaration -> declaration.getIdentifier().equals(node.getIdentifier())).findFirst()
					.orElse(null);
			if (member == null) {
				error(node.getLocation(), "Designator " + node + " refers to an inexistent member "
						+ node.getIdentifier() + " in class " + type);
			} else if (member.getIdentifier().equals(GlobalScope.THIS_CONSTANT_NAME)) {
				error(node.getLocation(), "Invalid member designator " + node);
			} else {
				target = member;
			}
		} else if (type instanceof ArrayTypeSymbol) {
			var arrayLength = symbolTable.getGlobalScope().getArrayLength();
			if (node.getIdentifier().equals(arrayLength.getIdentifier())) {
				target = arrayLength;
			} else {
				error(node.getLocation(), "Invalid member access " + node.getIdentifier() + " on array " + node);
			}
		} else {
			error(node.getLocation(), "Designator " + node + " does not refer to a class type");
		}
		symbolTable.fixTarget(node, target);
	}

	@Override
	public void visit(AssignmentNode node) {
		statement = node;
		Visitor.super.visit(node);
	}

	@Override
	public void visit(CallStatementNode node) {
		statement = node;
		Visitor.super.visit(node);
	}

	@Override
	public void visit(IfStatementNode node) {
		statement = node;
		Visitor.super.visit(node);
	}

	@Override
	public void visit(ReturnStatementNode node) {
		statement = node;
		Visitor.super.visit(node);
	}

	@Override
	public void visit(WhileStatementNode node) {
		statement = node;
		Visitor.super.visit(node);
	}

	@Override
	public void visit(LocalDeclarationNode node) {
		statement = node;
		Visitor.super.visit(node);
	}

	private void error(Location location, String message) {
		diagnostics.reportError(message + " LOCATION " + location);
	}
}
