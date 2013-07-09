package edu.columbia.cs.psl.mountaindew.example;

import org.apache.mahout.common.parameters.Parametered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DummyInterface {
	
	//Logger log = LoggerFactory.getLogger(Parametered.class);
	String tmp = "123";
	
	public void doSomething();
	
	public final class DummeyInner {
		public static void doSomething2() {
			System.out.println("Do some thing in inner class");
		}
	}
}



