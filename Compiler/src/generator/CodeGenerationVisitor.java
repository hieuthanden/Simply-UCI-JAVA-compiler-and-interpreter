package generator;

import java.util.Objects;

import static bytecode.OpCode.*;

import checker.symbols.ConstantSymbol;
import checker.symbols.FieldSymbol;
import checker.symbols.LocalSymbol;
import checker.symbols.MethodSymbol;
import checker.symbols.ParameterSymbol;
import checker.symbols.Symbol;
import checker.symbols.SymbolTable;
import checker.symbols.TypeSymbol;
import parser.tree.ArrayCreationNode;
import parser.tree.AssignmentNode;
import parser.tree.BasicDesignatorNode;
import parser.tree.BinaryExpressionNode;
import parser.tree.ElementAccessNode;
import parser.tree.IfStatementNode;
import parser.tree.IntegerLiteralNode;
import parser.tree.LocalDeclarationNode;
import parser.tree.MemberAccessNode;
import parser.tree.MethodCallNode;
import parser.tree.ObjectCreationNode;
import parser.tree.ReturnStatementNode;
import parser.tree.StringLiteralNode;
import parser.tree.TypeCastNode;
import parser.tree.UnaryExpressionNode;
import parser.tree.Visitor;
import parser.tree.WhileStatementNode;

public class CodeGenerationVisitor implements Visitor {
	private final SymbolTable symbolTable;
	private final MethodSymbol method;
	private final Assembler assembler;
	private int loopDepth = 0;

	public CodeGenerationVisitor(SymbolTable symbolTable, MethodSymbol method, Assembler assembler) {
		Objects.requireNonNull(symbolTable);
		Objects.requireNonNull(method);
		Objects.requireNonNull(assembler);
		this.symbolTable = symbolTable;
		this.method = method;
		this.assembler = assembler;
	}

	@Override
	public void visit(LocalDeclarationNode node) {
		if (loopDepth > 0) {
			// re-initialize local variables inside a loop with their default value
			var local = (LocalSymbol) symbolTable.find(method, node.getVariable().getIdentifier());
			if (!method.getLocals().contains(local)) {
				throw new AssertionError("Invalid local");
			}
			loadDefaultValue(local.getType());
			assembler.emit(STORE, localIndex(local));
		}
	}


	@Override
	public void visit(AssignmentNode node) {
		var left = node.getLeft();
		var right = node.getRight();
		var target = symbolTable.getTarget(left);
		if (left instanceof BasicDesignatorNode) {
			if (target instanceof FieldSymbol) {
				loadThis();
			}
		} else if (left instanceof ElementAccessNode) {
			var elementAccess = (ElementAccessNode) left;
			elementAccess.getDesignator().accept(this);
			elementAccess.getExpression().accept(this);
		} else if (left instanceof MemberAccessNode) {
			var memberAccess = (MemberAccessNode) left;
			memberAccess.getDesignator().accept(this);
		} else {
			throw new AssertionError("Unsupport assignment left hand side");
		}
		right.accept(this);
		if (target instanceof FieldSymbol) {
			assembler.emit(PUTFIELD, target);
		} else if (left instanceof ElementAccessNode) {
			assembler.emit(ASTORE);
		} else if (target instanceof ParameterSymbol || target instanceof LocalSymbol) {
			assembler.emit(STORE, localIndex(target));
		} else {
			throw new AssertionError("Unsupported assignment");
		}
	}

	@Override
	public void visit(IfStatementNode node) {
		node.getCondition().accept(this);
		var thenBlock = node.getThenBlock();
		var elseBlock = node.getElseBlock();
		if (elseBlock == null) {
			var endLabel = assembler.createLabel();
			assembler.emit(IF_FALSE, endLabel);
			thenBlock.accept(this);
			assembler.setLabel(endLabel);
		} else {
			var elseLabel = assembler.createLabel();
			var endLabel = assembler.createLabel();
			assembler.emit(IF_FALSE, elseLabel);
			thenBlock.accept(this);
			assembler.emit(GOTO, endLabel);
			assembler.setLabel(elseLabel);
			elseBlock.accept(this);
			assembler.setLabel(endLabel);
		}
	}

