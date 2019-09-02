package checker;

import java.util.List;
import java.util.Objects;

import checker.symbols.ClassSymbol;
import checker.symbols.GlobalScope;
import checker.symbols.MethodSymbol;
import checker.symbols.ParameterSymbol;
import checker.symbols.Symbol;
import checker.symbols.SymbolTable;
import checker.symbols.TypeSymbol;
import common.Diagnostics;
import common.Location;
import parser.tree.ArrayCreationNode;
import parser.tree.AssignmentNode;
import parser.tree.BasicDesignatorNode;
import parser.tree.BinaryExpressionNode;
import parser.tree.CallStatementNode;
import parser.tree.DesignatorNode;
import parser.tree.ElementAccessNode;
import parser.tree.ExpressionNode;
import parser.tree.IfStatementNode;
import parser.tree.IntegerLiteralNode;
import parser.tree.MemberAccessNode;
import parser.tree.MethodCallNode;
import parser.tree.ObjectCreationNode;
import parser.tree.ReturnStatementNode;
import parser.tree.StringLiteralNode;
import parser.tree.TypeCastNode;
import parser.tree.UnaryExpressionNode;
import parser.tree.Visitor;
import parser.tree.WhileStatementNode;

public class TypeCheckVisitor implements Visitor {
	private final SymbolTable symbolTable;
	private final MethodSymbol method;
	private final Diagnostics diagnostics;
	private final TypeSymbol booleanType;
	private final TypeSymbol intType;

	public TypeCheckVisitor(SymbolTable symbolTable, MethodSymbol method, Diagnostics diagnostics) {
		Objects.requireNonNull(symbolTable);
		Objects.requireNonNull(method);
		Objects.requireNonNull(diagnostics);
		this.symbolTable = symbolTable;
		this.method = method;
		this.diagnostics = diagnostics;
		var globalScope = symbolTable.getGlobalScope();
		booleanType = globalScope.getBooleanType();
		intType = globalScope.getIntType();
	}

	@Override
	public void visit(AssignmentNode node) {
		Visitor.super.visit(node);
		var leftType = symbolTable.findType(node.getLeft());
		var rightType = symbolTable.findType(node.getRight());
		checkType(node.getLocation(), rightType, leftType);
		var target = symbolTable.getTarget(node.getLeft());
		if (target == symbolTable.getGlobalScope().getArrayLength()) {
			error(node.getLocation(), "Array length is read-only");
		}
		if (target.getIdentifier().equals(GlobalScope.THIS_CONSTANT_NAME)) {
			error(node.getLocation(), "this is read-only");
		}
	}

	@Override
	public void visit(WhileStatementNode node) {
		Visitor.super.visit(node);
		var type = symbolTable.findType(node.getCondition());
		checkType(node.getCondition().getLocation(), type, booleanType);
	}

	@Override
	public void visit(IfStatementNode node) {
		Visitor.super.visit(node);
		var type = symbolTable.findType(node.getCondition());
		checkType(node.getCondition().getLocation(), type, booleanType);
	}

	@Override
	public void visit(ReturnStatementNode node) {
		Visitor.super.visit(node);
		var returnType = method.getReturnType();
		var expression = node.getExpression();
		if (returnType == null) {
			if (expression != null) {
				error(node.getLocation(), "No return expression in void methods");
			}
		} else {
			if (expression == null) {
				error(node.getLocation(), "Return statement must have an expression");
			} else {
				var type = symbolTable.findType(expression);
				checkType(expression.getLocation(), type, returnType);
			}
		}
	}

	@Override
	public void visit(CallStatementNode node) {
		Visitor.super.visit(node);
		var type = symbolTable.findType(node.getCall());
		if (type != null) {
			error(node.getLocation(), "Can only call a void method as a statement");
		}
	}

	@Override
	public void visit(BinaryExpressionNode node) {
		Visitor.super.visit(node);
		var left = node.getLeft();
		var right = node.getRight();
		var leftType = symbolTable.findType(left);
		var rightType = symbolTable.findType(right);
		var leftLocation = left.getLocation();
		var rightLocation = right.getLocation();
		switch (node.getOperator()) {
		case AND:
		case OR:
			checkType(leftLocation, leftType, booleanType);
			checkType(rightLocation, rightType, booleanType);
			symbolTable.fixType(node, booleanType);
			break;
		case EQUAL:
		case UNEQUAL:
			checkEqualComparison(node.getLocation(), leftType, rightType);
			symbolTable.fixType(node, booleanType);
			break;
		case LESS:
		case LESS_EQUAL:
		case GREATER:
		case GREATER_EQUAL:
			checkType(leftLocation, leftType, intType);
			checkType(rightLocation, rightType, intType);
			symbolTable.fixType(node, booleanType);
			break;
		case INSTANCEOF:
			checkInstanceOf(node.getLocation(), leftType, right);
			symbolTable.fixType(node, booleanType);
			break;
		case DIVIDE:
		case MINUS:
		case MODULO:
		case PLUS:
		case TIMES:
			checkType(leftLocation, leftType, intType);
			checkType(rightLocation, rightType, intType);
			symbolTable.fixType(node, intType);
			break;
		default:
			throw new AssertionError("Unsupported operator");
		}
	}

