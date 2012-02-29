package edu.columbia.cs.psl.mountaindew.runtime.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import edu.columbia.cs.psl.mountaindew.runtime.Interceptor;

public class TaintClassVisitor extends ClassVisitor {
	private String className;
	public final static String TAINT_FIELD_NAME = "___taint__by_mountaindew";

	public TaintClassVisitor(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
	}
	
	@Override
	public MethodVisitor visitMethod(int acc, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(acc, name, desc, signature,
				exceptions);
		mv = new TaintMethodVisitor(Opcodes.ASM4, mv, acc, name, desc);
//		((TaintMethodVisitor) mv).setClassName(className);
		return mv;
	}


	@Override
	public void visitEnd() {
		super.visitEnd();
		
		FieldNode fn = new FieldNode(Opcodes.ASM4, Opcodes.ACC_PRIVATE,
				TaintClassVisitor.TAINT_FIELD_NAME,
				Type.BOOLEAN_TYPE.getDescriptor(), null, Boolean.FALSE); //TODO: abstract the interceptor type
		fn.accept(cv);
	}

	public void setClassName(String name) {
		this.className = name;
	}
}
