package edu.columbia.cs.psl.mountaindew.runtime;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

public class InterceptingMethodVisitor extends AdviceAdapter{
	private String name;
	private int api;
	private Label timeVarStart = new Label();
	private Label timeVarEnd = new Label();
	
	protected InterceptingMethodVisitor(int api, MethodVisitor mv, int access,
			String name, String desc) {
		super(api, mv, access, name, desc);
		this.name = name;
		this.api = api;
	}
	boolean rewrite = false;
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if(desc.equals("Ledu/columbia/cs/psl/mountaindew/runtime/annotation/MetamorphicInspected;"))
			rewrite = true;
		return null;
	}
	
	@Override
	protected void onMethodEnter() {
		if(!rewrite)
			return;

		
		super.onMethodEnter();
	}
	String vars = "vars - ";
	@Override
	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
		// TODO Auto-generated method stub
		super.visitLocalVariable(name, desc, signature, start, end, index);
		vars += name + "; ";
	}
	
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		visitLabel(timeVarEnd);
		super.visitMaxs(maxStack, maxLocals);
	}

	public void onMethodExit(int opcode) {
		if(!rewrite)
			return;
		visitLabel(timeVarStart);
		int timeVar = newLocal(Type.getType("J"));
		super.visitFieldInsn(GETSTATIC,
		        "java/lang/System", "err",
		       "Ljava/io/PrintStream;");
		    super.visitLdcInsn("Entering " +  name + vars);
		    super.visitMethodInsn(INVOKEVIRTUAL,
		        "java/io/PrintStream", "println",
		        "(Ljava/lang/String;)V");
		    
	     if(opcode==RETURN) {
	         visitInsn(ACONST_NULL);
	     } else if(opcode==ARETURN || opcode==ATHROW) {
	         dup();
	     } else {
	         if(opcode==LRETURN || opcode==DRETURN) {
	             dup2();
	         } else {
	             dup();
	         }
	         box(Type.getReturnType(this.methodDesc));
	     }
	     visitIntInsn(SIPUSH, opcode);
	     visitMethodInsn(INVOKESTATIC, "edu/columbia/cs/psl/mountaindew/runtime/Interceptor", "onExit", "(Ljava/lang/Object;I)V");
	   }
	private String className;
	public void setClassName(String className) {
		this.className = className;
	}

}
