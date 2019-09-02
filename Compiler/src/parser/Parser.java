package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.Diagnostics;
import common.Location;
import lexer.Lexer;
import lexer.tokens.StaticToken;
import lexer.tokens.IdentifierToken;
import lexer.tokens.IntegerToken;
import lexer.tokens.StringToken;
import lexer.tokens.Tag;
import lexer.tokens.Token;
import parser.tree.ArrayCreationNode;
import parser.tree.ArrayTypeNode;
import parser.tree.AssignmentNode;
import parser.tree.BasicDesignatorNode;
import parser.tree.BasicTypeNode;
import parser.tree.BinaryExpressionNode;
import parser.tree.CallStatementNode;
import parser.tree.ClassNode;
import parser.tree.DesignatorNode;
import parser.tree.ElementAccessNode;
import parser.tree.ExpressionNode;
import parser.tree.IfStatementNode;
import parser.tree.IntegerLiteralNode;
import parser.tree.LocalDeclarationNode;
import parser.tree.MemberAccessNode;
import parser.tree.MethodCallNode;
import parser.tree.MethodNode;
import parser.tree.Node;
import parser.tree.ObjectCreationNode;
import parser.tree.Operator;
import parser.tree.ProgramNode;
import parser.tree.ReturnStatementNode;
import parser.tree.StatementBlockNode;
import parser.tree.StatementNode;
import parser.tree.StringLiteralNode;
import parser.tree.TypeCastNode;
import parser.tree.TypeNode;
import parser.tree.UnaryExpressionNode;
import parser.tree.VariableNode;
import parser.tree.WhileStatementNode;

public class Parser {
	private static final String ERROR_IDENTIFIER = "$ERROR$";

	private final Lexer lexer;
	private final Diagnostics diagnostics;
	private Token current;
	private int previousEnd;
	private int currentStart;

	public Parser(Lexer lexer, Diagnostics diagnostics) {
		this.lexer = lexer;
		this.diagnostics = diagnostics;
		next();
	}

	public ProgramNode parseProgram() {
		var start = currentStart;
		var classes = new ArrayList<ClassNode>();
		while (!isEnd()) {
			classes.add(parseClass());
		}
		var location = new Location(start, previousEnd);
		return new ProgramNode(location, classes);
	}

	private ClassNode parseClass() {
		int start = currentStart;
		check(Tag.CLASS);
		var identifier = readIdentifier();
		BasicTypeNode baseClass = null;
		if (is(Tag.EXTENDS)) {
			next();
			baseClass = parseBasicType();
		}
		check(Tag.OPEN_BRACE);
		var variables = new ArrayList<VariableNode>();
		var methods = new ArrayList<MethodNode>();
		while (!isEnd() && !is(Tag.CLOSE_BRACE)) {
			var member = parseClassMember();
			if (member instanceof VariableNode) {
				variables.add((VariableNode) member);
			} else {
				methods.add((MethodNode) member);
			}
		}
		check(Tag.CLOSE_BRACE);
		var location = new Location(start, previousEnd);
		return new ClassNode(location, identifier, baseClass, variables, methods);
	}

	private Node parseClassMember() {
		int start = currentStart;
		var type = parseType();
		var identifier = readIdentifier();
		if (is(Tag.OPEN_PARENTHESIS)) {
			return parseMethod(start, type, identifier);
		} else {
			check(Tag.SEMICOLON);
			var location = new Location(start, previousEnd);
			return new VariableNode(location, type, identifier);
		}
	}

	private MethodNode parseMethod(int start, TypeNode type, String identifier) {
		var parameters = parseParameterList();
		var statementBlock = parseStatementBlock();
		var location = new Location(start, previousEnd);
		return new MethodNode(location, type, identifier, parameters, statementBlock);
	}

