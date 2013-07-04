package edu.columbia.cs.psl.mountaindew.example;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InstanceExample {
	
	private int set;
	
	public InstanceExample(int set) {
		this.set = set;
	}
	
	public int getSet() {
		return set;
	}

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// TODO Auto-generated method stub
		InstanceExample ie = new InstanceExample(2);
		System.out.println("Real instance get set: " + ie.getSet());
		
		Class tmpClass = Class.forName("edu.columbia.cs.psl.mountaindew.example.InstanceExample");
		Method m = tmpClass.getMethod("getSet");
		System.out.println("Reflect method: " + Integer.valueOf(m.invoke(ie).toString()));

	}

}
