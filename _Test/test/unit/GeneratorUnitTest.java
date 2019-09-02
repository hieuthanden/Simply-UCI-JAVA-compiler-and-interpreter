package unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import checker.Checker;
import common.Diagnostics;
import generator.Generator;
import lexer.Lexer;
import parser.Parser;

public class GeneratorUnitTest {
	@Test
	public void testHelloWorld() {
		var input = "class Test {" + 
				"  void main() {" + 
				"    writeString(\"Hello World!\");" + 
				"  }" + 
				"}";
		var output = "METHOD null main() [] " + 
				"CODE: { " + 
				"LDC Hello World! " + 
				"INVOKESTATIC writeString " + 
				"RETURN " + 
				"} ";
		assertEquals(output, compile(input));
	}
	
	@Test
	public void testArithmetics() {
		var input = "class Test {" + 
				"  void main() {" + 
				"    writeInt(-12 - 1 + +2 * 9 / 4 % 3);" + 
				"  }" + 
				"}";
		var output = "METHOD null main() [] " + 
				"CODE: { " + 
				"LDC -12 " + 
				"LDC 1 " + 
				"ISUB " + 
				"LDC 2 " + 
				"LDC 9 " + 
				"IMUL " + 
				"LDC 4 " + 
				"IDIV " + 
				"LDC 3 " + 
				"IREM " + 
				"IADD " + 
				"INVOKESTATIC writeInt " + 
				"RETURN " + 
				"} ";
		assertEquals(output, compile(input));
	}
	
	@Test
	public void testIfStatement() {
		var input = "class Test {" + 
				"  void main() {" + 
				"    int a;" + 
				"    a = 3;" + 
				"    if (a < 3) {" + 
				"      writeString(\"Too small\");" + 
				"    } else {" + 
				"      if (a > 3) {" + 
				"         writeString(\"Too large\");" + 
				"      } else {" + 
				"         writeString(\"OK\");  " + 
				"      }" + 
				"    }" + 
				"  }" + 
				"}";
		var output = "METHOD null main() [int] " + 
				"CODE: { " + 
				"LDC 3 " + 
				"STORE 1 " + 
				"LOAD 1 " + 
				"LDC 3 " + 
				"ICMPLT " + 
				"IF_FALSE 3 " + 
				"LDC Too small " + 
				"INVOKESTATIC writeString " + 
				"GOTO 9 " + 
				"LOAD 1 " + 
				"LDC 3 " + 
				"ICMPGT " + 
				"IF_FALSE 3 " + 
				"LDC Too large " + 
				"INVOKESTATIC writeString " + 
				"GOTO 2 " + 
				"LDC OK " + 
				"INVOKESTATIC writeString " + 
				"RETURN " + 
				"} ";
		assertEquals(output, compile(input));
	}
	
	@Test
	public void testWhileStatement() {
		var input = "class Test {" + 
				"  void main() {" + 
				"    int[] array;" + 
				"    array = new int[10];" + 
				"    int index;" + 
				"    index = 0;" + 
				"    while (index < array.length) {" + 
				"      array[index] = index * index;" + 
				"      writeInt(array[index]);" + 
				"      index = index + 1;" + 
				"    }" + 
				"  } " + 
				"}" + 
				"";
		var output = "METHOD null main() [int[], int] " + 
				"CODE: { " + 
				"LDC 10 " + 
				"NEWARRAY int[] " + 
				"STORE 1 " + 
				"LDC 0 " + 
				"STORE 2 " + 
				"LOAD 2 " + 
				"LOAD 1 " + 
				"ARRAYLENGTH " + 
				"ICMPLT " + 
				"IF_FALSE 15 " + 
				"LOAD 1 " + 
				"LOAD 2 " + 
				"LOAD 2 " + 
				"LOAD 2 " + 
				"IMUL " + 
				"ASTORE " + 
				"LOAD 1 " + 
				"LOAD 2 " + 
				"ALOAD " + 
				"INVOKESTATIC writeInt " + 
				"LOAD 2 " + 
				"LDC 1 " + 
				"IADD " + 
				"STORE 2 " + 
				"GOTO -20 " + 
				"RETURN " + 
				"} ";
		assertEquals(output, compile(input));
	}
	
	@Test
	public void testLogicalShortcuts() {
		var input = "class Test {" + 
				"  void main() {" + 
				"    boolean a;" + 
				"    boolean b;" + 
				"    a = true;" + 
				"    b = false;" + 
				"    if (a && !b || !a && b) {" + 
				"      writeString(\"OK\");" + 
				"    }" + 
				"  }  " + 
				"}";
		var output = "METHOD null main() [boolean, boolean] " + 
				"CODE: { " + 
				"LDC true " + 
				"STORE 1 " + 
				"LDC false " + 
				"STORE 2 " + 
				"LOAD 1 " + 
				"IF_FALSE 3 " + 
				"LOAD 2 " + 
				"BNEG " + 
				"GOTO 1 " + 
				"LDC false " + 
				"IF_TRUE 7 " + 
				"LOAD 1 " + 
				"BNEG " + 
				"IF_FALSE 2 " + 
				"LOAD 2 " + 
				"GOTO 1 " + 
				"LDC false " + 
				"GOTO 1 " + 
				"LDC true " + 
				"IF_FALSE 2 " + 
				"LDC OK " + 
				"INVOKESTATIC writeString " + 
				"RETURN " + 
				"} ";
		assertEquals(output, compile(input));
	}
	
	@Test
	public void testFunctionCall() {
		var input = "class Test {" + 
				"  void main() {" + 
				"    writeInt(div(div(100, 2), 2));" + 
				"  }" + 
				"  " + 
				"  int div(int x, int y) {" + 
				"    return x / y;" + 
				"  }" + 
				"}";
		var output = "METHOD null main() [] " + 
				"CODE: { " +
				"LOAD 0 " +
				"LOAD 0 " +
				"LDC 100 " + 
				"LDC 2 " + 
				"INVOKEVIRTUAL div " + 
				"LDC 2 " + 
				"INVOKEVIRTUAL div " + 
				"INVOKESTATIC writeInt " + 
				"RETURN " + 
				"} " + 
				"METHOD int div(int, int) [] " + 
				"CODE: { " + 
				"LOAD 1 " + 
				"LOAD 2 " + 
				"IDIV " + 
				"RETURN " + 
				"RETURN " + 
				"} ";
		assertEquals(output, compile(input));
	}
	
	private String compile(String source) {
		var diagnostics = new Diagnostics();
		var lexer = new Lexer(new StringReader(source), diagnostics);
		var parser = new Parser(lexer, diagnostics);
		var tree = parser.parseProgram();
		var checker = new Checker(tree, diagnostics);
		assertFalse(diagnostics.hasErrors(), "compiler error: " + diagnostics.getErrors());
		var table = checker.getSymbolTable();
		var generator = new Generator(table);
		var assembly = generator.getAssembly(); 
		return assembly.toString().replaceAll("\r", "").replaceAll("\n", " ");
	}
}
