package generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import bytecode.BytecodeArray;
import bytecode.BytecodeAssembly;
import bytecode.BytecodeClass;
import bytecode.BytecodeField;
import bytecode.BytecodeMethod;
import bytecode.BytecodeType;
import bytecode.Instruction;
import checker.symbols.ArrayTypeSymbol;
import checker.symbols.ClassSymbol;
import checker.symbols.FieldSymbol;
import checker.symbols.InbuiltTypeSymbol;
import checker.symbols.MethodSymbol;
import checker.symbols.SymbolTable;
import checker.symbols.TypeSymbol;
import parser.tree.MethodNode;

public class Generator {
	private final SymbolTable symbolTable;
	private final Map<TypeSymbol, BytecodeType> typeMap = new HashMap<>();
	private final Map<MethodSymbol, BytecodeMethod> methodMap = new HashMap<>();
	private final Map<FieldSymbol, BytecodeField> fieldMap = new HashMap<>();

	private final BytecodeAssembly assembly = new BytecodeAssembly();

	public Generator(SymbolTable symbolTable) {
		Objects.requireNonNull(symbolTable);
		this.symbolTable = symbolTable;
		generateMetadata();
		generateCode();
	}

	public BytecodeAssembly getAssembly() {
		return assembly;
	}

	private void generateMetadata() {
		registerInbuiltMethods();
		var allTypes = symbolTable.getGlobalScope().getTypes();
		for (var type : allTypes) {
			registerType(type);
		}
		for (var type : allTypes) {
			fixType(type);
		}
		fixMainMethod();
	}

	private void registerInbuiltMethods() {
		var globalScope = symbolTable.getGlobalScope();
		methodMap.put(globalScope.getHaltMethod(), assembly.getHaltMethod());
		methodMap.put(globalScope.getWriteIntMethod(), assembly.getWriteIntMethod());
		methodMap.put(globalScope.getWriteStringMethod(), assembly.getWriteStringMethod());
		methodMap.put(globalScope.getReadIntMethod(), assembly.getReadIntMethod());
		methodMap.put(globalScope.getReadStringMethod(), assembly.getReadStringMethod());
	}

	private void registerType(TypeSymbol type) {
		if (type instanceof InbuiltTypeSymbol) {
			typeMap.put(type, mapInbuiltType(type));
		} else if (type instanceof ClassSymbol) {
			var classSymbol = (ClassSymbol) type;
			var bytecodeClass = new BytecodeClass(type.getIdentifier());
			assembly.getTypes().add(bytecodeClass);
			typeMap.put(type, bytecodeClass);
			for (var field : classSymbol.getFields()) {
				registerField(bytecodeClass, field);
			}
			for (var method : classSymbol.getMethods()) {
				registerMethod(bytecodeClass, method);
			}
		} else if (type instanceof ArrayTypeSymbol) {
			var bytecodeArray = new BytecodeArray(type.getIdentifier());
			assembly.getTypes().add(bytecodeArray);
			typeMap.put(type, bytecodeArray);
		}
	}

	private void registerField(BytecodeClass containingClass, FieldSymbol field) {
		var bytecodeField = new BytecodeField(containingClass, field.getIdentifier());
		containingClass.getFields().add(bytecodeField);
		fieldMap.put(field, bytecodeField);
	}

	private void registerMethod(BytecodeClass containingClass, MethodSymbol method) {
		var bytecodeMethod = new BytecodeMethod(containingClass, method.getIdentifier());
		assembly.getMethods().add(bytecodeMethod);
		methodMap.put(method, bytecodeMethod);
	}

	private void fixType(TypeSymbol type) {
		if (type instanceof ClassSymbol) {
			fixClassType((ClassSymbol) type);
		} else if (type instanceof ArrayTypeSymbol) {
			fixArrayType((ArrayTypeSymbol) type);
		}
	}

	private void fixClassType(ClassSymbol classSymbol) {
		var bytecodeClass = (BytecodeClass) typeMap.get(classSymbol);
		if (classSymbol.getBaseClass() != null) {
			bytecodeClass.setBaseType((BytecodeClass) typeMap.get(classSymbol.getBaseClass()));
		}
		for (var field : classSymbol.getFields()) {
			fixField(field);
		}
		for (var method : classSymbol.getMethods()) {
			bytecodeClass.getMethods().add(methodMap.get(method));
			fixMethod(method);
		}
	}

	private void fixField(FieldSymbol field) {
		var bytecodeField = fieldMap.get(field);
		bytecodeField.setType(typeMap.get(field.getType()));
	}

	private void fixArrayType(ArrayTypeSymbol arrayType) {
		var bytecodeArray = (BytecodeArray) typeMap.get(arrayType);
		bytecodeArray.setElementType(typeMap.get(arrayType.getElementType()));
	}

	private void fixMethod(MethodSymbol method) {
		var bytecodeMethod = methodMap.get(method);
		if (method.getReturnType() != null) {
			bytecodeMethod.setReturnType(typeMap.get(method.getReturnType()));
		}
		for (var parameter : method.getParameters()) {
			bytecodeMethod.getParameterTypes().add(typeMap.get(parameter.getType()));
		}
		for (var local : method.getLocals()) {
			bytecodeMethod.getLocalTypes().add(typeMap.get(local.getType()));
		}
	}

	private void fixMainMethod() {
		var mainMethod = symbolTable.getGlobalScope().getMainMethod();
		assembly.setMainMethod(methodMap.get(mainMethod));
	}

	private BytecodeType mapInbuiltType(TypeSymbol type) {
		var globalScope = symbolTable.getGlobalScope();
		if (type == globalScope.getBooleanType()) {
			return assembly.getBooleanType();
		}
		if (type == globalScope.getIntType()) {
			return assembly.getIntType();
		}
		if (type == globalScope.getStringType()) {
			return assembly.getStringType();
		}
		throw new AssertionError("Unsupported inbuilt type");
	}

	private void generateCode() {
		var globalScope = symbolTable.getGlobalScope();
		for (var type : globalScope.getTypes()) {
			if (type instanceof ClassSymbol) {
				for (var method : ((ClassSymbol) type).getMethods()) {
					generateCode(method);
				}
			}
		}
	}

	private void generateCode(MethodSymbol method) {
		var bytecodeMethod = methodMap.get(method);
		var methodNode = (MethodNode) symbolTable.getDeclarationNode(method);
		var code = bytecodeMethod.getCode();
		var assembler = new Assembler(code);
		methodNode.getBody().accept(new CodeGenerationVisitor(symbolTable, method, assembler));
		assembler.complete();
		fixOperands(code);
	}

	private void fixOperands(List<Instruction> code) {
		for (var instruction : code) {
			var operand = instruction.getOperand();
			if (operand instanceof TypeSymbol) {
				instruction.setOperand(typeMap.get((TypeSymbol) operand));
			} else if (operand instanceof MethodSymbol) {
				instruction.setOperand(methodMap.get((MethodSymbol) operand));
			} else if (operand instanceof FieldSymbol) {
				instruction.setOperand(fieldMap.get((FieldSymbol) operand));
			}
		}
	}
}
