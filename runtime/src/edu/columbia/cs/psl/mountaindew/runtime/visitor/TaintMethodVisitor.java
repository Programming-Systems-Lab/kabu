package edu.columbia.cs.psl.mountaindew.runtime.visitor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class TaintMethodVisitor extends AdviceAdapter {
	private Class myClass;

	protected TaintMethodVisitor(int api, MethodVisitor mv, int access,
			String name, String desc) {
		super(api, mv, access, name, desc);
	}
	
	private String className;

}