	@Override
	public void visit(WhileStatementNode node) {
		var beginLabel = assembler.createLabel();
		var endLabel = assembler.createLabel();
		assembler.setLabel(beginLabel);
		node.getCondition().accept(this);
		assembler.emit(IF_FALSE, endLabel);
		loopDepth++;
		node.getBody().accept(this);
		loopDepth--;
		assembler.emit(GOTO, beginLabel);
		assembler.setLabel(endLabel);
	}

	@Override
	public void visit(ReturnStatementNode node) {
		Visitor.super.visit(node);
		assembler.emit(RETURN);
	}

	@Override
	public void visit(UnaryExpressionNode node) {
		Visitor.super.visit(node);
		switch (node.getOperator()) {
		case MINUS:
			assembler.emit(INEG);
			break;
		case NOT:
			assembler.emit(BNEG);
			break;
		case PLUS:
			break;
		default:
			throw new AssertionError("Unsupported operator");
		}
	}

	@Override
	public void visit(BinaryExpressionNode node) {
		switch (node.getOperator()) {
		case AND:
			logicalAnd(node);
			break;
		case OR:
			logicalOr(node);
			break;
		case INSTANCEOF:
			typeTest(node);
			break;
		default:
			ordinaryBinaryOperator(node);
			break;
		}
	}

	private void typeTest(BinaryExpressionNode node) {
		node.getLeft().accept(this);
		var type = symbolTable.findType(node.getRight());
		assembler.emit(INSTANCEOF, type);
	}

	private void logicalAnd(BinaryExpressionNode node) {
		var shortcutLabel = assembler.createLabel();
		var endLabel = assembler.createLabel();
		node.getLeft().accept(this);
		assembler.emit(IF_FALSE, shortcutLabel);
		node.getRight().accept(this);
		assembler.emit(GOTO, endLabel);
		assembler.setLabel(shortcutLabel);
		assembler.emit(LDC, false);
		assembler.setLabel(endLabel);
	}

	private void logicalOr(BinaryExpressionNode node) {
		var shortcutLabel = assembler.createLabel();
		var endLabel = assembler.createLabel();
		node.getLeft().accept(this);
		assembler.emit(IF_TRUE, shortcutLabel);
		node.getRight().accept(this);
		assembler.emit(GOTO, endLabel);
		assembler.setLabel(shortcutLabel);
		assembler.emit(LDC, true);
		assembler.setLabel(endLabel);
	}

	private void ordinaryBinaryOperator(BinaryExpressionNode node) {
		Visitor.super.visit(node);
		switch (node.getOperator()) {
		case PLUS:
			assembler.emit(IADD);
			break;
		case MINUS:
			assembler.emit(ISUB);
			break;
		case TIMES:
			assembler.emit(IMUL);
			break;
		case DIVIDE:
			assembler.emit(IDIV);
			break;
		case MODULO:
			assembler.emit(IREM);
			break;
		case GREATER:
			assembler.emit(ICMPGT);
			break;
		case GREATER_EQUAL:
			assembler.emit(ICMPGE);
			break;
		case LESS:
			assembler.emit(ICMPLT);
			break;
		case LESS_EQUAL:
			assembler.emit(ICMPLE);
			break;
		case EQUAL:
			assembler.emit(CMPEQ);
			break;
		case UNEQUAL:
			assembler.emit(CMPNE);
			break;
		default:
			throw new AssertionError("Unsupported operator");
		}
	}

	@Override
	public void visit(TypeCastNode node) {
		node.getDesignator().accept(this);
		var type = symbolTable.findType(node.getType());
		assembler.emit(CHECKCAST, type);
	}

	@Override
	public void visit(MethodCallNode node) {
		var target = (MethodSymbol) symbolTable.getTarget(node.getDesignator());
		if (target.getScope() == symbolTable.getGlobalScope()) {
			staticCall(node, target);
		} else {
			dynamicCall(node, target);
		}
	}

