package parser.tree;

public interface Visitor {
	default void visit(ArrayCreationNode node) {
		node.getElementType().accept(this);
		node.getExpression().accept(this);
	}

	default void visit(ArrayTypeNode node) {
		node.getElementType().accept(this);
	}

	default void visit(AssignmentNode node) {
		node.getLeft().accept(this);
		node.getRight().accept(this);
	}

	default void visit(BasicDesignatorNode node) {
	}

	default void visit(BasicTypeNode node) {
	}

	default void visit(BinaryExpressionNode node) {
		node.getLeft().accept(this);
		node.getRight().accept(this);
	}

	default void visit(CallStatementNode node) {
		node.getCall().accept(this);
	}

	default void visit(ClassNode node) {
		if (node.getBaseClass() != null) {
			node.getBaseClass().accept(this);
		}
		for (var variable : node.getVariables()) {
			variable.accept(this);
		}
		for (var method : node.getMethods()) {
			method.accept(this);
		}
	}

	default void visit(ElementAccessNode node) {
		node.getDesignator().accept(this);
		node.getExpression().accept(this);
	}

	default void visit(IfStatementNode node) {
		node.getCondition().accept(this);
		node.getThenBlock().accept(this);
		if (node.getElseBlock() != null) {
			node.getElseBlock().accept(this);
		}
	}

	default void visit(IntegerLiteralNode node) {
	}

	default void visit(LocalDeclarationNode node) {
		node.getVariable().accept(this);
	}

	default void visit(MemberAccessNode node) {
		node.getDesignator().accept(this);
	}

	default void visit(MethodCallNode node) {
		node.getDesignator().accept(this);
		for (var argument : node.getArguments()) {
			argument.accept(this);
		}
	}

	default void visit(MethodNode node) {
		node.getReturnType().accept(this);
		for (var parameter : node.getParameters()) {
			parameter.accept(this);
		}
		node.getBody().accept(this);
	}

	default void visit(ObjectCreationNode node) {
		node.getType().accept(this);
	}

	default void visit(ProgramNode node) {
		for (var classNode : node.getClasses()) {
			classNode.accept(this);
		}
	}

	default void visit(ReturnStatementNode node) {
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
		}
	}

	default void visit(StatementBlockNode node) {
		for (var statement : node.getStatements()) {
			statement.accept(this);
		}
	}

	default void visit(StringLiteralNode node) {
	}

	default void visit(TypeCastNode node) {
		node.getType().accept(this);
		node.getDesignator().accept(this);
	}

	default void visit(UnaryExpressionNode node) {
		node.getOperand().accept(this);
	}

	default void visit(VariableNode node) {
		node.getType().accept(this);
	}

	default void visit(WhileStatementNode node) {
		node.getCondition().accept(this);
		node.getBody().accept(this);
	}
}
