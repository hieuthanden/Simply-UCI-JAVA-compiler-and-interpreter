package checker;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import checker.symbols.SymbolTable;
import checker.symbols.TypeSymbol;
import checker.symbols.VariableSymbol;
import common.Diagnostics;
import common.Location;
import parser.tree.AssignmentNode;
import parser.tree.BasicTypeNode;
import parser.tree.CallStatementNode;
import parser.tree.ClassNode;
import parser.tree.IfStatementNode;
import parser.tree.LocalDeclarationNode;
import parser.tree.MethodNode;
import parser.tree.ProgramNode;
import parser.tree.ReturnStatementNode;
import parser.tree.StatementBlockNode;
import parser.tree.StatementNode;
import parser.tree.TypeNode;
import parser.tree.VariableNode;
import parser.tree.Visitor;
import parser.tree.WhileStatementNode;
import checker.symbols.ClassSymbol;
import checker.symbols.ConstantSymbol;
import checker.symbols.FieldSymbol;
import checker.symbols.GlobalScope;
import checker.symbols.InbuiltTypeSymbol;
import checker.symbols.LocalSymbol;
import checker.symbols.MethodSymbol;
import checker.symbols.ParameterSymbol;
import checker.symbols.Symbol;

class TableConstructor {
	private final SymbolTable symbolTable = new SymbolTable();
	private final GlobalScope globalScope;
	private final Diagnostics diagnostics;

