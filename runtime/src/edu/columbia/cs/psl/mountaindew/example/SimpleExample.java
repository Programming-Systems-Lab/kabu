package edu.columbia.cs.psl.mountaindew.example;

import edu.columbia.cs.psl.mountaindew.runtime.Interceptor;
import edu.columbia.cs.psl.mountaindew.runtime.annotation.MetamorphicInspected;

@MetamorphicInspected
public class SimpleExample {
	private Interceptor inter = new Interceptor();
	@MetamorphicInspected
	public String go(String in,String in2, String[] in3)
	{
		if(inter == null)
			inter = new Interceptor();
//		in ="3";
//		Interceptor.catchParam(in, in3);
		String foobar = "x";
		int foo = 10;
		int bar=200;
		

		return in.toLowerCase();
	}
	
	public static void main(String[] args) {
		String[] barzzz = {"aa","bb"};
//		Interceptor.catchParam(args, args);
		System.out.println(new SimpleExample().go("abc","def",barzzz));
	}
}
