package unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import static unit.TestHelper.*;

public class GarbageCollectionTest {
	@Test
	public void testListRecycling() {
		var source = "class List {" + 
				"    Item list;" + 
				"    void main() {" + 
				"        list = new Item();" + 
				"        int i;" + 
				"        i = 1;" + 
				"        while (i < 10000) {" + 
				"            list.next = new Item();" + 
				"            i = i + 1;" + 
				"        }" + 
				"        writeString(\"list done\");" + 
				"    }" + 
				"}" + 
				"class Item { Item next; }";
		var expected = "list done ";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testListOutOfMemory() {
		var source = "class List {" + 
				"    Item head;" + 
				"    Item tail;" + 
				"    void main() {" + 
				"        head = new Item();" + 
				"        tail = head;" + 
				"        int i;" + 
				"        i = 1;" + 
				"        while (i < 10000) {" + 
				"		    Item item;" + 
				"			item = new Item();" + 
				"            tail.next = item;" + 
				"            tail = item;" + 
				"            i = i + 1;" + 
				"        }" + 
				"        writeString(\"list done\");" + 
				"    }" + 
				"}" + 
				"class Item { Item next; }";
		var expected = "VM ERROR: Out of memory ";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testArrayRecycling() {
		var source = "class ArrayRecycling {" + 
					"    Item[] array;" +
					"    void main()  {" + 
					"	    int r;" + 
					"		r = 0;" + 
					"		while (r < 1000) {" + 
					"			array = new Item[10];" + 
					"			int i;" + 
					"			i = 0;" + 
					"			while (i < array.length) {" + 
					"				array[i] = new Item();" + 
					"				array[i].sub = new Item[10];" + 
					"				int j;" + 
					"				j = 0;" + 
					"				while (j < array[i].sub.length)  {" + 
					"				   array[i].sub[j] = new Item();" + 
					"				   j = j + 1;" + 
					"				}" + 
					"				i = i + 1;" + 
					"			}" + 
					"			r = r + 1;" + 
					"		}" + 
					"        writeString(\"array done\");" + 
					"    }" + 
					"}" + 
					"class Item { Item[] sub; }";
		var expected = "array done ";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
	
	@Test
	public void testArrayOutOfMemory() {
		var source = "class ArrayOutOfMemory2 {" + 
				"    Vector[] array;" +
				"    void main() {" + 
				"		int size;" + 
				"		size = 1000;" + 
				"		array = new Vector[size];" + 
				"		int i;" + 
				"		int j;" + 
				"		i = 0;" + 
				"		while (i < array.length) {" + 
				"			array[i] = new Vector();" + 
				"			array[i].values = new int[size];" + 
				"			j = 0;" + 
				"			while (j < array[i].values.length) {" + 
				"			   array[i].values[j] = i + j;" + 
				"			   j = j + 1;" + 
				"			}" + 
				"			i = i + 1;" + 
				"		}" + 
				"		i = 0;" + 
				"		while (i < array.length) {" + 
				"			j = 0;" + 
				"			while (j < array[i].values.length) {" + 
				"				if (array[i].values[j] != i + j) {" + 
				"					halt(\"data corrupt\");" + 
				"				}" + 
				"			   j = j + 1;" + 
				"			}" + 
				"			i = i + 1;" + 
				"		}" + 
				"       writeString(\"array done\");" + 
				"    }" + 
				"}" + 
				"class Vector { int[] values; }";
		var expected = "VM ERROR: Out of memory ";
		var output = compileAndRun(source);
		assertEquals(expected, output);
	}
}
