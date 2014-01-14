package edu.columbia.cs.psl.mountaindew.example;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.sun.xml.internal.ws.org.objectweb.asm.Type;

public class MyObject2 {
	
	public int returnInt(int abc) {
		return abc;
	}
	
	public int returnInt() {
		return 1;
	}
	
	public int[] returnIntArray() {
		return new int[3];
	}
	
	public Integer returnInteger(Integer a, Integer b) {
		return a;
	}
	
	public ArrayList returnList() {
		return new ArrayList();
	}
	
	public int[] returnArray() {
		return new int[3];
	}
	
	public Object[] toArray(Object[] array) {
		return new Object[3];
	}
	
	public Object[] toArray() {
		return new Object[3];
	}
	
	public Object[] testToArray(List items) {
		return new Object[3];
	}
	
	public List testToList(Object[] tmp) {
		return new ArrayList();
	}
	
	public void doNothing() {
		
	}
	
	public double returnDouble() {
		return 2.0;
	}
	
	public static void main(String args[]) {
		MyObject2 mo = new MyObject2();
		System.out.println("Class name: " + mo.getClass().getName());
		try {
			Method[] ms = mo.getClass().getMethods();
			
			for (Method m: ms) {
				String mD = Type.getMethodDescriptor(m);
				System.out.println("Chec method descriptor: " + mD);
				System.out.println("Method name: " + m.getName());
				System.out.println("Method modifier: " + m.getModifiers());
				System.out.println("Returns: " + m.getReturnType().getName());
				System.out.println("Returns from ASM: " + Type.getDescriptor(m.getReturnType()));
				
				StringBuilder sb = new StringBuilder();
				sb.append(mo.getClass().getName() + ":");
				sb.append(m.getName() + ":");
				sb.append(m.getReturnType().getName() + ":");
				
				Class[] params = m.getParameterTypes();
				
				for (Class c: params) {
					System.out.println("Check param: " + Type.getDescriptor(c));
					if (!c.isArray()) {
						sb.append("L" + c.getName() + ";");
					} else {
						sb.append(c.getName());
					}
				}
				sb.append(":");
				sb.append(m.getModifiers());
				System.out.println("Full name: " + sb.toString());
			}
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
