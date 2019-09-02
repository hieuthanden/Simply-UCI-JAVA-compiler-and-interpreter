package parser.tree;

import java.util.List;
import common.Location;

public class ProgramNode extends Node {
	private final List<ClassNode> classes;

	public ProgramNode(Location location, List<ClassNode> classes) {
		super(location);
		this.classes = classes;
	}

	public List<ClassNode> getClasses() {
		return classes;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public String toString() {
		String text = "";
		for (int index = 0; index < classes.size(); index++) {
			text += classes.get(index);
			if (index < classes.size() - 1) {
				text += "\r\n\r\n";
			}
		}
		return text;
	}
}
