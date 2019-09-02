package unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import static unit.TestHelper.*;

public class ObjectOrientedTest {
	@Test
	public void testExternalVirtualCalls() {
		var source = "class Test {" + 
				"  void main() {" + 
				"    Vehicle v;" + 
				"    v = new Cabriolet();" + 
				"    v.drive();" + 
				"    v = new Car();" + 
				"    v.drive();" + 
				"    v = new Vehicle();" + 
				"    v.drive();" + 
				"  }" + 
				"}" + 
				"class Vehicle {" + 
				"  void drive() {" + 
				"    writeString(\"Vehicle drive\");" + 
				"  }" + 
				"}" + 
				"class Car extends Vehicle {" + 
				"  void drive() {" + 
				"    writeString(\"Car drive\");" + 
				"  }" + 
				"}" + 
				"class Cabriolet extends Car {" + 
				"  void drive() {" + 
				"    writeString(\"Cabriolet drive\");" + 
				"  }" + 
				"}";
		var expected = "Cabriolet drive Car drive Vehicle drive ";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testInternalVirtualCall() {
		var source = "class Test {" + 
				"  void main() {" + 
				"    Vehicle v;" + 
				"    v = new Car();" + 
				"    v.printVehicle();" + 
				"  }" + 
				"}" + 
				"class Vehicle {" + 
				"  void printVehicle() {" + 
				"    print();" + 
				"  }" + 
				"  void print() {}" + 
				"}" + 
				"class Car extends Vehicle {" + 
				"  void print() {" + 
				"    writeString(\"Car\");" + 
				"  }" + 
				"}";
		var expected = "Car ";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testExternalTypeCasts() {
		var source = "class Test {" + 
				"  void main() {" + 
				"    Vehicle v;" + 
				"    v = new Cabriolet();" + 
				"    if (v instanceof Cabriolet) {" + 
				"       Cabriolet c;" + 
				"       c = (Cabriolet)v;" + 
				"       writeString(\"OK1\");" + 
				"    }" + 
				"    if (v instanceof Car) {" + 
				"       writeString(\"OK2\");" + 
				"    }" + 
				"    if (v instanceof Vehicle) {" + 
				"       writeString(\"OK3\");" + 
				"    }" + 
				"  }" + 
				"}" + 
				"class Vehicle {}" + 
				"class Car extends Vehicle {}" + 
				"class Cabriolet extends Car {}";
		var expected = "OK1 OK2 OK3 ";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testInternalTypeCasts() {
		var source = "class Test {" + 
				"  void main() {" + 
				"    Vehicle v;" + 
				"    v = new Car();" + 
				"    v.check();" + 
				"  }" + 
				"}" + 
				"class Vehicle {" + 
				"  void check() {" + 
				"    if (this instanceof Car) {" + 
				"       writeString(\"OK\");" +
				"    }" +
				"  }" +
				"}" + 
				"class Car extends Vehicle {}";
		var expected = "OK ";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testFailingTypeCast() {
		var source = "class Test {" + 
					"  void main() {" + 
					"    Vehicle v;" + 
					"    Car c;" + 
					"    v = new Vehicle();" + 
					"    c = (Car)v;" + 
					"  }" + 
					"}" +
					"class Vehicle {}" + 
					"class Car extends Vehicle {}";
		var expected = "VM ERROR: Invalid cast ";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testFieldInheritance() {
		var source = "class Test {" + 
				"  void main() {" + 
				"    Car c;" + 
				"    c = new Car();" + 
				"    c.name = \"MyCar\";" + 
				"    c.model = 1234;" + 
				"    c.print();" + 
				"  }" + 
				"}" + 
				"class Vehicle {" + 
				"  string name;" + 
				"}" + 
				"class Car extends Vehicle {" + 
				"  int model;" + 
				"  void print() {" + 
				"    writeString(name);" + 
				"    writeInt(model);" + 
				"  }" + 
				"}";
		var expected = "MyCar 1234";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testOverriding() {
		var source = "class Test {" +
				"  void main() {" +
				"    Vehicle v;" +
				"    v = new Cabriolet();" +
				"    v.drive();" +
				"    v.park();" +
				"    v.honk();" +
				"    v = new Car();" +
				"    v.drive();" +
				"    v.park();" +
				"    v.honk();" +
				"    v = new Vehicle();" +
				"    v.drive();" +
				"    v.park();" +
				"    v.honk();" +
				"  }" +
				"}" +
				"class Vehicle {" +
				"  void drive() {" +
				"    writeString(\"Vehicle drive\");" +
				"  }" +
				"  void honk() {" +
				"    writeString(\"Vehicle Beep\");" +
				"  }" +
				"  void park() {" +
				"    writeString(\"Vehicle Park\");" +
				"  }" +
				"}" +
				"class Car extends Vehicle {" +
				"  void honk() {" +
				"    writeString(\"Car Beep\");" +
				"  }" +
				"  void park() {" +
				"    writeString(\"Car Park\");" +
				"  }" +
				"  void drive() {" +
				"    writeString(\"Car drive\");" +
				"  }" +
				"}" +
				"class Cabriolet extends Car {" +
				"  void park() {" +
				"    writeString(\"Cabriolet Park\");" +
				"  }" +
				"  void drive() {" +
				"    writeString(\"Cabriolet drive\");" +
				"  }" +
				"  void honk() {" +
				"    writeString(\"Cabriolet Beep\");" +
				"  }" +
				"}";
		var expected = "Cabriolet drive Cabriolet Park Cabriolet Beep Car drive Car Park Car Beep Vehicle drive Vehicle Park Vehicle Beep ";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
}
