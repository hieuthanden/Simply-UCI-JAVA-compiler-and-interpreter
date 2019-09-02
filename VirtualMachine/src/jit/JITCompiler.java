package jit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.sun.jna.Platform;

import bytecode.Instruction;
import bytecode.OpCode;
import jit.x64.X64Assembler;
import jit.x64.X64Label;
import jit.x64.X64Register;
import runtime.descriptors.MethodDescriptor;
import static jit.x64.X64Register.*;
import static bytecode.OpCode.*;

public class JITCompiler {
	private static final List<X64Register> GENERAL_REGISTERS = List.of(RAX, RCX, RDX, RBX, R8, R9, R10, R11, R12, R13,
			R14, R15);
	private static final List<X64Register> CALLEE_SAVED = List.of(RBX, R12, R13, R14, R15);

	private static final Set<OpCode> BRANCH_INSTRUCTIONS = Set.of(GOTO, IF_TRUE, IF_FALSE);
	private static final Set<OpCode> UNCONDITIONAL_BRANCHES = Set.of(GOTO, RETURN);

	private final MethodDescriptor method;
	private final X64Assembler assembler = new X64Assembler();
	private final Allocation allocation;
	private final Map<X64Label, Allocation> branchState = new HashMap<>();
	private final Map<Instruction, X64Label> labels = new HashMap<>();

	public JITCompiler(MethodDescriptor method) {
		Objects.requireNonNull(method);
		this.method = method;
		if (!JITPrecondition.fulfilled(method)) {
			throw new AssertionError("Preconditions for JIT compilation not fulfilled");
		}
		var parameters = allocateParameters(method.getParameterTypes().length);
		var free = new HashSet<X64Register>(GENERAL_REGISTERS);
		free.removeAll(parameters);
		allocation = new Allocation(parameters, free);
		allocateLocals(method.getLocalTypes().length);
		createLabels(method.getCode());
		emitInstructions(method.getCode());
	}

	public byte[] getCode() {
		return assembler.getCode();
	}

	private void createLabels(Instruction[] instructions) {
		for (int position = 0; position < instructions.length; position++) {
			var current = instructions[position];
			if (BRANCH_INSTRUCTIONS.contains(current.getOpCode())) {
				var target = instructions[position + 1 + (int) current.getOperand()];
				if (!labels.containsKey(target)) {
					labels.put(target, assembler.createLabel());
				}
			}
		}
	}

	private void emitInstructions(Instruction[] code) {
		emitPrologue();
		for (int position = 0; position < code.length; position++) {
			alignBranchEntry(position);
			emitInstruction(position);
		}
	}

