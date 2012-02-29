package edu.columbia.cs.psl.mountaindew.runtime.visitor;

import java.util.ArrayList;
import java.util.Locale;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import edu.columbia.cs.psl.mountaindew.runtime.Interceptor;

public class InterceptingMethodVisitor extends AdviceAdapter{
	private String name;
	private int api;
	private Label timeVarStart = new Label();
	private Label timeVarEnd = new Label();
	public final static String INTERCEPTOR_FIELD_NAME = "___interceptor__by_mountaindew";
	public final static String INTERCEPTOR_CLASS_NAME = "edu/columbia/cs/psl/mountaindew/runtime/Interceptor";
	private Class myClass;
	private Type[] argumentTypes;
	
	protected InterceptingMethodVisitor(int api, MethodVisitor mv, int access,
			String name, String desc) {
		super(api, mv, access, name, desc);
		this.name = name;
		this.api = api;
		this.argumentTypes = Type.getArgumentTypes(desc);
	}
	boolean rewrite = false;
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if(desc.equals("Ledu/columbia/cs/psl/mountaindew/runtime/annotation/Metamorphic;"))
			rewrite = true;
		return null;
	}

	int refIdForInterceptor;
	@Override
	protected void onMethodEnter() {
		if(!rewrite)
			return;
		
		Label the_method = new Label();
		visitIntInsn(ALOAD, 0);
		super.visitFieldInsn(GETFIELD, className.replace(".", "/"), INTERCEPTOR_FIELD_NAME, "L"+Interceptor.class.getName().replace(".", "/")+";");
		super.visitJumpInsn(IFNONNULL, the_method);
		visitIntInsn(ALOAD, 0);
		visitTypeInsn(NEW, INTERCEPTOR_CLASS_NAME);
		dup();
		loadThis();
		visitMethodInsn(INVOKESPECIAL, INTERCEPTOR_CLASS_NAME, "<init>", "(Ljava/lang/Object;)V");
		super.visitFieldInsn(PUTFIELD, className.replace(".", "/"), INTERCEPTOR_FIELD_NAME, "L"+Interceptor.class.getName().replace(".", "/")+";");

		visitLabel(the_method);

		refIdForInterceptor = newLocal(Type.INT_TYPE);

		
		visitIntInsn(ALOAD, 0);
		super.visitFieldInsn(GETFIELD, className.replace(".", "/"), INTERCEPTOR_FIELD_NAME, "L"+Interceptor.class.getName().replace(".", "/")+";");
		visitLdcInsn(name);

		push(argumentTypes.length);
		newArray(Type.getType(String.class));
		for (int i = 0; i < argumentTypes.length; i++) {
			dup();
		    push(i);
		    if(argumentTypes[i].getSort() != Type.OBJECT && argumentTypes[i].getSort() != Type.ARRAY)
		    	visitLdcInsn(argumentTypes[i].getClassName());
		    else
		    	visitLdcInsn(argumentTypes[i].getInternalName().replace("/", "."));
		    box(Type.getType(String.class));
			arrayStore(Type.getType(String.class));
		}
		
		loadArgArray();
		invokeVirtual(Type.getType(Interceptor.class), Method.getMethod("int __onEnter (java.lang.String, java.lang.String[], java.lang.Object[])"));
		storeLocal(refIdForInterceptor);
		
		super.onMethodEnter();
	}
	

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		visitLabel(timeVarEnd);
		super.visitMaxs(maxStack, maxLocals);
	}
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
			super.visitFieldInsn(GETFIELD, className.replace(".", "/"), INTERCEPTOR_FIELD_NAME, "L"+Interceptor.class.getName().replace(".", "/")+";");
			swap();
	     visitIntInsn(SIPUSH, opcode);
			loadLocal(refIdForInterceptor);
	     visitMethodInsn(INVOKEVIRTUAL, INTERCEPTOR_CLASS_NAME, "onExit", "(Ljava/lang/Object;II)V");
	   }
	private String className;
	public void setClassName(String className) {
		this.className = className;
		try {
			myClass = getClass().getClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
