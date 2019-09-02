import java.io.FileReader;
import java.io.IOException;

import checker.Checker;
import common.Diagnostics;
import generator.Generator;
import lexer.Lexer;
import parser.Parser;

public class Compiler {
	private static final String OUTPUT_FILE_SUFFIX = ".out";

	public static void main(String[] arguments) throws IOException {
		if (arguments.length != 1) {
			System.out.println("Usage: java Compiler <file>");
			return;
		}
		var inputFile = arguments[0];
		var outputFile = getOutputFileName(inputFile);
		try (var reader = new FileReader(inputFile)) {
			var diagnostics = new Diagnostics();
			var lexer = new Lexer(reader, diagnostics);
			var parser = new Parser(lexer, diagnostics);
			var tree = parser.parseProgram();
			if (failed(diagnostics)) {
				System.out.println("Exit after syntax errors");
				return;
			}
			var checker = new Checker(tree, diagnostics);
			var table = checker.getSymbolTable();
			if (failed(diagnostics)) {
				System.out.println("Exit after semantic errors");
				return;
			}
			var generator = new Generator(table);
			var assembly = generator.getAssembly();
			System.out.println(assembly);
			assembly.save(outputFile);
			System.out.println("Successfully compiled");
		}
	}

	private static String getOutputFileName(String inputFile) {
		int index = inputFile.lastIndexOf('.');
		if (index >= 0) {
			inputFile = inputFile.substring(0, index);
		}
		return inputFile + OUTPUT_FILE_SUFFIX;
	}

	private static boolean failed(Diagnostics diagnostics) {
		if (diagnostics.hasErrors()) {
			for (String message : diagnostics.getErrors()) {
				System.out.println(message);
			}
			return true;
		}
		return false;
	}
}
