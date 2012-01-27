package edu.columbia.cs.psl.mountaindew.runtime;

import java.lang.reflect.Method;

public class Interceptor{
   
	public static void catchParam(Object name, Object val)
	{
		System.out.println("Local var: <" + name+"> =" + val);
	}
	
	public static void onExit(Object val, int op)
	{
		System.out.println("On exit: <" + val+"> " + op);
	}
} 

