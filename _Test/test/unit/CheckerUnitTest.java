package unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import checker.Checker;
import checker.symbols.ClassSymbol;
import checker.symbols.MethodSymbol;
import checker.symbols.SymbolTable;
import checker.symbols.TypeSymbol;
import common.Diagnostics;
import lexer.Lexer;
import parser.Parser;
import parser.tree.AssignmentNode;
import parser.tree.BinaryExpressionNode;
import parser.tree.CallStatementNode;
import parser.tree.ExpressionNode;
import parser.tree.IfStatementNode;
import parser.tree.MethodNode;
import parser.tree.ReturnStatementNode;
import parser.tree.StatementNode;
import parser.tree.UnaryExpressionNode;
import parser.tree.WhileStatementNode;

public class CheckerUnitTest {
	@Test
	public void testAssignments() {
		var source = "class A { void main() { int x; x = 3; boolean y; y = true; string z; z = \"Hello\"; }}";
		var table = loadSymbolTable(source);
		var global = table.getGlobalScope();
		var main = global.getMainMethod();

		var variableX = table.find(main, "x");
		var assignment1 = (AssignmentNode) getStatement(table, main, 1);
		assertSame(variableX, table.getTarget(assignment1.getLeft()));
		checkAssignment(table, global.getIntType(), assignment1);

		var variableY = table.find(main, "y");
		var booleanType = global.getBooleanType();
		var assignment2 = (AssignmentNode) getStatement(table, main, 3);
		assertSame(variableY, table.getTarget(assignment2.getLeft()));
		checkAssignment(table, booleanType, assignment2);

		var variableZ = table.find(main, "z");
		var stringType = global.getStringType();
		var assignment3 = (AssignmentNode) getStatement(table, main, 5);
		assertSame(variableZ, table.getTarget(assignment3.getLeft()));
		checkAssignment(table, stringType, assignment3);
	}

	@Test
	public void testIntExpressions() {
		var source = "class A { void main() { int x; x = 1; x = -(x + 1) * (x / 2 - 2); }}";
		var table = loadSymbolTable(source);
		var global = table.getGlobalScope();
		var main = global.getMainMethod();

		var variableX = table.find(main, "x");
		var assignment = (AssignmentNode) getStatement(table, main, 2);
		assertSame(variableX, table.getTarget(assignment.getLeft()));
		checkAssignment(table, global.getIntType(), assignment);
	}

	@Test
	public void testBooleanExpressions() {
		var source = "class A { void main() { boolean x; x = !(1 == 0 && (x != x) || false || 1 < 2); }}";
		var table = loadSymbolTable(source);
		var global = table.getGlobalScope();
		var main = global.getMainMethod();

		var variableX = table.find(main, "x");
		var booleanType = global.getBooleanType();
		var assignment = (AssignmentNode) getStatement(table, main, 1);
		assertSame(variableX, table.getTarget(assignment.getLeft()));
		checkAssignment(table, booleanType, assignment);
	}

	@Test
	public void testMethodCalls() {
		var source = "class A { void main() { test(12 + 1, true, \"Hello\"); } void test(int x, boolean y, string z) { } }";
		var table = loadSymbolTable(source);
		var global = table.getGlobalScope();
		var main = global.getMainMethod();

		var call = (CallStatementNode) getStatement(table, main, 0);
		var designator = call.getCall().getDesignator();
		assertSame(getMethod(table, "test"), table.getTarget(designator));
		var arguments = call.getCall().getArguments();
		assertEquals(3, arguments.size());
		checkType(table, global.getIntType(), arguments.get(0));
		checkType(table, global.getBooleanType(), arguments.get(1));
		checkType(table, global.getStringType(), arguments.get(2));
	}

	@Test
	public void testIfStatement() {
		var source = "class A { void main() { int x; if(1 != 0) { x = 1; } else { x = 0; } } }";
		var table = loadSymbolTable(source);
		var global = table.getGlobalScope();
		var main = global.getMainMethod();

		var ifStatement = (IfStatementNode) getStatement(table, main, 1);
		checkType(table, global.getBooleanType(), ifStatement.getCondition());
		var thenStatement = (AssignmentNode) ifStatement.getThenBlock().getStatements().get(0);
		checkAssignment(table, global.getIntType(), thenStatement);
		var elseStatement = (AssignmentNode) ifStatement.getElseBlock().getStatements().get(0);
		checkAssignment(table, global.getIntType(), elseStatement);
	}

	@Test
	public void testWhileStatement() {
		var source = "class A { void main() { int x; while(x < 10) { x = x + 1; } } }";
		var table = loadSymbolTable(source);
		var global = table.getGlobalScope();

		var main = global.getMainMethod();
		var whileStatement = (WhileStatementNode) getStatement(table, main, 1);
		checkType(table, global.getBooleanType(), whileStatement.getCondition());
		var innerStatement = (AssignmentNode) whileStatement.getBody().getStatements().get(0);
		checkAssignment(table, global.getIntType(), innerStatement);
	}

	@Test
	public void testReturnStatement() {
		var source = "class A { void main() { } boolean test() { return 1 < 10; } }";
		var table = loadSymbolTable(source);
		var global = table.getGlobalScope();

		var test = getMethod(table, "test");
		var returnStatement = (ReturnStatementNode) getStatement(table, test, 0);
		checkType(table, global.getBooleanType(), returnStatement.getExpression());
	}