	private void emitInstruction(int position) {
		var code = method.getCode();
		var instruction = code[position];
		var opCode = instruction.getOpCode();
		var operand = instruction.getOperand();
		switch (opCode) {
		case LDC:
			emitConstant(operand);
			break;
		case BNEG:
			// TODO: Implement
			var bneg_operand = pop();
			assembler.NEG(bneg_operand);
			push(bneg_operand);
			break;
		case INEG:
			// TODO: Implement
			var ineg_operand = pop();
			assembler.NOT(ineg_operand);
			push(ineg_operand);
			break;
		case IADD:
			// TODO: Implement
			var add_operand2 = pop();
			var add_operand1 = pop();
			var add_result = acquire();
			assembler.MOV_RegReg(add_result, add_operand1);
			assembler.ADD_RegReg(add_result, add_operand2);
			release(add_operand1);
			release(add_operand2);
			push(add_result);
			break;

		case ISUB:
			// TODO: Implement
			var sub_operand2 = pop();
			var sub_operand1 = pop();
			var sub_result = acquire();
			assembler.MOV_RegReg(sub_result, sub_operand1);
			assembler.SUB_RegReg(sub_result, sub_operand2);
			release(sub_operand1);
			release(sub_operand2);
			push(sub_result);
			break;
		case IMUL:
			// TODO: Implement
			var mul_operand2 = pop();
			var mul_operand1 = pop();
			var mul_result = acquire();
			assembler.MOV_RegReg(mul_result, mul_operand1);
			assembler.IMUL_RegReg(mul_result, mul_operand2);
			release(mul_operand1);
			release(mul_operand2);
			push(mul_result);
			break;

		case IDIV:
			// TODO: Implement
			reserve(RAX);
			reserve(RDX);
			forceStack(1, RAX);
			var idiv_operand2 = pop();
			pop(); // is RAX
			assembler.CDQ();
			assembler.IDIV(idiv_operand2);
			push(RAX);
			release(idiv_operand2);
			release(RDX);
			break;

		case IREM:
			// TODO: Implement
			reserve(RAX);
			reserve(RDX);
			forceStack(1, RAX);
			var IREM_operand2 = pop();
			pop(); // is RAX
			assembler.CDQ();
			assembler.IDIV(IREM_operand2);
			push(RDX);
			release(IREM_operand2);
			release(RAX);
			
			break;
		case LOAD:
			// TODO: Implement
			var l_index = (int)instruction.getOperand();
			if (l_index <= allocation.getParameters().size()) {
				var reg = allocation.getParameters().get(l_index - 1);
				push(reg);
			}
			else {
				var reg = allocation.getLocals().get(l_index - 1 - (allocation.getParameters().size()));
				push(reg);
			}
			break;
		case STORE:
			// TODO: Implement
			var source = pop();
			var s_index = (int)instruction.getOperand();
			if (s_index <= allocation.getParameters().size()) {
				var target = allocation.getParameters().get(s_index - 1);
				assembler.MOV_RegReg(target, source);
			}
			else {
				var target = allocation.getLocals().get(s_index - 1 - (allocation.getParameters().size()));
				assembler.MOV_RegReg(target, source);
			}
			release(source);
			break;
		case CMPEQ:
			// TODO: Implement
			var CMPEQ_operand2 = pop();
			var CMPEQ_operand1 = pop();
			assembler.CMP_RegReg(CMPEQ_operand1, CMPEQ_operand2);
			release(CMPEQ_operand1);
			release(CMPEQ_operand2);
			break;
		case CMPNE:
			// TODO: Implement
			var CMPNE_operand2 = pop();
			var CMPNE_operand1 = pop();
			assembler.CMP_RegReg(CMPNE_operand1, CMPNE_operand2);
			release(CMPNE_operand1);
			release(CMPNE_operand2);
			break;
		case ICMPLT:
			// TODO: Implement
			var ICMPLT_operand2 = pop();
			var ICMPLT_operand1 = pop();
			assembler.CMP_RegReg(ICMPLT_operand1, ICMPLT_operand2);
			release(ICMPLT_operand1);
			release(ICMPLT_operand2);
			break;
		case ICMPLE:
			// TODO: Implement
			var ICMPLE_operand2 = pop();
			var ICMPLE_operand1 = pop();
			assembler.CMP_RegReg(ICMPLE_operand1, ICMPLE_operand2);
			release(ICMPLE_operand1);
			release(ICMPLE_operand2);
			break;
		case ICMPGT:
			// TODO: Implement
			var ICMPGT_operand2 = pop();
			var ICMPGT_operand1 = pop();
			assembler.CMP_RegReg(ICMPGT_operand1, ICMPGT_operand2);
			release(ICMPGT_operand1);
			release(ICMPGT_operand2);
			break;
		case ICMPGE:
			// TODO: Implement
			var ICMPGE_operand2 = pop();
			var ICMPGE_operand1 = pop();
			assembler.CMP_RegReg(ICMPGE_operand1, ICMPGE_operand2);
			release(ICMPGE_operand1);
			release(ICMPGE_operand2);
			break;
		case IF_FALSE:
			// TODO: Implement
			var f_offset = (int)instruction.getOperand();
			var f_target = code[position + 1 + f_offset];
			var f_label = labels.get(f_target);
			matchAllocation(f_label);
			if (code[position - 1].getOpCode() == CMPEQ) {
				assembler.JNE_Rel(f_label);;
			}
			else if (code[position - 1].getOpCode() == CMPNE) {
				assembler.JE_Rel(f_label);
			}
			else if (code[position - 1].getOpCode() == ICMPLT) {
				assembler.JGE_Rel(f_label);
			}
			else if (code[position - 1].getOpCode() == ICMPLE) {
				assembler.JG_Rel(f_label);
			}
			else if (code[position - 1].getOpCode() == ICMPGT) {
				assembler.JLE_Rel(f_label);
			}
			else if (code[position - 1].getOpCode() == ICMPGE) {
				assembler.JL_Rel(f_label);
			}
			break;
	
		case IF_TRUE:
			// TODO: Implement
			var t_offset = (int)instruction.getOperand();
			var t_target = code[position + 1 + t_offset];
			var t_label = labels.get(t_target);
			matchAllocation(t_label);
			if (code[position - 1].getOpCode() == CMPEQ) {
				assembler.JE_Rel(t_label);
			}
			else if (code[position - 1].getOpCode() == CMPNE) {
				assembler.JNE_Rel(t_label);
			}
			else if (code[position - 1].getOpCode() == ICMPLT) {
				assembler.JL_Rel(t_label);
			}
			else if (code[position - 1].getOpCode() == ICMPLE) {
				assembler.JLE_Rel(t_label);
			}
			else if (code[position - 1].getOpCode() == ICMPGT) {
				assembler.JG_Rel(t_label);
			}
			else if (code[position - 1].getOpCode() == ICMPGE) {
				assembler.JGE_Rel(t_label);
			}

			break;
		case GOTO:
			// TODO: Implement
			var go_offset = (int)instruction.getOperand();
			var go_target = code[position + 1 + go_offset];
			var go_label = labels.get(go_target);
			matchAllocation(go_label);
			assembler.JMP_Rel(go_label);
			break;
		case RETURN:
			emitReturn();
			break;
		default:
			throw new AssertionError("Unsupported instruction in JIT compiler");
		}
	}