	private List<VariableNode> parseParameterList() {
		var parameters = new ArrayList<VariableNode>();
		check(Tag.OPEN_PARENTHESIS);
		if (isIdentifier()) {
			parameters.add(parseParameter());
			while (is(Tag.COMMA)) {
				next();
				parameters.add(parseParameter());
			}
		}
		check(Tag.CLOSE_PARENTHESIS);
		return parameters;
	}

	private VariableNode parseParameter() {
		int start = currentStart;
		var type = parseType();
		var identifier = readIdentifier();
		var location = new Location(start, previousEnd);
		return new VariableNode(location, type, identifier);
	}

	private TypeNode parseType() {
		int start = currentStart;
		TypeNode type = parseBasicType();
		if (is(Tag.OPEN_BRACKET)) {
			next();
			check(Tag.CLOSE_BRACKET);
			var location = new Location(start, previousEnd);
			type = new ArrayTypeNode(location, type);
		}
		return type;
	}

	private BasicTypeNode parseBasicType() {
		int start = currentStart;
		var identifier = readIdentifier();
		var location = new Location(start, previousEnd);
		return new BasicTypeNode(location, identifier);
	}

	private StatementBlockNode parseStatementBlock() {
		var start = currentStart;
		var statements = new ArrayList<StatementNode>();
		check(Tag.OPEN_BRACE);
		while (!isEnd() && !is(Tag.CLOSE_BRACE)) {
			var statement = parseStatement();
			if (statement != null) {
				statements.add(statement);
			}
		}
		check(Tag.CLOSE_BRACE);
		var location = new Location(start, previousEnd);
		return new StatementBlockNode(location, statements);
	}

	private StatementNode parseStatement() {
		if (is(Tag.IF)) {
			return parseIfStatement();
		} else if (is(Tag.WHILE)) {
			return parseWhileStatement();
		} else if (is(Tag.RETURN)) {
			return parseReturnStatement();
		} else if (isIdentifier()) {
			return parseBasicStatement();
		} else if (is(Tag.SEMICOLON)) {
			next();
		} else if (!is(Tag.CLOSE_BRACE)) {
			error("Invalid statement " + current);
			next();
		}
		return null;
	}

	private StatementNode parseBasicStatement() {
		int start = currentStart;
		var left = readIdentifier();
		var leftLocation = new Location(start, previousEnd);
		if (is(Tag.OPEN_BRACKET)) {
			return parseArrayLocalOrAssign(start, left, leftLocation);
		} else if (is(Tag.PERIOD)) {
			var leftDesignator = new BasicDesignatorNode(leftLocation, left);
			var designator = parseDesignator(start, leftDesignator);
			return parseAssignOrCall(start, designator);
		} else if (is(Tag.ASSIGN)) {
			var designator = new BasicDesignatorNode(leftLocation, left);
			return parseAssignment(start, designator);
		} else if (is(Tag.OPEN_PARENTHESIS)) {
			var designator = new BasicDesignatorNode(leftLocation, left);
			return parseCallStatement(start, designator);
		} else {
			var type = new BasicTypeNode(leftLocation, left);
			return parseLocalDeclaration(start, type);
		}
	}

	private StatementNode parseAssignOrCall(int start, DesignatorNode designator) {
		if (is(Tag.ASSIGN)) {
			return parseAssignment(start, designator);
		} else {
			return parseCallStatement(start, designator);
		}
	}

	private StatementNode parseArrayLocalOrAssign(int start, String left, Location leftLocation) {
		check(Tag.OPEN_BRACKET);
		if (is(Tag.CLOSE_BRACKET)) {
			next();
			var location = new Location(start, previousEnd);
			var elementType = new BasicTypeNode(leftLocation, left);
			var arrayType = new ArrayTypeNode(location, elementType);
			return parseLocalDeclaration(start, arrayType);
		} else {
			var expression = parseExpression();
			check(Tag.CLOSE_BRACKET);
			var location = new Location(start, previousEnd);
			var basicDesignator = new BasicDesignatorNode(leftLocation, left);
			var elementAccess = new ElementAccessNode(location, basicDesignator, expression);
			var designator = parseDesignator(start, elementAccess);
			return parseAssignOrCall(start, designator);
		}
	}

