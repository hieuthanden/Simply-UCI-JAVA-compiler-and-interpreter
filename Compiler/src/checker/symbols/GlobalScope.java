package checker.symbols;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GlobalScope extends Symbol {
	public static final String VOID_TYPE_NAME = "void";
	public static final String INT_TYPE_NAME = "int";
	public static final String BOOLEAN_TYPE_NAME = "boolean";
	public static final String STRING_TYPE_NAME = "string";
	public static final String MAIN_METHOD_NAME = "main";
	public static final String FALSE_CONSTANT_NAME = "false";
	public static final String TRUE_CONSTANT_NAME = "true";
	public static final String NULL_CONSTANT_NAME = "null";
	public static final String THIS_CONSTANT_NAME = "this";
	public static final String ARRAY_LENGTH_NAME = "length";
	public static final String HALT_METHOD_NAME = "halt";
	public static final String WRITE_INT_NAME = "writeInt";
	public static final String WRITE_STRING_NAME = "writeString";
	public static final String READ_INT_NAME = "readInt";
	public static final String READ_STRING_NAME = "readString";

	private final List<ClassSymbol> classes = new ArrayList<>();
	private final List<TypeSymbol> types = new ArrayList<>();
	private final List<MethodSymbol> methods = new ArrayList<>();
	private final List<ConstantSymbol> constants = new ArrayList<>();

	private final TypeSymbol nullType = new InbuiltTypeSymbol(this, "@NULL");
	private TypeSymbol booleanType;
	private TypeSymbol intType;
	private TypeSymbol stringType;

	private final List<TypeSymbol> builtInTypes = new ArrayList<>();

	private ConstantSymbol trueConstant;
	private ConstantSymbol falseConstant;
	private ConstantSymbol nullConstant;

	private MethodSymbol haltMethod;
	private MethodSymbol writeIntMethod;
	private MethodSymbol writeStringMethod;
	private MethodSymbol readIntMethod;
	private MethodSymbol readStringMethod;

	private FieldSymbol arrayLength;
	private MethodSymbol mainMethod;

	public GlobalScope() {
		super(null, null);
	}

	public TypeSymbol getBooleanType() {
		return booleanType;
	}

	public void setBooleanType(TypeSymbol booleanType) {
		this.booleanType = booleanType;
	}

	public TypeSymbol getIntType() {
		return intType;
	}

	public void setIntType(TypeSymbol intType) {
		this.intType = intType;
	}

	public TypeSymbol getStringType() {
		return stringType;
	}

	public void setStringType(TypeSymbol stringType) {
		this.stringType = stringType;
	}

	public List<TypeSymbol> getBuiltInTypes() {
		return builtInTypes;
	}

	public ConstantSymbol getTrueConstant() {
		return trueConstant;
	}

	public void setTrueConstant(ConstantSymbol trueConstant) {
		this.trueConstant = trueConstant;
	}

	public ConstantSymbol getFalseConstant() {
		return falseConstant;
	}

	public void setFalseConstant(ConstantSymbol falseConstant) {
		this.falseConstant = falseConstant;
	}

	public ConstantSymbol getNullConstant() {
		return nullConstant;
	}

	public void setNullConstant(ConstantSymbol nullConstant) {
		this.nullConstant = nullConstant;
	}

	public MethodSymbol getHaltMethod() {
		return haltMethod;
	}

	public void setHaltMethod(MethodSymbol haltMethod) {
		this.haltMethod = haltMethod;
	}

	public MethodSymbol getWriteIntMethod() {
		return writeIntMethod;
	}

	public void setWriteIntMethod(MethodSymbol writeIntMethod) {
		this.writeIntMethod = writeIntMethod;
	}

	public MethodSymbol getWriteStringMethod() {
		return writeStringMethod;
	}

	public void setWriteStringMethod(MethodSymbol writeStringMethod) {
		this.writeStringMethod = writeStringMethod;
	}

	public MethodSymbol getReadIntMethod() {
		return readIntMethod;
	}

	public void setReadIntMethod(MethodSymbol readIntMethod) {
		this.readIntMethod = readIntMethod;
	}

	public MethodSymbol getReadStringMethod() {
		return readStringMethod;
	}

	public void setReadStringMethod(MethodSymbol readStringMethod) {
		this.readStringMethod = readStringMethod;
	}

	public FieldSymbol getArrayLength() {
		return arrayLength;
	}

	public void setArrayLength(FieldSymbol arrayLength) {
		this.arrayLength = arrayLength;
	}

	public MethodSymbol getMainMethod() {
		return mainMethod;
	}

	public void setMainMethod(MethodSymbol mainMethod) {
		this.mainMethod = mainMethod;
	}

	public List<ClassSymbol> getClasses() {
		return classes;
	}

	public List<TypeSymbol> getTypes() {
		return types;
	}

	public List<MethodSymbol> getMethods() {
		return methods;
	}

	public List<ConstantSymbol> getConstants() {
		return constants;
	}

	public TypeSymbol getNullType() {
		return nullType;
	}

	@Override
	public List<Symbol> allDeclarations() {
		var declarations = new ArrayList<Symbol>();
		declarations.addAll(types);
		declarations.addAll(methods);
		declarations.addAll(constants);
		return declarations;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		var other = (GlobalScope) obj;
		return super.equals(other) && Objects.equals(classes, other.classes) && Objects.equals(types, other.types)
				&& Objects.equals(methods, other.methods) && Objects.equals(constants, other.constants)
				&& Objects.equals(builtInTypes, other.builtInTypes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), classes, types, methods, constants, builtInTypes);
	}

	@Override
	public String toString() {
		return "TYPES: " + types + " METHODS: " + methods + " CONSTANTS: " + constants;
	}
}
