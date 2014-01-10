package edu.columbia.cs.psl.mountaindew.example;

import java.util.List;
import java.util.ArrayList;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

import superstring.Superstring;

public class SuperstringExample {
	
	@Metamorphic
	public Superstring buildSuperstring(List<String> fragments) {
		Superstring ss = new Superstring();
		ss.solve(fragments);
		return ss;
	}

	public static void main(String[] args) {
		List<String> fragments = new ArrayList<String>();
		fragments.add("string");
		fragments.add("lost");
		fragments.add("the");
		fragments.add("hello");

		
		SuperstringExample se = new SuperstringExample();
		Superstring ss = se.buildSuperstring(fragments);
		System.out.println("Solution: " + ss.getSolution());
		/*for (int i = 0; i < 10; i++) {
			Superstring ss = se.buildSuperstring(fragments);
			System.out.println("Solution: " + ss.getSolution());
		}*/
		
		/*Superstring ss = se.buildSuperstring(fragments);
		System.out.println("Solution: " + ss.getSolution());*/
	}
}
