package unit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import org.junit.jupiter.api.Test;

import common.Diagnostics;
import lexer.Lexer;
import parser.Parser;
import parser.tree.ArrayCreationNode;
import parser.tree.ArrayTypeNode;
import parser.tree.AssignmentNode;
import parser.tree.BasicDesignatorNode;
import parser.tree.BasicTypeNode;
import parser.tree.BinaryExpressionNode;
import parser.tree.CallStatementNode;
import parser.tree.ElementAccessNode;
import parser.tree.ExpressionNode;
import parser.tree.IfStatementNode;
import parser.tree.IntegerLiteralNode;
import parser.tree.LocalDeclarationNode;
import parser.tree.MemberAccessNode;
import parser.tree.MethodCallNode;
import parser.tree.Operator;
import parser.tree.ProgramNode;
import parser.tree.ReturnStatementNode;
import parser.tree.StringLiteralNode;
import parser.tree.TypeNode;
import parser.tree.UnaryExpressionNode;
import parser.tree.WhileStatementNode;

public class ParserUnitTest {
	@Test
	public void twoClasses() {
		var source = "class A { } class B {}";
		var tree = parse(source);

		var classes = tree.getClasses();
		assertEquals(2, classes.size());

		var firstClass = classes.get(0);
		assertEquals("A", firstClass.getIdentifier());
		assertNull(firstClass.getBaseClass());
		assertTrue(firstClass.getVariables().isEmpty());

		var secondClass = classes.get(1);
		assertEquals("B", secondClass.getIdentifier());
		assertNull(secondClass.getBaseClass());
		assertTrue(secondClass.getVariables().isEmpty());
		assertTrue(secondClass.getMethods().isEmpty());
	}

	@Test
	public void twoFields() {
		var source = "class A { int[] a; boolean b; }";
		var tree = parse(source);

		var fields = tree.getClasses().get(0).getVariables();
		assertEquals(2, fields.size());

		var firstField = fields.get(0);
		assertEquals("a", firstField.getIdentifier());
		var firstType = firstField.getType();
		assertArrayType(firstType, "int");

		var secondField = fields.get(1);
		assertEquals("b", secondField.getIdentifier());
		assertBasicType(secondField.getType(), "boolean");
	}

	@Test
	public void twoMethods() {
		var source = "class A { void main() { ;; } int do(A x, string y) {} }";
		var tree = parse(source);

		var methods = tree.getClasses().get(0).getMethods();
		assertEquals(2, methods.size());

		var mainMethod = methods.get(0);
		assertEquals("main", mainMethod.getIdentifier());
		assertTrue(mainMethod.getParameters().isEmpty());
		assertBasicType(mainMethod.getReturnType(), "void");

		var doMethod = methods.get(1);
		assertEquals("do", doMethod.getIdentifier());
		assertBasicType(doMethod.getReturnType(), "int");
		var parameters = doMethod.getParameters();
		assertEquals(2, parameters.size());

		var firstParam = parameters.get(0);
		assertEquals("x", firstParam.getIdentifier());
		assertBasicType(firstParam.getType(), "A");

		var secondParam = parameters.get(1);
		assertEquals("y", secondParam.getIdentifier());
		assertBasicType(secondParam.getType(), "string");
	}

	@Test
	public void localAssignments() {
		var source = "class A { void main() { int[] a; string s; a = new int[100]; s = \"Hello\"; } }";
		var tree = parse(source);

		var statements = tree.getClasses().get(0).getMethods().get(0).getBody().getStatements();
		assertEquals(4, statements.size());

		var firstLocal = (LocalDeclarationNode) statements.get(0);
		assertEquals("a", firstLocal.getVariable().getIdentifier());
		assertArrayType(firstLocal.getVariable().getType(), "int");

		var secondLocal = (LocalDeclarationNode) statements.get(1);
		assertEquals("s", secondLocal.getVariable().getIdentifier());
		assertBasicType(secondLocal.getVariable().getType(), "string");

		var firstAssign = (AssignmentNode) statements.get(2);
		assertBasicDesignator(firstAssign.getLeft(), "a");
		var firstRight = (ArrayCreationNode) firstAssign.getRight();
		assertBasicType(firstRight.getElementType(), "int");
		assertIntLiteral(firstRight.getExpression(), 100);

		var secondAssign = (AssignmentNode) statements.get(3);
		assertBasicDesignator(secondAssign.getLeft(), "s");
		assertStringLiteral(secondAssign.getRight(), "Hello");
	}

