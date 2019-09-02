package lexer.tokens;

public enum Tag {
	// specials
    END,
    // keywords
    CLASS,
    ELSE,
    EXTENDS,
    IF,
    INSTANCEOF,
    NEW,
    RETURN,
    WHILE,
    // operators
    AND,
    ASSIGN,
    DIVIDE,
    EQUAL, UNEQUAL,
    GREATER, GREATER_EQUAL,
    MINUS,
    MODULO,
    NOT,
    OR,
    PLUS,
    LESS, LESS_EQUAL,
    TIMES,
    // punctuation
    OPEN_BRACE, CLOSE_BRACE,
    OPEN_BRACKET, CLOSE_BRACKET,
    OPEN_PARENTHESIS, CLOSE_PARENTHESIS,
    COMMA,
    PERIOD,
    SEMICOLON
}
