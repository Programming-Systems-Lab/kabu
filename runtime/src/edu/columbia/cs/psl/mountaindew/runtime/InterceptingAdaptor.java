package edu.columbia.cs.psl.mountaindew.runtime;

import java.util.ArrayList;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;

import com.sun.xml.internal.ws.org.objectweb.asm.Type;

public class InterceptingAdaptor extends ClassVisitor{

	private String className;
	public InterceptingAdaptor(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
		// TODO Auto-generated constructor stub
	}
	
	boolean rewrite = false;
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if(desc.equals("Ledu/columbia/cs/psl/mountaindew/runtime/annotation/MetamorphicInspected;"))
		{
			rewrite = true;
		}
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int acc, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(acc, name, desc, signature, exceptions);
		if(rewrite)
			mv = new InterceptingMethodVisitor(Opcodes.ASM4, mv, acc, name, desc);
				((InterceptingMethodVisitor) mv).setClassName(className);
		return mv;
	}
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		// TODO Auto-generated method stub
		return super.visitField(access, name, desc, signature, value);
	}
@Override
public void visitEnd() {
	super.visitEnd();
	if(rewrite)
	{
		FieldNode fn = new FieldNode(Opcodes.ASM4, Opcodes.ACC_PRIVATE, "___interceptor__by_mountaindew", Type.getDescriptor(Interceptor.class), null, null);
		fn.accept(cv);
	}
}
	public void setClassName(String name) {
		this.className=name;
	}
}
