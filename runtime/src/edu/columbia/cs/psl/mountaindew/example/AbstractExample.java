package edu.columbia.cs.psl.mountaindew.example;

import edu.columbia.cs.psl.mountaindew.runtime.annotation.Metamorphic;

@Metamorphic
public abstract class AbstractExample {
	@Metamorphic
	public int addThree(int input)
	{
		return input+3;
	}
	
}
