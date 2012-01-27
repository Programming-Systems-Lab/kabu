package edu.columbia.cs.psl.mountaindew.runtime;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InterceptingAdaptor extends ClassVisitor{

	private String className;
	public InterceptingAdaptor(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
		// TODO Auto-generated constructor stub
	}

	@Override
	public MethodVisitor visitMethod(int acc, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(acc, name, desc, signature, exceptions);
		mv = new InterceptingMethodVisitor(Opcodes.ASM4, mv, acc, name, desc);
		((InterceptingMethodVisitor) mv).setClassName(className);
		return mv;
	}

	public void setClassName(String name) {
		this.className=name;
	}
}
