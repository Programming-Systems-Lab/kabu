package edu.columbia.cs.psl.mountaindew.runtime.visitor;

import java.util.ArrayList;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import edu.columbia.cs.psl.mountaindew.runtime.Interceptor;


public class InterceptingClassVisitor extends ClassVisitor {

	private String className;
	
	public static String IS_CHILD_FIELD = "__metamorphicIsChild";

	public InterceptingClassVisitor(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
	}

	@Override
	public MethodVisitor visitMethod(int acc, String name, String desc,
			String signature, String[] exceptions) {
//		super.visitMethod(acc, name, desc, signature, exceptions);
		
		MethodVisitor mv = cv.visitMethod(acc, name, desc, signature,
				exceptions);
		mv = new InterceptingMethodVisitor(Opcodes.ASM4, mv, acc, name, desc);
		((InterceptingMethodVisitor) mv).setClassName(className);
		return mv;
	}


	@Override
	public void visitEnd() {

		super.visitEnd();

		FieldNode fn = new FieldNode(Opcodes.ASM4, Opcodes.ACC_PRIVATE,
				InterceptingMethodVisitor.INTERCEPTOR_FIELD_NAME,
				Type.getDescriptor(Interceptor.class), null, null); //TODO: abstract the interceptor type
		fn.accept(cv);
		
		
		FieldNode fn2 = new FieldNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC,
				IS_CHILD_FIELD,
				Type.BOOLEAN_TYPE.getDescriptor(), null, false); //TODO: abstract the interceptor type
		fn2.accept(cv);
		
	}

	public void setClassName(String name) {
		this.className = name;
	}
}
