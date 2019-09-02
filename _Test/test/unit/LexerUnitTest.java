package unit;
import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import common.Diagnostics;
import lexer.Lexer;
import lexer.tokens.StaticToken;
import lexer.tokens.StringToken;
import lexer.tokens.IdentifierToken;
import lexer.tokens.IntegerToken;
import lexer.tokens.Tag;
import lexer.tokens.Token;
import static lexer.tokens.Tag.*;

public class LexerUnitTest {
	@Test
	public void testBlockComment() {
		var source = "class /* Comment */ A { }";
		var diagnostics = new Diagnostics();
		var lexer = new Lexer(new StringReader(source), diagnostics);
		assertStaticToken(CLASS, lexer.next());
		assertIdentifierToken("A", lexer.next());
		assertStaticToken(OPEN_BRACE, lexer.next());
		assertStaticToken(CLOSE_BRACE, lexer.next());
		assertStaticToken(END, lexer.next());
		assertFalse(diagnostics.hasErrors());
	}
	
	@Test
	public void testLineComment() {
		var source = "class // Comment \r\nA { }";
		var diagnostics = new Diagnostics();
		var lexer = new Lexer(new StringReader(source), diagnostics);
		assertStaticToken(CLASS, lexer.next());
		assertIdentifierToken("A", lexer.next());
		assertStaticToken(OPEN_BRACE, lexer.next());
		assertStaticToken(CLOSE_BRACE, lexer.next());
		assertStaticToken(END, lexer.next());
		assertFalse(diagnostics.hasErrors());
	}
	
	@Test
	public void testAllKeywords() {
		var source = "class else extends if instanceof new return while";
		var diagnostics = new Diagnostics();
		var lexer = new Lexer(new StringReader(source), diagnostics);
		assertStaticToken(CLASS, lexer.next());
		assertStaticToken(ELSE, lexer.next());
		assertStaticToken(EXTENDS, lexer.next());
		assertStaticToken(IF, lexer.next());
		assertStaticToken(INSTANCEOF, lexer.next());
		assertStaticToken(NEW, lexer.next());
		assertStaticToken(RETURN, lexer.next());
		assertStaticToken(WHILE, lexer.next());
		assertStaticToken(END, lexer.next());
		assertFalse(diagnostics.hasErrors());
	}
	
	@Test
	public void testAllOperators() {
		var source = "&& = / == != > >= - % ! || + < <= *";
		var diagnostics = new Diagnostics();
		var lexer = new Lexer(new StringReader(source), diagnostics);
		assertStaticToken(AND, lexer.next());
		assertStaticToken(ASSIGN, lexer.next());
		assertStaticToken(DIVIDE, lexer.next());
		assertStaticToken(EQUAL, lexer.next());
		assertStaticToken(UNEQUAL, lexer.next());
		assertStaticToken(GREATER, lexer.next());
		assertStaticToken(GREATER_EQUAL, lexer.next());
		assertStaticToken(MINUS, lexer.next());
		assertStaticToken(MODULO, lexer.next());
		assertStaticToken(NOT, lexer.next());
		assertStaticToken(OR, lexer.next());
		assertStaticToken(PLUS, lexer.next());
		assertStaticToken(LESS, lexer.next());
		assertStaticToken(LESS_EQUAL, lexer.next());
		assertStaticToken(TIMES, lexer.next());
		assertStaticToken(END, lexer.next());
		assertFalse(diagnostics.hasErrors());
	}
	
	@Test
	public void testAllPunctuation() {
		var source = "{}[](),.;";
		var diagnostics = new Diagnostics();
		var lexer = new Lexer(new StringReader(source), diagnostics);
		assertStaticToken(OPEN_BRACE, lexer.next());
		assertStaticToken(CLOSE_BRACE, lexer.next());
		assertStaticToken(OPEN_BRACKET, lexer.next());
		assertStaticToken(CLOSE_BRACKET, lexer.next());
		assertStaticToken(OPEN_PARENTHESIS, lexer.next());
		assertStaticToken(CLOSE_PARENTHESIS, lexer.next());
		assertStaticToken(COMMA, lexer.next());
		assertStaticToken(PERIOD, lexer.next());
		assertStaticToken(SEMICOLON, lexer.next());
		assertStaticToken(END, lexer.next());
		assertFalse(diagnostics.hasErrors());
	}
	
	@Test
	public void testAllValueTokens() {
		var source = "123 45\"ABC!\"12abc34";
		var diagnostics = new Diagnostics();
		var lexer = new Lexer(new StringReader(source), diagnostics);
		assertIntegerToken(123, lexer.next());
		assertIntegerToken(45, lexer.next());
		assertStringToken("ABC!", lexer.next());
		assertIntegerToken(12, lexer.next());
		assertIdentifierToken("abc34", lexer.next());
		assertStaticToken(END, lexer.next());
		assertFalse(diagnostics.hasErrors());
	}
	
	// TODO: Optionally: check locations too 
	private void assertStaticToken(Tag tag, Token actual) {
		assertTrue(actual instanceof StaticToken && ((StaticToken)actual).getTag() == tag); 
	}
	
	private void assertIdentifierToken(String value, Token actual) {
		assertTrue(actual instanceof IdentifierToken && ((IdentifierToken)actual).getValue().equals(value));
	}
	
	private void assertIntegerToken(int value, Token actual) {
		assertTrue(actual instanceof IntegerToken && ((IntegerToken)actual).getValue() == value);
	}
	
	private void assertStringToken(String value, Token actual) {
		assertTrue(actual instanceof StringToken && ((StringToken)actual).getValue().equals(value));
	}
}
