package edu.columbia.cs.psl.mountaindew.example;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.LogState;

@LogState
public class FakeInnerObj {
	
	private int i = 1;
	
	private String j =  "abc";
	
	@LogState
	public void buildClassifier(int test) {
		test = 6;
		i = 2;
		j = "cde";
	}

}