	public TableConstructor(ProgramNode syntaxTree, Diagnostics diagnostics) {
		Objects.requireNonNull(syntaxTree);
		Objects.requireNonNull(diagnostics);
		this.globalScope = symbolTable.getGlobalScope();
		this.diagnostics = diagnostics;
		registerDeclarations(syntaxTree);
		checkValidIdentifiers();
		checkUniqueIdentifiers();
		if (!diagnostics.hasErrors()) {
			resolveTypesInSymbols();
		}
		if (!diagnostics.hasErrors()) {
			checkInheritance();
		}
		if (!diagnostics.hasErrors()) {
			checkMainMethod();
		}
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	private void registerDeclarations(ProgramNode programNode) {
		registerBuiltIns();
		for (var classNode : programNode.getClasses()) {
			registerClass(classNode);
		}
	}

	private void registerBuiltIns() {
		registerBuiltInTypes();
		registerBuiltInMethods();
		registerBuiltInConstants();
		registerBuiltInField();
	}

	private void registerBuiltInField() {
		var arrayLength = new FieldSymbol(globalScope, GlobalScope.ARRAY_LENGTH_NAME);
		arrayLength.setType(globalScope.getIntType());
		globalScope.setArrayLength(arrayLength);
	}

	private void registerBuiltInConstants() {
		globalScope.setNullConstant(registerBuiltInConstant(globalScope.getNullType(), GlobalScope.NULL_CONSTANT_NAME));
		globalScope.setFalseConstant(
				registerBuiltInConstant(globalScope.getBooleanType(), GlobalScope.FALSE_CONSTANT_NAME));
		globalScope
				.setTrueConstant(registerBuiltInConstant(globalScope.getBooleanType(), GlobalScope.TRUE_CONSTANT_NAME));
	}

	private ConstantSymbol registerBuiltInConstant(TypeSymbol type, String name) {
		var constant = new ConstantSymbol(globalScope, name, type);
		globalScope.getConstants().add(constant);
		return constant;
	}

	private void registerBuiltInTypes() {
		globalScope.setBooleanType(registerBuiltInType(GlobalScope.BOOLEAN_TYPE_NAME));
		globalScope.setIntType(registerBuiltInType(GlobalScope.INT_TYPE_NAME));
		globalScope.setStringType(registerBuiltInType(GlobalScope.STRING_TYPE_NAME));
	}

	private InbuiltTypeSymbol registerBuiltInType(String name) {
		var type = new InbuiltTypeSymbol(globalScope, name);
		globalScope.getTypes().add(type);
		globalScope.getBuiltInTypes().add(type);		
		return type;
	}

	private void registerBuiltInMethods() {
		globalScope.setHaltMethod(registerBuiltInMethod(null, GlobalScope.HALT_METHOD_NAME, globalScope.getStringType()));
		globalScope.setWriteIntMethod(registerBuiltInMethod(null, GlobalScope.WRITE_INT_NAME, globalScope.getIntType()));
		globalScope.setWriteStringMethod(registerBuiltInMethod(null, GlobalScope.WRITE_STRING_NAME, globalScope.getStringType()));
		globalScope.setReadIntMethod(registerBuiltInMethod(globalScope.getIntType(), GlobalScope.READ_INT_NAME, null));
		globalScope.setReadStringMethod(registerBuiltInMethod(globalScope.getStringType(), GlobalScope.READ_STRING_NAME, null));
	}

	private MethodSymbol registerBuiltInMethod(TypeSymbol returnType, String identifier, TypeSymbol parameterType) {
		var method = new MethodSymbol(globalScope, identifier);
		globalScope.getMethods().add(method);
		method.setReturnType(returnType);
		if (parameterType != null) {
			var parameter = new ParameterSymbol(method, "value");
			parameter.setType(parameterType);
			method.getParameters().add(parameter);
		}
		return method;
	}

	private void registerClass(ClassNode classNode) {
		var classSymbol = new ClassSymbol(globalScope, classNode.getIdentifier());
		globalScope.getClasses().add(classSymbol);
		globalScope.getTypes().add(classSymbol);
		symbolTable.linkDeclaration(classNode, classSymbol);
		for (var variableNode : classNode.getVariables()) {
			registerField(classSymbol, variableNode);
		}
		for (var methodNode : classNode.getMethods()) {
			registerMethod(classSymbol, methodNode);
		}
	}

	private void registerField(ClassSymbol classSymbol, VariableNode variableNode) {
		var fieldSymbol = new FieldSymbol(classSymbol, variableNode.getIdentifier());
		classSymbol.getFields().add(fieldSymbol);
		symbolTable.linkDeclaration(variableNode, fieldSymbol);
	}

	private void registerMethod(ClassSymbol classSymbol, MethodNode methodNode) {
		var methodSymbol = new MethodSymbol(classSymbol, methodNode.getIdentifier());
		classSymbol.getMethods().add(methodSymbol);
		symbolTable.linkDeclaration(methodNode, methodSymbol);
		for (var parameterNode : methodNode.getParameters()) {
			registerParameter(methodSymbol, parameterNode);
		}
		methodNode.getBody().accept(new LocalVariableVisitor(methodSymbol));
	}

	private void registerParameter(MethodSymbol methodSymbol, VariableNode parameterNode) {
		var parameterSymbol = new ParameterSymbol(methodSymbol, parameterNode.getIdentifier());
		methodSymbol.getParameters().add(parameterSymbol);
		symbolTable.linkDeclaration(parameterNode, parameterSymbol);
	}

	private class LocalVariableVisitor implements Visitor {
		private final MethodSymbol method;
		private Deque<Set<LocalSymbol>> stack = new LinkedList<>();

		public LocalVariableVisitor(MethodSymbol method) {
			this.method = method;
		}

		@Override
		public void visit(StatementBlockNode node) {
			stack.push(new HashSet<LocalSymbol>());
			Visitor.super.visit(node);
			stack.pop();
		}

		@Override
		public void visit(LocalDeclarationNode node) {
			var variable = node.getVariable();
			var localSymbol = new LocalSymbol(method, variable.getIdentifier());
			method.getLocals().add(localSymbol);
			symbolTable.linkDeclaration(variable, localSymbol);
			stack.peek().add(localSymbol);
		}

		@Override
		public void visit(AssignmentNode node) {
			record(node);
		}

		@Override
		public void visit(IfStatementNode node) {
			record(node);
			Visitor.super.visit(node);
		}

		@Override
		public void visit(WhileStatementNode node) {
			record(node);
			Visitor.super.visit(node);
		}

		@Override
		public void visit(CallStatementNode node) {
			record(node);
		}

		@Override
		public void visit(ReturnStatementNode node) {
			record(node);
		}

		private void record(StatementNode statement) {
			for (var scope : stack) {
				for (var localSymbol : scope) {
					localSymbol.getVisibleIn().add(statement);
				}
			}
		}
	}

	private void checkValidIdentifiers() {
		for (var classSymbol : globalScope.getClasses()) {
			checkValidIdentifier(classSymbol);
			for (var field : classSymbol.getFields()) {
				checkValidIdentifier(field);
			}
			for (var method : classSymbol.getMethods()) {
				checkValidIdentifier(method);
				for (var member : method.allDeclarations()) {
					if (member != method.getThisConstant()) {
						checkValidIdentifier(member);
					}
				}
			}
		}
	}

	private static final Set<String> RESERVED = Set.of(GlobalScope.BOOLEAN_TYPE_NAME, GlobalScope.FALSE_CONSTANT_NAME,
			GlobalScope.INT_TYPE_NAME, GlobalScope.STRING_TYPE_NAME, GlobalScope.THIS_CONSTANT_NAME,
			GlobalScope.TRUE_CONSTANT_NAME, GlobalScope.NULL_CONSTANT_NAME, GlobalScope.VOID_TYPE_NAME);

	private void checkValidIdentifier(Symbol symbol) {
		if (RESERVED.contains(symbol.getIdentifier())) {
			error(symbol, "Reserved keyword " + symbol + " used as an identifier");
		}
	}

	private void checkUniqueIdentifiers() {
		checkUniqueIdentifiers(globalScope);
		for (var classSymbol : globalScope.getClasses()) {
			checkUniqueIdentifiers(classSymbol);
			for (var method : classSymbol.getMethods()) {
				checkUniqueIdentifiers(method);
			}
		}
	}

	private void checkUniqueIdentifiers(Symbol scope) {
		for (var declaration : scope.allDeclarations()) {
			if (scope.allDeclarations().stream()
					.filter(symbol -> symbol.getIdentifier().equals(declaration.getIdentifier())).count() > 1) {
				error(declaration, "Identifier " + declaration + " is declared more than once in the scope");
			}
		}
	}

	private void resolveTypesInSymbols() {
		for (var classSymbol : symbolTable.getGlobalScope().getClasses()) {
			resolveTypesInSymbol(classSymbol);
		}
	}

	private void resolveTypesInSymbol(ClassSymbol classSymbol) {
		var classNode = (ClassNode) symbolTable.getDeclarationNode(classSymbol);
		if (classNode.getBaseClass() != null) {
			classSymbol.setBaseClass(resolveType(classNode.getBaseClass()));
		}
		for (var field : classSymbol.getFields()) {
			resolveTypeInSymbol(field);
		}
		for (var method : classSymbol.getMethods()) {
			resolveTypesInSymbol(method);
		}
	}

	private void resolveTypeInSymbol(VariableSymbol variable) {
		var variableNode = (VariableNode) symbolTable.getDeclarationNode(variable);
		variable.setType(resolveType(variableNode.getType()));
	}

	private void resolveTypesInSymbol(MethodSymbol method) {
		var methodNode = (MethodNode) symbolTable.getDeclarationNode(method);
		var returnType = methodNode.getReturnType();
		if (!(returnType instanceof BasicTypeNode
				&& ((BasicTypeNode) returnType).getIdentifier().equals(GlobalScope.VOID_TYPE_NAME))) {
			method.setReturnType(resolveType(returnType));
		}
		for (var parameter : method.getParameters()) {
			resolveTypeInSymbol(parameter);
		}
		for (var local : method.getLocals()) {
			resolveTypeInSymbol(local);
		}
	}

	private TypeSymbol resolveType(TypeNode node) {
		var result = symbolTable.findType(node);
		if (result == null) {
			error(node.getLocation(), "Undeclared type " + node);
		}
		return result;
	}

	private void checkInheritance() {
		for (var classSymbol : symbolTable.getGlobalScope().getClasses()) {
			checkInheritance(classSymbol);
		}
	}

	private void checkInheritance(ClassSymbol classSymbol) {
		checkAcyclic(classSymbol, new HashSet<>());
		checkOverrding(classSymbol);
	}

	private void checkOverrding(ClassSymbol classSymbol) {
		var baseClass = classSymbol.getBaseClass();
		if (baseClass != null) {
			for (var method : classSymbol.getMethods()) {
				var baseMethod = symbolTable.find(baseClass, method.getIdentifier());
				if (baseMethod instanceof MethodSymbol) {
					checkIdenticalSignatures(method, (MethodSymbol) baseMethod);
				} else if (baseMethod != null) {
					error(method, "Method " + method + " has same identifier than field in base class");
				}
			}
		}
	}

	private void checkIdenticalSignatures(MethodSymbol method, MethodSymbol baseMethod) {
		if (method.getReturnType() != baseMethod.getReturnType()) {
			error(method, "Overridden method " + method + " has different return type");
		}
		if (method.getParameters().size() != baseMethod.getParameters().size()) {
			error(method, "Overridden method " + method + " has different number of parameters");
		} else {
			for (int index = 0; index < method.getParameters().size(); index++) {
				if (method.getParameters().get(index).getType() != baseMethod.getParameters().get(index).getType()) {
					error(method,
							"Overridden method " + method + " has different types for parameter at position " + index);
				}
			}
		}
	}

	private void checkAcyclic(ClassSymbol classSymbol, Set<ClassSymbol> visited) {
		var baseClass = classSymbol.getBaseClass();
		if (baseClass != null) {
			if (baseClass instanceof ClassSymbol) {
				if (visited.contains(baseClass)) {
					error(baseClass, "Cyclic inheritance");
					classSymbol.setBaseClass(null);
				} else {
					visited.add(classSymbol);
					checkAcyclic((ClassSymbol) baseClass, visited);
				}
			} else {
				error(baseClass, "Invalid base class type");
			}
		}
	}

	private void checkMainMethod() {
		var entries = symbolTable.getGlobalScope().getClasses().stream().flatMap(symbol -> symbol.getMethods().stream())
				.filter(method -> method.getIdentifier().equals(GlobalScope.MAIN_METHOD_NAME))
				.collect(Collectors.toList());
		if (entries.size() == 0) {
			diagnostics.reportError("Main method missing");
		} else if (entries.size() > 1) {
			diagnostics.reportError("Only one Main() method is permitted");
		} else {
			var main = entries.get(0);
			symbolTable.getGlobalScope().setMainMethod(main);
			if (main.getParameters().size() > 0) {
				diagnostics.reportError("Main method must have no parameters");
			}
			if (main.getReturnType() != null) {
				diagnostics.reportError("Main method must be void");
			}
		}
	}

	private void error(Symbol symbol, String message) {
		var node = symbolTable.getDeclarationNode(symbol);
		var location = node == null ? new Location(0, 0) : node.getLocation();
		error(location, message);
	}

	private void error(Location location, String message) {
		diagnostics.reportError(message + " LOCATION " + location);
	}
}