	@Test
	public void testArray() {
		var source = "class A { void main() { int[] a; a = new int[1 + 2]; a[1] = a[0] * a[a.length - 1]; } }";
		var table = loadSymbolTable(source);
		var global = table.getGlobalScope();
		var main = global.getMainMethod();

		var assignment1 = (AssignmentNode) getStatement(table, main, 1);
		checkAssignment(table, table.findType("int[]"), assignment1);

		var assignment2 = (AssignmentNode) getStatement(table, main, 2);
		checkAssignment(table, global.getIntType(), assignment2);
	}

	@Test
	public void testObject() {
		var source = "class A { int f; void main() { A a; a = new A(); a.f = f % 2; f = a.f + 1; } }";
		var table = loadSymbolTable(source);
		var global = table.getGlobalScope();
		var main = global.getMainMethod();

		var assignment1 = (AssignmentNode) getStatement(table, main, 1);
		checkAssignment(table, (ClassSymbol) global.getMainMethod().getScope(), assignment1);

		var assignment2 = (AssignmentNode) getStatement(table, main, 2);
		checkAssignment(table, global.getIntType(), assignment2);

		var assignment3 = (AssignmentNode) getStatement(table, main, 3);
		checkAssignment(table, global.getIntType(), assignment3);
	}
	
	@Test
	public void testAssignMismatch() {
		assertSemanticError("class A { void main() { int x; x = 1 > 2; } }");
	}
	
	@Test
	public void testOperatorMismatch() {
		assertSemanticError("class A { void main() { int x; x = x && x; } }");
		assertSemanticError("class A { void main() { int x; x = !x; } }");
		assertSemanticError("class A { void main() { boolean x; int y; y = 1 + x; } }");
	}
	
	@Test
	public void testConditionMismatch() {
		assertSemanticError("class A { void main() { if (1) {} } }");
		assertSemanticError("class A { void main() { while (\"Hello\") {} } }");		
	}
		
	@Test
	public void testReturnMismatch() {
		assertSemanticError("class A { void main() {  } int test() { return 0 == 0; } }");
		assertSemanticError("class A { void main() { boolean b; b = test(); } int test() { return 0; } }");
	}
	

	@Test
	public void testParameterMismatch() {
		assertSemanticError("class A { void main() { test(1); } void test() { } }");
		assertSemanticError("class A { void main() { test(1); } void test(boolean b) { } }");
		assertSemanticError("class A { void main() { test(true); } void test(boolean b, int c) { } }");
		assertSemanticError("class A { void main() { test(true, 0); } void test(boolean b, string c) { } }");
	}

	private MethodSymbol getMethod(SymbolTable table, String methodName) {
		return (MethodSymbol) table.find(table.getGlobalScope().getMainMethod(), methodName);
	}

	private StatementNode getStatement(SymbolTable table, MethodSymbol method, int statementIndex) {
		var methodNode = (MethodNode) table.getDeclarationNode(method);
		return methodNode.getBody().getStatements().get(statementIndex);
	}

	private void checkAssignment(SymbolTable table, TypeSymbol type, AssignmentNode assignment) {
		checkType(table, type, assignment.getLeft());
		checkType(table, type, assignment.getRight());
	}

	private void checkType(SymbolTable table, TypeSymbol type, ExpressionNode expression) {
		assertSame(type, table.findType(expression));
		if (expression instanceof BinaryExpressionNode) {
			checkType(table, (BinaryExpressionNode) expression);
		} else if (expression instanceof UnaryExpressionNode) {
			checkType(table, (UnaryExpressionNode) expression);
		}
	}

	private void checkType(SymbolTable table, UnaryExpressionNode unary) {
		var booleanType = table.getGlobalScope().getBooleanType();
		var intType = table.getGlobalScope().getIntType();
		ExpressionNode operand = unary.getOperand();
		switch (unary.getOperator()) {
		case PLUS:
		case MINUS:
			checkType(table, intType, operand);
			break;
		case NOT:
			checkType(table, booleanType, operand);
			break;
		default:
			fail("not supported");
		}
	}

	private void checkType(SymbolTable table, BinaryExpressionNode binary) {
		var booleanType = table.getGlobalScope().getBooleanType();
		var intType = table.getGlobalScope().getIntType();
		var left = binary.getLeft();
		var right = binary.getRight();
		switch (binary.getOperator()) {
		case AND:
		case OR:
			checkType(table, booleanType, left);
			checkType(table, booleanType, right);
			break;
		case PLUS:
		case MINUS:
		case TIMES:
		case DIVIDE:
		case MODULO:
		case LESS:
		case LESS_EQUAL:
		case GREATER:
		case GREATER_EQUAL:
			checkType(table, intType, left);
			checkType(table, intType, right);
			break;
		case EQUAL:
		case UNEQUAL:
			var compareType = table.findType(left);
			checkType(table, compareType, left);
			checkType(table, compareType, right);
			break;
		default:
			fail("not supported");
		}
	}

	private SymbolTable loadSymbolTable(String source) {
		var diagnostics = new Diagnostics();
		var checker = getChecker(source, diagnostics);
		assertFalse(diagnostics.hasErrors());
		return checker.getSymbolTable();
	}
	
	private void assertSemanticError(String source) {
		var diagnostics = new Diagnostics();
		getChecker(source, diagnostics);
		assertTrue(diagnostics.hasErrors());
	}
	
	private Checker getChecker(String source, Diagnostics diagnostics) {
		var lexer = new Lexer(new StringReader(source), diagnostics);
		var parser = new Parser(lexer, diagnostics);
		var tree = parser.parseProgram();
		return new Checker(tree, diagnostics);
	}
}