	@Test
	public void complexExpression() {
		var source = "class A { void main() { x = 1 + 2 * 3 <= 4 - 5 - 6 && !b || (c == -d); } }";
		var tree = parse(source);

		var statement = (AssignmentNode) tree.getClasses().get(0).getMethods().get(0).getBody().getStatements().get(0);
		var orExpression = (BinaryExpressionNode) statement.getRight();
		assertEquals(Operator.OR, orExpression.getOperator());

		var andExpression = (BinaryExpressionNode) orExpression.getLeft();
		assertEquals(Operator.AND, andExpression.getOperator());

		var lessEqual = (BinaryExpressionNode) andExpression.getLeft();
		assertEquals(Operator.LESS_EQUAL, lessEqual.getOperator());

		var oneTwoThree = (BinaryExpressionNode) lessEqual.getLeft();
		assertEquals(Operator.PLUS, oneTwoThree.getOperator());
		assertIntLiteral(oneTwoThree.getLeft(), 1);

		var twoThree = (BinaryExpressionNode) oneTwoThree.getRight();
		assertEquals(Operator.TIMES, twoThree.getOperator());
		assertIntLiteral(twoThree.getLeft(), 2);
		assertIntLiteral(twoThree.getRight(), 3);

		var fourFiveSix = (BinaryExpressionNode) lessEqual.getRight();
		assertEquals(Operator.MINUS, fourFiveSix.getOperator());
		var fourFive = (BinaryExpressionNode) fourFiveSix.getLeft();
		assertEquals(Operator.MINUS, fourFive.getOperator());
		assertIntLiteral(fourFive.getLeft(), 4);
		assertIntLiteral(fourFive.getRight(), 5);
		assertIntLiteral(fourFiveSix.getRight(), 6);

		var notExpression = (UnaryExpressionNode) andExpression.getRight();
		assertEquals(Operator.NOT, notExpression.getOperator());
		assertBasicDesignator(notExpression.getOperand(), "b");

		var equalsExpression = (BinaryExpressionNode) orExpression.getRight();
		assertEquals(Operator.EQUAL, equalsExpression.getOperator());
		assertBasicDesignator(equalsExpression.getLeft(), "c");
		var negate = (UnaryExpressionNode) equalsExpression.getRight();
		assertEquals(Operator.MINUS, negate.getOperator());
		assertBasicDesignator(negate.getOperand(), "d");
	}

	@Test
	public void nestedIfStatements() {
		var source = "class A { void main() { if (b) { if (c) { x = 1; y = 2; } else { z = 3; } } } }";
		var tree = parse(source);
		var outerIf = (IfStatementNode) tree.getClasses().get(0).getMethods().get(0).getBody().getStatements().get(0);
		assertNull(outerIf.getElseBlock());
		assertBasicDesignator(outerIf.getCondition(), "b");
		var then = outerIf.getThenBlock().getStatements();
		assertEquals(1, then.size());

		var innerIf = (IfStatementNode) then.get(0);
		assertBasicDesignator(innerIf.getCondition(), "c");
		assertEquals(2, innerIf.getThenBlock().getStatements().size());
		assertNotNull(innerIf.getElseBlock());
		assertEquals(1, innerIf.getElseBlock().getStatements().size());
	}

	@Test
	public void whileStatements() {
		var source = "class A { void main() { while (b) { while (c) { x = 1; y = 2; } } } }";
		var tree = parse(source);

		var outerWhile = (WhileStatementNode) tree.getClasses().get(0).getMethods().get(0).getBody().getStatements()
				.get(0);
		assertBasicDesignator(outerWhile.getCondition(), "b");

		var innerWhile = (WhileStatementNode) outerWhile.getBody().getStatements().get(0);
		assertBasicDesignator(innerWhile.getCondition(), "c");
		assertEquals(2, innerWhile.getBody().getStatements().size());
	}