	private StatementNode parseAssignment(int start, DesignatorNode designator) {
		check(Tag.ASSIGN);
		var expression = parseExpression();
		var location = new Location(start, previousEnd);
		check(Tag.SEMICOLON);
		return new AssignmentNode(location, designator, expression);
	}

	private IfStatementNode parseIfStatement() {
		int start = currentStart;
		check(Tag.IF);
		check(Tag.OPEN_PARENTHESIS);
		var condition = parseExpression();
		check(Tag.CLOSE_PARENTHESIS);
		var thenBlock = parseStatementBlock();
		StatementBlockNode elseBlock = null;
		if (is(Tag.ELSE)) {
			next();
			elseBlock = parseStatementBlock();
		}
		var location = new Location(start, previousEnd);
		return new IfStatementNode(location, condition, thenBlock, elseBlock);
	}

	private WhileStatementNode parseWhileStatement() {
		int start = currentStart;
		check(Tag.WHILE);
		check(Tag.OPEN_PARENTHESIS);
		var condition = parseExpression();
		check(Tag.CLOSE_PARENTHESIS);
		var body = parseStatementBlock();
		var location = new Location(start, previousEnd);
		return new WhileStatementNode(location, condition, body);
	}

	private CallStatementNode parseCallStatement(int start, DesignatorNode left) {
		var methodCall = parseMethodCall(start, left);
		check(Tag.SEMICOLON);
		var location = new Location(start, previousEnd);
		return new CallStatementNode(location, methodCall);
	}

	private ReturnStatementNode parseReturnStatement() {
		int start = currentStart;
		check(Tag.RETURN);
		ExpressionNode expression = null;
		if (!is(Tag.SEMICOLON)) {
			expression = parseExpression();
		}
		check(Tag.SEMICOLON);
		var location = new Location(start, previousEnd);
		return new ReturnStatementNode(location, expression);
	}

	private LocalDeclarationNode parseLocalDeclaration(int start, TypeNode type) {
		var identifier = readIdentifier();
		check(Tag.SEMICOLON);
		var location = new Location(start, previousEnd);
		return new LocalDeclarationNode(location, new VariableNode(location, type, identifier));
	}

	private ExpressionNode parseExpression() {
		int start = currentStart;
		var left = parseLogicTerm();
		while (is(Tag.OR)) {
			next();
			var right = parseLogicTerm();
			var location = new Location(start, previousEnd);
			left = new BinaryExpressionNode(location, left, Operator.OR, right);
		}
		return left;
	}

	private ExpressionNode parseLogicTerm() {
		int start = currentStart;
		var left = parseLogicFactor();
		while (is(Tag.AND)) {
			next();
			var right = parseLogicFactor();
			var location = new Location(start, previousEnd);
			left = new BinaryExpressionNode(location, left, Operator.AND, right);
		}
		return left;
	}

	private static final Map<Tag, Operator> COMPARE_OPERATORS = Map.of(Tag.EQUAL, Operator.EQUAL, Tag.UNEQUAL,
			Operator.UNEQUAL, Tag.LESS, Operator.LESS, Tag.LESS_EQUAL, Operator.LESS_EQUAL, Tag.GREATER,
			Operator.GREATER, Tag.GREATER_EQUAL, Operator.GREATER_EQUAL, Tag.INSTANCEOF, Operator.INSTANCEOF);

	private ExpressionNode parseLogicFactor() {
		int start = currentStart;
		var left = parseSimpleExpression();
		while (isOperator(COMPARE_OPERATORS)) {
			var op = readOperator(COMPARE_OPERATORS);
			var right = parseSimpleExpression();
			var location = new Location(start, previousEnd);
			left = new BinaryExpressionNode(location, left, op, right);
		}
		return left;
	}

