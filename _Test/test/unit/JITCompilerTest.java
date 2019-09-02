package unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import checker.Checker;
import common.Diagnostics;
import error.VMException;
import generator.Generator;
import lexer.Lexer;
import parser.Parser;
import runtime.Interpreter;
import runtime.Loader;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;

public class JITCompilerTest {
	@Test
	public void testConstReturn() {
		var source = "class Test {" + 
					"  void main() {" + 
					"    writeInt(getValue());" + 
					"  }" + 
					"" + 
					"  int getValue() {" + 
					"    return 12345;" + 
					"  }" + 
					"}";
		var expected = "12345";
		var output = compileAndJITRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testMax() {
		var source = "class Test {" + 
					"	void main() { " + 
					"		writeInt(max(9,11)); " + 
					"       writeString(\"\");  " + 
					"		writeInt(max(7,3));  " + 
					"	}" + 
					"	int max(int a, int b) {" + 
					"       if (a > b) {" + 
					"           return a;" + 
					"       }" + 
					"       return b;" + 
					"   }" + 
					"}";
		var expected = "11 7";
		var output = compileAndJITRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testLocals() {
		var source = "class Test {" + 
					"	void main() {" + 
					"		writeInt(evaluate());" + 
					"	}" + 
					"	int evaluate() {" + 
					"		int a;" + 
					"		int b;" + 
					"		int c;" + 
					"		a = 1;" + 
					"		b = 2;" + 
					"		c = 3;" + 
					"       return a + b - c * c;" + 
					"   }" + 
					"}";
		var expected = "-6";
		var output = compileAndJITRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testWhileLoop() {
		var source = "class Test {" + 
					"   void main() {" + 
					"      writeInt(whileTest(10));" + 
					"   }" + 
					"   int whileTest(int n) {" + 
					"      int i;" + 
					"      i = 0;" + 
					"	   int r;" + 
					"	   r = 1;" + 
					"      while (i < n) {" + 
					"	     i = i + 1;" + 
					"	     r = r + r;" + 
					"	   } " + 
					"	   return r;" + 
					"   }" + 
					"}";
		var expected = "1024";
		var output = compileAndJITRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testFactorial() {
		var source = "class Test {" + 
					"	void main() { " + 
					"		writeInt(factorial(12));  " + 
					"	}" + 
					"   int factorial(int i) {" + 
					"		int p;" + 
					"		p = 1;" + 
					"		while (i > 1) {" + 
					"			p = p * i;" + 
					"			i = i - 1;" + 
					"		}" + 
					"       return p;" + 
					"   }" + 
					"}";
		var expected = "479001600";
		var output = compileAndJITRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testPrimeTest() {
		var source = "class Test {" + 
					"   void main() {" + 
					"      int n;" + 
					"	   n = 97;" + 
					"      writeInt(n);" + 
					"      if (isPrime(n)) {" + 
					"		  writeString(\" is prime.\");" + 
					"	   } else {" + 
					"		  writeString(\" is NOT prime.\");" + 
					"	   }" + 
					"   }" + 
					"   boolean isPrime(int n) {" + 
					"      int i;" + 
					"	   i = 2;" + 
					"	   while (i < n) {" + 
					"	     if (n % i == 0) { return false; }" + 
					"	     i = i + 1;" + 
					"	   }" + 
					"      return true;" + 
					"   }" + 
					"}";
		var expected = "97 is prime. ";
		var output = compileAndJITRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testINEGBNEGDIV() {
		var source = "class Test {" +
				"	boolean f;" +
				"	void main() { " +
				"		writeInt(max(-9,22)); " +
				"       writeString(\"\");  " +
				"		writeInt(max(-7,6));  " +
				"       writeString(\"\");  " +
				"		writeInt(max(5,5));  " +
				"	}" +
				"	int max(int a, int b) {" +
				"		boolean t;" +
				"		t = true;" +
				"		f = false;" +
				"		if (!(a != b)) {" +
				"			return 0;" +
				"		}" +
				"       	if (a/2 >= b/2 && !f) {" +
				"           		return a/2;" +
				"       	}" +
				"		else {" +
				"       		return b/2;" +
				"		}" +
				"   }" +
				"}";
		var expected = "11 3 0";
		var output = compileAndJITRun(source);
		assertEquals(expected, output);
	}
	
	private static String compileAndJITRun(String source) {
		var original = System.out;
		try {
			var output = new ByteArrayOutputStream();
			System.setOut(new PrintStream(output));
			var diagnostics = new Diagnostics();
			var lexer = new Lexer(new StringReader(source), diagnostics);
			var parser = new Parser(lexer, diagnostics);
			var tree = parser.parseProgram();
			var checker = new Checker(tree, diagnostics);
			assertFalse(diagnostics.hasErrors(), "Compiler error: " + diagnostics.getErrors());
			var table = checker.getSymbolTable();
			var generator = new Generator(table);
			var assembly = generator.getAssembly();
			var loader = new Loader(assembly);
			var interpreter = new Interpreter(loader, true);
			try {
				interpreter.run();
			} catch (VMException e) {
				System.out.println("VM ERROR: " + e.getMessage());
			}
			System.out.flush();
			return output.toString().replaceAll("\r", "").replaceAll("\n", " ");
		} finally {
			System.setOut(original);
		}
	}
}