	@Test
	public void methodCalls() {
		var source = "class A { void main() { call(1, 2, 3); this.x = func(5 % 4); } }";
		var tree = parse(source);

		var statements = tree.getClasses().get(0).getMethods().get(0).getBody().getStatements();
		assertEquals(2, statements.size());

		var firstCall = ((CallStatementNode) statements.get(0)).getCall();
		assertBasicDesignator(firstCall.getDesignator(), "call");
		var firstArgs = firstCall.getArguments();
		assertEquals(3, firstArgs.size());
		assertIntLiteral(firstArgs.get(0), 1);
		assertIntLiteral(firstArgs.get(1), 2);
		assertIntLiteral(firstArgs.get(2), 3);

		var secondAssign = (AssignmentNode) statements.get(1);
		var assignLeft = (MemberAccessNode) secondAssign.getLeft();
		assertEquals("x", assignLeft.getIdentifier());
		assertBasicDesignator(assignLeft.getDesignator(), "this");
		var secondCall = (MethodCallNode) secondAssign.getRight();
		var secondArgs = secondCall.getArguments();
		assertEquals(1, secondArgs.size());
		var expression = (BinaryExpressionNode) secondArgs.get(0);
		assertEquals(Operator.MODULO, expression.getOperator());
		assertIntLiteral(expression.getLeft(), 5);
		assertIntLiteral(expression.getRight(), 4);
	}

	@Test
	public void methodReturns() {
		var source = "class A { void test() { return; return 2 * 3; } }";
		var tree = parse(source);

		var statements = tree.getClasses().get(0).getMethods().get(0).getBody().getStatements();
		assertEquals(2, statements.size());

		var firstReturn = (ReturnStatementNode) statements.get(0);
		assertNull(firstReturn.getExpression());

		var secondReturn = (ReturnStatementNode) statements.get(1);
		assertNotNull(secondReturn.getExpression());
		var expression = (BinaryExpressionNode) secondReturn.getExpression();
		assertEquals(Operator.TIMES, expression.getOperator());
		assertIntLiteral(expression.getLeft(), 2);
		assertIntLiteral(expression.getRight(), 3);
	}

	@Test
	public void complexDesignators() {
		var source = "class A { void test() { a[1 + b.f].g[0].h = x.y[2].z(); } }";
		var tree = parse(source);

		var assignment = (AssignmentNode) tree.getClasses().get(0).getMethods().get(0).getBody().getStatements().get(0);

		var left1 = (MemberAccessNode) assignment.getLeft();
		assertEquals("h", left1.getIdentifier());
		var left2 = (ElementAccessNode) left1.getDesignator();
		assertIntLiteral(left2.getExpression(), 0);
		var left3 = (MemberAccessNode) left2.getDesignator();
		assertEquals("g", left3.getIdentifier());
		var left4 = (ElementAccessNode) left3.getDesignator();
		assertBasicDesignator(left4.getDesignator(), "a");
		var expression = (BinaryExpressionNode) left4.getExpression();
		assertIntLiteral(expression.getLeft(), 1);
		var innerRight = (MemberAccessNode) expression.getRight();
		assertEquals("f", innerRight.getIdentifier());
		assertBasicDesignator(innerRight.getDesignator(), "b");

		var call = (MethodCallNode) assignment.getRight();
		assertEquals(0, call.getArguments().size());
		var right1 = (MemberAccessNode) call.getDesignator();
		assertEquals("z", right1.getIdentifier());
		var right2 = (ElementAccessNode) right1.getDesignator();
		assertIntLiteral(right2.getExpression(), 2);
		var right3 = (MemberAccessNode) right2.getDesignator();
		assertEquals("y", right3.getIdentifier());
		assertBasicDesignator(right3.getDesignator(), "x");
	}

	private void assertIntLiteral(ExpressionNode node, int value) {
		assertTrue(node instanceof IntegerLiteralNode);
		assertEquals(value, ((IntegerLiteralNode) node).getValue());
	}

	private void assertStringLiteral(ExpressionNode node, String value) {
		assertTrue(node instanceof StringLiteralNode);
		assertEquals(value, ((StringLiteralNode) node).getValue());
	}

	private void assertBasicDesignator(ExpressionNode node, String identifier) {
		assertTrue(node instanceof BasicDesignatorNode);
		assertEquals(identifier, ((BasicDesignatorNode) node).getIdentifier());
	}

	private ProgramNode parse(String source) {
		var diagnostics = new Diagnostics();
		var lexer = new Lexer(new StringReader(source), diagnostics);
		var parser = new Parser(lexer, diagnostics);
		var tree = parser.parseProgram();
		assertFalse(diagnostics.hasErrors());
		return tree;
	}

	private void assertArrayType(TypeNode node, String identifier) {
		assertTrue(node instanceof ArrayTypeNode);
		assertBasicType(((ArrayTypeNode) node).getElementType(), identifier);
	}

	private void assertBasicType(TypeNode node, String identifier) {
		assertTrue(node instanceof BasicTypeNode);
		assertEquals(identifier, ((BasicTypeNode) node).getIdentifier());
	}
}
