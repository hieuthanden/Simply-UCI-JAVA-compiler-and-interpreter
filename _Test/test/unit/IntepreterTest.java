package unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import static unit.TestHelper.*;

public class IntepreterTest {
	@Test
	public void testLoadStore() {
		var source = "class Test {" + 
				"  void main() {" + 
				"    int x;" +
				"    x = 1;" +
				"    x = x + 2;" +
				"    writeInt(x);" +
				"  }" + 
				"}" + 
				"";
		var expected = "3";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testArithmetics() {
		var source = "class Test {" + 
					"  void main() {" + 
					"    writeInt(-12 - 1 + +2 * 9 / 4 % 3);" + 
					"  }" + 
					"}";
		var expected = "-12";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}

	@Test
	public void testArray() {
		var source = "class Test {" + 
					"  void main() {" + 
					"    int[] array;" + 
					"    array = new int[10];" + 
					"    int index;" + 
					"    index = 0;" + 
					"    while (index < array.length) {" + 
					"      array[index] = index * index;" + 
					"      index = index + 1;" + "    }" + 
					"    index = 0;" + 
					"    while (index < array.length) {" + 
					"      writeInt(array[index]); writeString(\"\");" + 
					"      index = index + 1;" + "    }" + "  }" + "}";
		var expected = "0 " + 
					"1 " + 
					"4 " + 
					"9 " + 
					"16 " + 
					"25 " + 
					"36 " + 
					"49 " + 
					"64 " + 
					"81 ";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testComparison() {
		var source = "class Test {" + 
				"  void main() {" + 
				"    if (1 < 2) {" + 
				"      writeString(\"OK1\");" + 
				"    } else {" + 
				"      halt(\"Error 1\");" + 
				"    }" + 
				"    if (2 > 1) {" + 
				"      writeString(\"OK2\");" + 
				"    } else {" + 
				"      halt(\"Error 2\");" + 
				"    }" + 
				"    if (1 >= 2) {" + 
				"      halt(\"Error 3\");" + 
				"    }" + 
				"    if (1 <= 2) {" + 
				"      writeString(\"OK3\");" + 
				"    }" + 
				"    if (1 == 1) {" + 
				"      writeString(\"OK4\");" + 
				"    }" + 
				"    if (1 != 2) {" + 
				"      writeString(\"OK5\");" + 
				"    }" +  
				"  }" + 
				"}";
		var expected = "OK1 " + 
				"OK2 " + 
				"OK3 " + 
				"OK4 " + 
				"OK5 ";
		var output = compileAndRun(source);
		
		assertEquals(expected, output);
	}

	@Test
	public void testFunction() {
		var source = "class Test {" + 
				"  void main() {" + 
				"    int result;" + 
				"    result = div(12, 4);" + 
				"    writeInt(result);" + 
				"    writeString(\"\");" + 
				"    writeInt(div(div(100, 2), 2));" + 
				"  }" + 
				"  " + 
				"  int div(int x, int y) {" + 
				"    return x / y;" + 
				"  }" + 
				"}";
		var expected = "3 25";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testRecursion() {
		var source = "class Test {" + 
				"  void main() {" + 
				"    run(10);" + 
				"  }" + 
				"  " + 
				"  void run(int amount) {" + 
				"    if (amount > 0) {" + 
				"      writeInt(amount);" + 
				"      writeString(\"\");" + 
				"      run(amount - 1);" + 
				"    }" + 
				"  }" + 
				"}" + 
				"";
		var expected = "10 " + 
				"9 " + 
				"8 " + 
				"7 " + 
				"6 " + 
				"5 " + 
				"4 " + 
				"3 " + 
				"2 " + 
				"1 ";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
}
