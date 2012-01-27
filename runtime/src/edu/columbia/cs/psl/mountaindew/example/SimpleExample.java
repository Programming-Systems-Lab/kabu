package edu.columbia.cs.psl.mountaindew.example;

import edu.columbia.cs.psl.mountaindew.runtime.annotation.MetamorphicInspected;

public class SimpleExample {
	@MetamorphicInspected
	public String go(String in)
	{
		int foo = 10;
		int bar=200;
		
		System.out.println("I am a " + this.getClass().getName());
		return in.toLowerCase();
	}
	
	public static void main(String[] args) {
		System.out.println(Thread.currentThread().getContextClassLoader());
		System.out.println(new SimpleExample().go("abc"));
	}
}