	private void staticCall(MethodCallNode node, MethodSymbol target) {
		for (var argument : node.getArguments()) {
			argument.accept(this);
		}
		assembler.emit(INVOKESTATIC, target);
	}

	private void dynamicCall(MethodCallNode node, MethodSymbol target) {
		if (node.getDesignator() instanceof BasicDesignatorNode) {
			loadThis();
		} else if (node.getDesignator() instanceof MemberAccessNode) {
			var left = ((MemberAccessNode) node.getDesignator()).getDesignator();
			left.accept(this);
		}
		for (var argument : node.getArguments()) {
			argument.accept(this);
		}
		assembler.emit(INVOKEVIRTUAL, target);
	}

	@Override
	public void visit(ArrayCreationNode node) {
		node.getExpression().accept(this);
		var type = symbolTable.findType(node);
		assembler.emit(NEWARRAY, type);
	}

	@Override
	public void visit(ObjectCreationNode node) {
		var type = symbolTable.findType(node);
		assembler.emit(NEW, type);
	}

	@Override
	public void visit(BasicDesignatorNode node) {
		var target = symbolTable.getTarget(node);
		if (target instanceof ConstantSymbol) {
			loadConstant((ConstantSymbol) target);
		} else if (target instanceof ParameterSymbol || target instanceof LocalSymbol) {
			assembler.emit(LOAD, localIndex(target));
		} else if (target instanceof FieldSymbol) {
			loadThis();
			assembler.emit(GETFIELD, target);
		} else {
			throw new AssertionError("Unsupported basic designator");
		}
	}

	@Override
	public void visit(MemberAccessNode node) {
		Visitor.super.visit(node);
		var target = symbolTable.getTarget(node);
		if (target == symbolTable.getGlobalScope().getArrayLength()) {
			assembler.emit(ARRAYLENGTH);
		} else {
			assembler.emit(GETFIELD, target);
		}
	}

	@Override
	public void visit(ElementAccessNode node) {
		Visitor.super.visit(node);
		assembler.emit(ALOAD);
	}

	@Override
	public void visit(IntegerLiteralNode node) {
		assembler.emit(LDC, node.getValue());
	}

	@Override
	public void visit(StringLiteralNode node) {
		assembler.emit(LDC, node.getValue());
	}

	private void loadConstant(ConstantSymbol constant) {
		var globalScope = symbolTable.getGlobalScope();
		if (constant == globalScope.getFalseConstant()) {
			assembler.emit(LDC, false);
		} else if (constant == globalScope.getTrueConstant()) {
			assembler.emit(LDC, true);
		} else if (constant == globalScope.getNullConstant()) {
			assembler.emit(ACONST_NULL);
		} else if (constant == method.getThisConstant()) {
			loadThis();
		} else {
			throw new AssertionError("Unsupported constant");
		}
	}

	private void loadThis() {
		assembler.emit(LOAD, localIndex(method.getThisConstant()));
	}

	private void loadDefaultValue(TypeSymbol type) {
		var globalScope = symbolTable.getGlobalScope();
		if (type == globalScope.getBooleanType()) {
			assembler.emit(LDC, false);
		} else if (type == globalScope.getIntType()) {
			assembler.emit(LDC, 0);
		} else {
			assembler.emit(ACONST_NULL);
		}
	}

	private int localIndex(Symbol symbol) {
		if (symbol == method.getThisConstant()) {
			return 0;
		} else if (symbol instanceof ParameterSymbol) {
			var index = method.getParameters().indexOf(symbol);
			if (index < 0) {
				throw new AssertionError("Invalid parameter");
			}
			return 1 + index;
		} else if (symbol instanceof LocalSymbol) {
			var index = method.getLocals().indexOf(symbol);
			if (index < 0) {
				throw new AssertionError("Invalid local");
			}
			return 1 + method.getParameters().size() + index;
		}
		throw new AssertionError("Unsupported load");
	}
}