	@Override
	public void visit(UnaryExpressionNode node) {
		Visitor.super.visit(node);
		var operand = node.getOperand();
		var location = operand.getLocation();
		var type = symbolTable.findType(operand);
		switch (node.getOperator()) {
		case MINUS:
		case PLUS:
			checkType(location, type, intType);
			symbolTable.fixType(node, intType);
			break;
		case NOT:
			checkType(location, type, booleanType);
			symbolTable.fixType(node, booleanType);
			break;
		default:
			throw new AssertionError("Unsupported operator");
		}
	}

	@Override
	public void visit(TypeCastNode node) {
		Visitor.super.visit(node);
		var sourceType = symbolTable.findType(node.getDesignator());
		var targetType = symbolTable.findType(node.getType());
		checkTypeCast(node.getLocation(), sourceType, targetType);
		symbolTable.fixType(node, targetType);
	}

	@Override
	public void visit(ObjectCreationNode node) {
		Visitor.super.visit(node);
		var type = symbolTable.findType(node.getType());
		if (!(type instanceof ClassSymbol)) {
			error(node.getType().getLocation(), "No class type " + type + " in object creation");
		}
		symbolTable.fixType(node, type);
	}

	@Override
	public void visit(ArrayCreationNode node) {
		Visitor.super.visit(node);
		var elementType = symbolTable.findType(node.getElementType());
		TypeSymbol arrayType = null;
		if (elementType == null) {
			error(node.getElementType().getLocation(), "Undeclared element type");
		} else {
			arrayType = symbolTable.findType(elementType.getIdentifier() + "[]");
		}
		symbolTable.fixType(node, arrayType);
	}

	@Override
	public void visit(MethodCallNode node) {
		Visitor.super.visit(node);
		var target = symbolTable.getTarget(node.getDesignator());
		TypeSymbol returnType = null;
		if (target instanceof MethodSymbol) {
			var callee = (MethodSymbol) target;
			checkParameterPassing(node.getLocation(), node.getArguments(), callee.getParameters());
			returnType = callee.getReturnType();
		} else {
			error(node.getDesignator().getLocation(), "Designator does not refer to a method");
		}
		symbolTable.fixType(node, returnType);
	}

	@Override
	public void visit(BasicDesignatorNode node) {
		var type = symbolTable.getTargetType(node);
		symbolTable.fixType(node, type);
	}

	@Override
	public void visit(ElementAccessNode node) {
		Visitor.super.visit(node);
		var type = symbolTable.getTargetType(node);
		symbolTable.fixType(node, type);
		var indexType = symbolTable.findType(node.getExpression());
		checkType(node.getExpression().getLocation(), indexType, intType);
	}

	@Override
	public void visit(MemberAccessNode node) {
		Visitor.super.visit(node);
		var type = symbolTable.getTargetType(node);
		symbolTable.fixType(node, type);
	}

	@Override
	public void visit(IntegerLiteralNode node) {
		symbolTable.fixType(node, intType);
	}

	@Override
	public void visit(StringLiteralNode node) {
		symbolTable.fixType(node, symbolTable.getGlobalScope().getStringType());
	}

	private void checkParameterPassing(Location location, List<ExpressionNode> arguments,
			List<ParameterSymbol> parameters) {
		if (arguments.size() != parameters.size()) {
			error(location, "Number of arguments differs to the number of parameters");
		} else {
			for (int index = 0; index < arguments.size(); index++) {
				var argument = arguments.get(index);
				var argumentType = symbolTable.findType(argument);
				checkType(argument.getLocation(), argumentType, parameters.get(index).getType());
			}
		}
	}

	private void checkEqualComparison(Location location, TypeSymbol leftType, TypeSymbol rightType) {
		if (leftType == null || rightType == null
				|| !leftType.isCompatibleTo(rightType) && !rightType.isCompatibleTo(leftType)) {
			error(location, "Operand types " + leftType + " " + rightType + " cannot be compared");
		}
	}

	private void checkInstanceOf(Location location, TypeSymbol leftType, ExpressionNode right) {
		if (right instanceof DesignatorNode) {
			var target = symbolTable.getTarget((DesignatorNode) right);
			checkTypeCast(location, leftType, target);
		} else {
			error(location, "invalid operand on type test");
		}
	}

	private void checkTypeCast(Location location, TypeSymbol sourceType, Symbol target) {
		if (target instanceof ClassSymbol) {
			var targetType = (ClassSymbol) target;
			if (!sourceType.isCompatibleTo(targetType) && !targetType.isCompatibleTo(sourceType)) {
				error(location, "Types are not related by inheritance");
			}
		} else {
			error(location, "target does not denote a class");
		}
	}

	private void checkType(Location location, TypeSymbol source, TypeSymbol target) {
		if (source == null || !source.isCompatibleTo(target)) {
			error(location, "Operand type " + source + " is incompatible to " + target);
		}
	}

	private void error(Location location, String message) {
		diagnostics.reportError(message + " LOCATION " + location);
	}
}