	private static final Map<Tag, Operator> ADD_OPERATORS = Map.of(Tag.PLUS, Operator.PLUS, Tag.MINUS, Operator.MINUS);

	private ExpressionNode parseSimpleExpression() {
		var start = currentStart;
		var left = parseTerm();
		while (isOperator(ADD_OPERATORS)) {
			var op = readOperator(ADD_OPERATORS);
			var right = parseTerm();
			var location = new Location(start, previousEnd);
			left = new BinaryExpressionNode(location, left, op, right);
		}
		return left;
	}

	private static final Map<Tag, Operator> MULTIPLY_OPERATORS = Map.of(Tag.TIMES, Operator.TIMES, Tag.DIVIDE,
			Operator.DIVIDE, Tag.MODULO, Operator.MODULO);

	private ExpressionNode parseTerm() {
		var start = currentStart;
		var left = parseFactor();
		while (isOperator(MULTIPLY_OPERATORS)) {
			var op = readOperator(MULTIPLY_OPERATORS);
			var right = parseFactor();
			var location = new Location(start, previousEnd);
			left = new BinaryExpressionNode(location, left, op, right);
		}
		return left;
	}

	private static final Map<Tag, Operator> UNARY_OPERATORS = Map.of(Tag.NOT, Operator.NOT, Tag.PLUS, Operator.PLUS,
			Tag.MINUS, Operator.MINUS);

	private ExpressionNode parseFactor() {
		if (isOperator(UNARY_OPERATORS)) {
			return parseUnaryExpression();
		} else if (is(Tag.OPEN_PARENTHESIS)) {
			int start = currentStart;
			next();
			var expression = parseExpression();
			check(Tag.CLOSE_PARENTHESIS);
			if (isIdentifier()) {
				return parseTypeCast(start, expression);
			} else {
				return expression;
			}
		} else {
			return parseOperand();
		}
	}

	private ExpressionNode parseUnaryExpression() {
		int start = currentStart;
		var op = readOperator(UNARY_OPERATORS);
		if (op == Operator.MINUS && isInteger()) {
			var value = readInteger(true);
			return new IntegerLiteralNode(new Location(start, previousEnd), -value);
		}
		var operand = parseFactor();
		var location = new Location(start, previousEnd);
		return new UnaryExpressionNode(location, op, operand);
	}

	private ExpressionNode parseOperand() {
		if (isInteger()) {
			return parseIntegerLiteral();
		} else if (isString()) {
			return parseStringLiteral();
		} else if (is(Tag.NEW)) {
			return parseCreation();
		} else {
			int start = currentStart;
			var designator = parseDesignator();
			if (is(Tag.OPEN_PARENTHESIS)) {
				return parseMethodCall(start, designator);
			} else {
				return designator; // includes "true", "false", "null"
			}
		}
	}

	private StringLiteralNode parseStringLiteral() {
		int start = currentStart;
		var value = readString();
		var location = new Location(start, previousEnd);
		return new StringLiteralNode(location, value);
	}

	private IntegerLiteralNode parseIntegerLiteral() {
		var start = currentStart;
		var value = readInteger(false);
		var location = new Location(start, previousEnd);
		return new IntegerLiteralNode(location, value);
	}

	private TypeCastNode parseTypeCast(int start, ExpressionNode expression) {
		var designator = parseDesignator();
		var location = new Location(start, previousEnd);
		String identifier;
		if (expression instanceof BasicDesignatorNode) {
			identifier = ((BasicDesignatorNode) expression).getIdentifier();
		} else {
			error("Invalid type " + expression + " in type cast");
			identifier = ERROR_IDENTIFIER;
		}
		var type = new BasicTypeNode(expression.getLocation(), identifier);
		return new TypeCastNode(location, type, designator);
	}

