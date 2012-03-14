package edu.columbia.cs.psl.mountaindew.test;

import java.util.ArrayList;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.MultiplyByNumericConstant;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Reverse;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Shuffle;

public class ShuffleTester {
	public static void main(String[] args) {
//		ShuffleProcessor p = new ShuffleProcessor();
//		
		String[] test  = new String[] {"a","b","c","d","e","f","h","i","j","k","l"};
		int[] test2 = new int[] {1,2,3,4,5,6,7,8,9,0};

		ArrayList<String> test3 = new ArrayList<String>();
		test3.add("abc");
		test3.add("def");
		test3.add("ghi");
		test3.add("jkl");
		Integer[] test4 = new Integer[] {1,2,3,4,5,6};		
//		try{
//		String[] b = p.apply(a);
//		for(String zz : a)
//			System.out.println(zz);
//		for(String zz : b)
//			System.out.println(zz);
//		
//		int[] d = p.apply(c);
//
//		for(int zz : c)
//			System.out.println(zz);
//		for(int zz : d)
//			System.out.println(zz);
		
		MetamorphicInputProcessor p = new MultiplyByNumericConstant(-3);
//
//		System.out.println("pre");
//		for(String zz : test)
//			System.out.println(zz);
//		System.out.println("post");
//		for(String zz : p.apply(test))
//			System.out.println(zz);
//		
		System.out.println("pre");
		for(int zz : test2)
			System.out.println(zz);
		System.out.println("post");
		for(int zz : p.apply(test2))
			System.out.println(zz);
		
//		System.out.println("pre");
//		for(String zz : test3)
//			System.out.println(zz);
//		System.out.println("post");
//		for(String zz : p.apply(test3))
//			System.out.println(zz);
//		
		System.out.println("pre");
		for(int zz : test4)
			System.out.println(zz);
		System.out.println("post");
		for(int zz : p.apply(test4))
			System.out.println(zz);
//		System.out.println(p.apply(test));
	}
}
