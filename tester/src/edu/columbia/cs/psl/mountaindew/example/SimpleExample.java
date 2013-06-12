package edu.columbia.cs.psl.mountaindew.example;

import java.util.ArrayList;
import java.util.Collections;

import weka.classifiers.functions.supportVector.PukTest;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.mountaindew.runtime.Interceptor;

@Metamorphic
public class SimpleExample extends AbstractExample {

	@Metamorphic
	public String go(String in,String in2, String[] in3)
	{
//		in ="3";
//		Interceptor.catchParam(in, in3);
		String foobar = "x";
		int foo = 10;
		int bar=200;
		

		return in.toLowerCase();
	}
	
	@Metamorphic
	public int timesThree(int input)
	{
		return input * 3;
	}
	
	@Metamorphic
	public double standardDeviation(ArrayList<Integer> in)
	{
		double r = 0;
		double mean = 0;
		for(int i : in)
			mean+=i;
		mean = mean / in.size();
		for(int i : in)
			r += Math.pow(i - mean, 2);
		r = r / in.size();
		r = Math.sqrt(r);
		return r;
	}
	
	@Metamorphic
	public ArrayList<Integer> sort(int[] in)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(Integer i : in)
		{
			result.add(i);
		}
		Collections.sort(result);
		
		return result;
	}
	
	@Metamorphic
	public int[] increArray(int[] in) {
		for (int i = 0 ; i < in.length; i++) {
			in[i]++;
		}
		return in;
	}
	public static void main(String[] args) {
		String[] barzzz = {"aa","bb"};
//		Interceptor.catchParam(args, args);
		SimpleExample ex = new SimpleExample();
//		System.out.println(ex.addThree(0));
//		System.out.println(ex.addThree(1));
//		System.out.println(ex.addThree(2,null));
		
//		System.out.println(ex.timesThree(2));
//		System.out.println(ex.timesThree(3));
//		System.out.println(ex.timesThree(4));
		
		System.out.println(ex.sort(new int[] {3,4,5}));
		System.out.println(ex.increArray(new int[] {7, 8, 9}));
//		System.out.println(ex.sort(new int[] {4,3,5}));
//		PukTest test = new PukTest("foo");
//		junit.textui.TestRunner.run(PukTest.suite());
//		System.out.println(new SimpleExample().go("abc","def",barzzz));
	}
}
