package edu.columbia.cs.psl.mountaindew.runtime;

import java.util.ArrayList;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

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
		{
			rewrite = true;
			if(parameters == null)
				parameters=new ArrayList<InterceptingMethodVisitor.Variable>();
			else
				System.err.println("Reused params?");
		}
		return null;
	}
	
	@Override
	protected void onMethodEnter() {
		if(!rewrite)
			return;
		
		Label the_method = new Label();
		visitIntInsn(ALOAD, 0);
		super.visitFieldInsn(GETFIELD, className.replace(".", "/"), "___interceptor__by_mountaindew", "L"+Interceptor.class.getName().replace(".", "/")+";");
		super.visitJumpInsn(IFNONNULL, the_method);
		visitIntInsn(ALOAD, 0);
		visitTypeInsn(NEW, "edu/columbia/cs/psl/mountaindew/runtime/Interceptor");
		dup();
		visitMethodInsn(INVOKESPECIAL, "edu/columbia/cs/psl/mountaindew/runtime/Interceptor", "<init>", "()V");
		super.visitFieldInsn(PUTFIELD, className.replace(".", "/"), "___interceptor__by_mountaindew", "L"+Interceptor.class.getName().replace(".", "/")+";");

		visitLabel(the_method);
		
		for(int i = 0;i<firstLocal;i++)
		{
			visitIntInsn(ALOAD, 0);
			super.visitFieldInsn(GETFIELD, className.replace(".", "/"), "___interceptor__by_mountaindew", "L"+Interceptor.class.getName().replace(".", "/")+";");
			visitLdcInsn(i);
			visitIntInsn(ALOAD, i);
//			loadArgArray();
			invokeVirtual(Type.getType(Interceptor.class), Method.getMethod("void catchParam (int,java.lang.Object)"));
		}
		super.onMethodEnter();
	}
	

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		visitLabel(timeVarEnd);
		super.visitMaxs(maxStack, maxLocals);
	}
	private ArrayList<Variable> parameters;
	public void onMethodExit(int opcode) {
		if(!rewrite)
			return;


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
	     visitIntInsn(ALOAD, 0);
			super.visitFieldInsn(GETFIELD, className.replace(".", "/"), "___interceptor__by_mountaindew", "L"+Interceptor.class.getName().replace(".", "/")+";");
			swap();
	     visitIntInsn(SIPUSH, opcode);
	     
	     visitMethodInsn(INVOKEVIRTUAL, "edu/columbia/cs/psl/mountaindew/runtime/Interceptor", "onExit", "(Ljava/lang/Object;I)V");
	   }
	private String className;
	public void setClassName(String className) {
		this.className = className;
	}
	class Variable
	{
		String name;
		String desc;
		int offset;
		int index;
	}
}
