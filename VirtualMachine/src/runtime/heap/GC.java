package runtime.heap;

import java.util.ArrayList;
import java.util.List;
import runtime.descriptors.*;

import runtime.CallStack;

public class GC {
	private final Heap heap;
	@SuppressWarnings("unused")
	private final FreeList freeList;
	@SuppressWarnings("unused")
	private final CallStack stack;
	
	public GC(Heap heap, FreeList freeList, CallStack stack) {
		this.heap = heap;
		this.freeList = freeList;
		this.stack = stack;
	}

	public void collect() {
		// TODO: Homework Week 8: Implement
		mark();
		sweep();
	}
	
	private void mark() {
		for (var root : getRootSet(stack)) {  
			traverse(root);
			}
	}
	
	private void traverse(Pointer current) {
		if (current != null && !isMarked(current.getAddress() - 16)) {
			setMark(current.getAddress() - 16 );
			for (var next : getPointers(current)) {
				traverse(next);
			}
		}
	}
	
	private Iterable<Pointer> getPointers(Pointer current) {
		var list = new ArrayList<Pointer>();
		var descriptor = heap.getDescriptor(current);
		if (descriptor instanceof ClassDescriptor) {
			var fields = ((ClassDescriptor)descriptor).getAllFields();
			for (var index = 0; index < fields.length; index++) {
				if (isPointerType(fields[index].getType())) {
					var value = heap.readField(current, index);
					if(value!= null) {
						list.add((Pointer) value);
					}
				}
			}
		}
		else if (descriptor instanceof ArrayDescriptor) {
			var element = ((ArrayDescriptor)descriptor).getElementType();
			if (isPointerType(element)) {
				for (var index = 0; index <= heap.getArrayLength(current) - 1; index++) {
					var value = heap.readElement(current, index);
						if(value!= null) {
							list.add((Pointer) value);
						}
					}
				}
			}
		return list;
	}
	
	private boolean isPointerType(TypeDescriptor type) {
		if ( type instanceof ArrayDescriptor || type instanceof ClassDescriptor)
			return true;
		else
			return false;
	}
	
	
	private void sweep() {
		var current = heap.HEAP_START;
		while (current < 8 * 1024) {
			if (!isMarked(current)) {
				if (!freeList.isFree(current)) {
					freeList.add(current);
				}
			}	
			clearMark(current);
			current += heap.getBlockSize(current);
		}
	}
	
	@SuppressWarnings("unused")
	private Iterable<Pointer> getRootSet(CallStack callStack) {
		var list = new ArrayList<Pointer>();
		for (var frame : callStack) {
			collectPointers(frame.getParameters(), list);
			collectPointers(frame.getLocals(), list);
			collectPointers(frame.getEvaluationStack().toArray(), list);
			list.add(frame.getThisReference());
		}
		return list;
	}
	
	private void collectPointers(Object[] values, List<Pointer> list) {
		for (var value : values) {
			if (value instanceof Pointer) {
				list.add((Pointer) value);
			}
		}
	}

	@SuppressWarnings("unused")
	private void setMark(long block) {
		heap.writeLong64(block, heap.readLong64(block) | 0x8000000000000000L);
	}

	@SuppressWarnings("unused")
	private void clearMark(long block) {
		heap.writeLong64(block, heap.readLong64(block) & 0x7fffffffffffffffL);
	}

	@SuppressWarnings("unused")
	private boolean isMarked(long block) {
		return (heap.readLong64(block) & 0x8000000000000000L) != 0;
	}
}
