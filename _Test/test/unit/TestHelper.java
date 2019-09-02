package unit;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import checker.Checker;
import common.Diagnostics;
import error.VMException;
import generator.Generator;
import lexer.Lexer;
import parser.Parser;
import runtime.Interpreter;
import runtime.Loader;

public class TestHelper {
	public static String compileAndRun(String source) {
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
			var interpreter = new Interpreter(loader, false);
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
