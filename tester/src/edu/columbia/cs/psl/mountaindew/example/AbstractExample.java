package edu.columbia.cs.psl.mountaindew.example;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;


@Metamorphic
public abstract class AbstractExample {
	@Metamorphic
	public int addThree(int input, Object fake)
	{
		return input+3;
	}
	
}
