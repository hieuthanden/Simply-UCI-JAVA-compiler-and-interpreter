package lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import common.Diagnostics;
import common.Location;
import lexer.tokens.StaticToken;
import lexer.tokens.IdentifierToken;
import lexer.tokens.IntegerToken;
import lexer.tokens.StringToken;
import lexer.tokens.Tag;
import lexer.tokens.Token;

public class Lexer {
	private final Reader reader;
	private final Diagnostics diagnostics;
	private int position;
	private char current;
	private boolean end;
	
	private static final Map<String, Tag> KEYWORDS = Map.of(
	      "class", Tag.CLASS,
	      "else", Tag.ELSE,
	      "extends", Tag.EXTENDS,
	      "if", Tag.IF,
	      "instanceof", Tag.INSTANCEOF,
	      "new", Tag.NEW,
	      "return", Tag.RETURN,
	      "while", Tag.WHILE
	    );

	public Lexer(Reader reader, Diagnostics diagnostics) {
		this.reader = reader;
		this.diagnostics = diagnostics;
		position = -1;
		readNext();
	}

	public Token next() {
		while (true) {
			skipBlanks();
			int tokenStart = position;
			if (end) {
				return new StaticToken(new Location(tokenStart, position), Tag.END);
			}
			if (isDigit(current)) {
				return readInteger();
			}
			if (isLetter(current)) {
				return readName();
			}
			switch (current) {
			case '/':
				readNext();
				if (current == '/') {
					skipLineComment();
				} else if (current == '*') {
					skipBlockComment();
				} else {
					return new StaticToken(new Location(tokenStart, position), Tag.DIVIDE);
				}
				break;
			case '"':
				return readString();
			case '&':
				readNext();
				checkNext('&');
				return new StaticToken(new Location(tokenStart, position), Tag.AND);
			case '|':
				readNext();
				checkNext('|');
				return new StaticToken(new Location(tokenStart, position), Tag.OR);
			case '!':
				return readPotentialEqualsOperator(Tag.NOT, Tag.UNEQUAL);
			case '=':
				return readPotentialEqualsOperator(Tag.ASSIGN, Tag.EQUAL);
			case '<':
				return readPotentialEqualsOperator(Tag.LESS, Tag.LESS_EQUAL);
			case '>':
				return readPotentialEqualsOperator(Tag.GREATER, Tag.GREATER_EQUAL);
			case '+':
				return readStaticToken(Tag.PLUS);
			case '-':
				return readStaticToken(Tag.MINUS);
			case '*':
				return readStaticToken(Tag.TIMES);
			case '%':
				return readStaticToken(Tag.MODULO);
			case ',':
				return readStaticToken(Tag.COMMA);
			case ';':
				return readStaticToken(Tag.SEMICOLON);
			case '.':
				return readStaticToken(Tag.PERIOD);
			case '(':
				return readStaticToken(Tag.OPEN_PARENTHESIS);
			case ')':
				return readStaticToken(Tag.CLOSE_PARENTHESIS);
			case '[':
				return readStaticToken(Tag.OPEN_BRACKET);
			case ']':
				return readStaticToken(Tag.CLOSE_BRACKET);
			case '{':
				return readStaticToken(Tag.OPEN_BRACE);
			case '}':
				return readStaticToken(Tag.CLOSE_BRACE);
			default:
				error(position, "unexpected token " + current);
				readNext();
				break;
			}
		}
	}

	private StaticToken readPotentialEqualsOperator(Tag plain, Tag equals) {
		readNext();
		if (current == '=') {
			readNext();
			return new StaticToken(new Location(position - 2, position), equals);
		}
		return new StaticToken(new Location(position - 1, position), plain);
	}

	private StaticToken readStaticToken(Tag tag) {
		readNext();
		return new StaticToken(new Location(position - 1, position), tag);
	}

	private Token readName() {
		int tokenStart = position;
		String name = Character.toString(current);
		readNext();
		while (!end && (isLetter(current) || isDigit(current))) {
			name += current;
			readNext();
		}
		if (KEYWORDS.containsKey(name)) {
			return new StaticToken(new Location(tokenStart, position), KEYWORDS.get(name));
		}
		return new IdentifierToken(new Location(tokenStart, position), name);
	}

	private IntegerToken readInteger() {
		int tokenStart = position;
		boolean overflow = false;
		int value = 0;
		while (!end && isDigit(current)) {
			int digit = current - '0';
			if (value > Integer.MAX_VALUE / 10 || value * 10 - 1 > Integer.MAX_VALUE - digit) {
				overflow = true;
			}
			value = value * 10 + digit;
			readNext();
		}
		if (overflow) {
			error(tokenStart, "Too large integer value");
			value = Integer.MAX_VALUE;
		}
		return new IntegerToken(new Location(tokenStart, position), value);
	}

	private StringToken readString() {
		int tokenStart = position;
		readNext();
		String value = "";
		while (!end && current != '"') {
			value += current;
			readNext();
		}
		if (end) {
			error(tokenStart, "String not closed");
		}
		checkNext('"');
		return new StringToken(new Location(tokenStart, position), value);
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean isLetter(char c) {
		return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
	}

	private void skipLineComment() {
		checkNext('/');
		while (!end && current != '\n') {
			readNext();
		}
	}

	private void skipBlockComment() {
		int tokenStart = position - 1;
		do {
			readNext(); 
			while (!end && current != '*') {
				readNext();
			}
			readNext();
		} while (!end && current != '/');
		if (end) {
			error(tokenStart, "Comment block not closed");
		}
		readNext();
	}

	private void skipBlanks() {
		while (!end && current <= ' ') {
			readNext();
		}
	}

	private void checkNext(char value) {
		if (value != current) {
			error(position, "Expected " + value + " instead of " + current);
		}
		readNext();
	}

	private void readNext() {
		try {
			var value = reader.read();
			if (value < 0) {
				if (!end) {
					position++;
				}
				current = '\0';
				end = true;
			} else {
				current = (char) value;
				position++;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void error(int position, String message) {
		diagnostics.reportError(message + " POSITION " + position);
	}
}
