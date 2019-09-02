package generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import bytecode.Instruction;
import bytecode.OpCode;

public class Assembler {
	private final List<Instruction> code;

	public static class Label {
	}

	private final Map<Label, Integer> targets = new HashMap<>();

	public Assembler(List<Instruction> code) {
		Objects.requireNonNull(code);
		this.code = code;
	}

	public void complete() {
		fixLabels();
		emit(OpCode.RETURN);
	}

	public Label createLabel() {
		return new Label();
	}

	public void setLabel(Label label) {
		targets.put(label, code.size());
	}

	private void fixLabels() {
		for (int source = 0; source < code.size(); source++) {
			var instruction = code.get(source);
			var operand = instruction.getOperand();
			if (operand instanceof Label) {
				var target = targets.get((Label) operand);
				int offset = target - source - 1;
				instruction.setOperand(offset);
			}
		}
	}

	public void emit(OpCode opCode) {
		emit(opCode, null);
	}

	public void emit(OpCode opCode, Object operand) {
		code.add(new Instruction(opCode, operand));
	}
}
