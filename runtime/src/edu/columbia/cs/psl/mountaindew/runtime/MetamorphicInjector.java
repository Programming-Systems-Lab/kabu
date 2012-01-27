package edu.columbia.cs.psl.mountaindew.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import edu.columbia.cs.psl.mountaindew.example.SimpleExample;

public class MetamorphicInjector {

	public void go()
	{
		EnhancingClassLoader l = new EnhancingClassLoader();
		Thread.currentThread().setContextClassLoader(l);
		try {
			Class c = l.loadClass("edu.columbia.cs.psl.mountaindew.example.SimpleExample");
			String[] args = {"abc","def"};
			System.out.println(">>>Returned a class " + c.getName());
			c.getMethod("main", String[].class).invoke(null, (Object) args);
			
//			c.getMethod("go",String.class).invoke(c.newInstance(),"zzz");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		SimpleExample.main(args);
	}
	public static void main(String[] args) {
		new MetamorphicInjector().go();
	}
}

