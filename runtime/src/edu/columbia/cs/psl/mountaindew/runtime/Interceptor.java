package edu.columbia.cs.psl.mountaindew.runtime;


public class Interceptor{

	public void catchParam(int name, Object val)
	{
		System.out.println("Local var: <" + name+">=" + val);
	}
	
	public void onExit(Object val, int op)
	{
		System.out.println("On exit: <" + val+"> " + op);
	}
} 

