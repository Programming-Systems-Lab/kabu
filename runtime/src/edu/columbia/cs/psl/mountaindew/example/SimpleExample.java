package edu.columbia.cs.psl.mountaindew.example;

import java.util.ArrayList;
import java.util.Collections;

import edu.columbia.cs.psl.mountaindew.runtime.Interceptor;
import edu.columbia.cs.psl.mountaindew.runtime.annotation.Metamorphic;

@Metamorphic
public class SimpleExample {

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
	public int addThree(int input)
	{
		return input+3;
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
	public static void main(String[] args) {
		String[] barzzz = {"aa","bb"};
//		Interceptor.catchParam(args, args);
		SimpleExample ex = new SimpleExample();
		System.out.println(ex.addThree(0));
		System.out.println(ex.addThree(1));
		System.out.println(ex.addThree(2));
		System.out.println(ex.sort(new int[] {3,4,5}));
		System.out.println(ex.sort(new int[] {4,3,5}));

//		System.out.println(new SimpleExample().go("abc","def",barzzz));
	}
}