	private ExpressionNode parseCreation() {
		int start = currentStart;
		check(Tag.NEW);
		int identStart = currentStart;
		var identifier = readIdentifier();
		var identLocation = new Location(identStart, previousEnd);
		var type = new BasicTypeNode(identLocation, identifier);
		if (is(Tag.OPEN_PARENTHESIS)) {
			next();
			check(Tag.CLOSE_PARENTHESIS);
			var location = new Location(start, previousEnd);
			return new ObjectCreationNode(location, type);
		} else {
			check(Tag.OPEN_BRACKET);
			var expression = parseExpression();
			check(Tag.CLOSE_BRACKET);
			var location = new Location(start, previousEnd);
			return new ArrayCreationNode(location, type, expression);
		}
	}

	private MethodCallNode parseMethodCall(int start, DesignatorNode left) {
		var arguments = new ArrayList<ExpressionNode>();
		check(Tag.OPEN_PARENTHESIS);
		if (!is(Tag.CLOSE_PARENTHESIS)) {
			arguments.add(parseExpression());
			while (is(Tag.COMMA)) {
				next();
				arguments.add(parseExpression());
			}
		}
		check(Tag.CLOSE_PARENTHESIS);
		var location = new Location(start, previousEnd);
		return new MethodCallNode(location, left, arguments);
	}

	private DesignatorNode parseDesignator() {
		int start = currentStart;
		var identifier = readIdentifier();
		var location = new Location(start, previousEnd);
		var left = new BasicDesignatorNode(location, identifier);
		return parseDesignator(start, left);
	}

	private DesignatorNode parseDesignator(int start, DesignatorNode left) {
		while (is(Tag.PERIOD) || is(Tag.OPEN_BRACKET)) {
			if (is(Tag.PERIOD)) {
				next();
				String identifier = readIdentifier();
				var location = new Location(start, previousEnd);
				left = new MemberAccessNode(location, left, identifier);
			} else {
				check(Tag.OPEN_BRACKET);
				var expression = parseExpression();
				check(Tag.CLOSE_BRACKET);
				var location = new Location(start, previousEnd);
				left = new ElementAccessNode(location, left, expression);
			}
		}
		return left;
	}

	private boolean is(Tag tag) {
		return current instanceof StaticToken && ((StaticToken) current).getTag() == tag;
	}

	private boolean isEnd() {
		return is(Tag.END);
	}

	private void check(Tag tag) {
		if (!is(tag)) {
			error(tag + " expected");
		}
		next();
	}

	private boolean isIdentifier() {
		return current instanceof IdentifierToken;
	}

	private String readIdentifier() {
		if (!isIdentifier()) {
			error("Identifier expected");
			next();
			return ERROR_IDENTIFIER;
		}
		var name = ((IdentifierToken) current).getValue();
		next();
		return name;
	}

	private boolean isInteger() {
		return current instanceof IntegerToken;
	}

	private int readInteger(boolean allowMinValue) {
		if (!isInteger()) {
			error("Integer expected");
			next();
			return 0;
		}
		var token = (IntegerToken) current;
		if (token.getValue() == Integer.MIN_VALUE && !allowMinValue) {
			error("Too large integer");
		}
		next();
		return token.getValue();
	}

	private boolean isString() {
		return current instanceof StringToken;
	}

	private String readString() {
		if (!isString()) {
			error("String expected");
			next();
			return "";
		}
		var value = ((StringToken) current).getValue();
		next();
		return value;
	}

	private Operator readOperator(Map<Tag, Operator> map) {
		var op = map.get(((StaticToken) current).getTag());
		next();
		return op;
	}

	private boolean isOperator(Map<Tag, Operator> map) {
		return current instanceof StaticToken && map.containsKey(((StaticToken) current).getTag());
	}

	private void next() {
		if (current != null) {
			previousEnd = current.getLocation().getEnd();
		}
		current = lexer.next();
		currentStart = current.getLocation().getStart();
	}

	private void error(String message) {
		diagnostics.reportError(message + " LOCATION " + current.getLocation());
	}
}