	private void emitReturn() {
		if (method.getReturnType() != null && allocation.getEvaluation().size() == 1) {
			forceStack(0, RAX);
			if (allocation.getEvaluation().pop() != RAX) {
				throw new AssertionError("Return must be in RAX");
			}
			release(RAX);
		}
		if (allocation.getEvaluation().size() != 0) {
			throw new AssertionError("Register stack not empty");
		}
		emitEpilogue();
	}

	private void emitConstant(Object operand) {
		int value;
		if (operand instanceof Integer) {
			value = (int) operand;
		} else if (operand instanceof Boolean) {
			value = (boolean) operand ? 1 : 0;
		} else {
			throw new AssertionError("Unsupported LDC operand in JIT compiler");
		}
		var target = acquire();
		assembler.MOV_RegImm(target, value);
		push(target);
	}

	private void emitPrologue() {
		for (var register : CALLEE_SAVED) {
			assembler.PUSH(register);
		}
	}

	private void emitEpilogue() {
		var list = new ArrayList<>(CALLEE_SAVED);
		Collections.reverse(list);
		for (var register : list) {
			assembler.POP(register);
		}
		assembler.RET();
	}

	private void alignBranchEntry(int position) {
		var code = method.getCode();
		var current = code[position];
		if (labels.containsKey(current)) {
			var label = labels.get(current);
			assembler.setLabel(label);
			var previous = position > 0 ? code[position - 1] : null;
			if (previous == null || !UNCONDITIONAL_BRANCHES.contains(previous.getOpCode())) {
				matchAllocation(label);
			} else {
				resetAllocation(label);
			}
		}
	}

	private void matchAllocation(X64Label label) {
		if (!branchState.containsKey(label)) {
			branchState.put(label, allocation.clone());
		} else {
			realignAllocation(branchState.get(label));
		}
	}

	private void resetAllocation(X64Label label) {
		if (branchState.containsKey(label)) {
			var expected = branchState.get(label);
			expected.copyTo(allocation);
		}
	}

	private void realignAllocation(Allocation expected) {
		realignRegisters(allocation.getParameters(), expected.getParameters());
		realignRegisters(allocation.getLocals(), expected.getLocals());
		realignRegisters(allocation.getEvaluation(), expected.getEvaluation());
		if (!allocation.equals(expected)) {
			throw new AssertionError("Failed allocation alignment");
		}
		expected.copyTo(allocation);
	}

	private void realignRegisters(List<X64Register> actual, List<X64Register> expected) {
		if (actual.size() != expected.size()) {
			throw new AssertionError("Inconsistent number of registers");
		}
		for (int index = 0; index < actual.size(); index++) {
			var source = actual.get(index);
			var target = expected.get(index);
			if (source != target) {
				reserve(target);
				forceRegister(actual, index, target);
				release(source);
			}
		}
	}

	private void forceStack(int stackPos, X64Register reg) {
		forceRegister(allocation.getEvaluation(), stackPos, reg);
	}

	private void forceRegister(List<X64Register> list, int index, X64Register reg) {
		var current = list.get(index);
		if (current != reg) {
			assembler.MOV_RegReg(reg, current);
			list.set(index, reg);
		}
	}

	private X64Register acquire() {
		return allocation.acquire();
	}

	private void release(X64Register reg) {
		allocation.release(reg);
	}

	private void reserve(X64Register reg) {
		if (allocation.isFree(reg)) {
			allocation.acquireSpecific(reg);
		} else {
			var target = allocation.acquire();
			assembler.MOV_RegReg(target, reg);
			allocation.relocate(reg, target);
		}
	}

	private void push(X64Register register) {
		allocation.getEvaluation().push(register);
	}

	@SuppressWarnings("unused")
	private X64Register pop() {
		return allocation.getEvaluation().pop();
	}

	@SuppressWarnings("unused")
	private X64Register peek() {
		return allocation.getEvaluation().peek();
	}

	private List<X64Register> allocateParameters(int paramCount) {
		boolean isWindows = Platform.isWindows();
		var parameters = new ArrayList<X64Register>();
		if (paramCount > 0) {
			parameters.add(isWindows ? RCX : RDI);
		}
		if (paramCount > 1) {
			parameters.add(isWindows ? RDX : RSI);
		}
		if (paramCount > 2) {
			parameters.add(isWindows ? R8 : RDX);
		}
		if (paramCount > 3) {
			parameters.add(isWindows ? R9 : RCX);
		}
		if (paramCount > 4) {
			throw new AssertionError("Only four parameters currently supported");
		}
		return parameters;
	}

	private void allocateLocals(int localCount) {
		for (int index = 0; index < localCount; index++) {
			allocation.addLocal();
		}
	}
}
