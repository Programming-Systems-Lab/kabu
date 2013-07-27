package edu.columbia.cs.psl.mountaindew.adapter;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;

public class DefaultAdapter extends AbstractAdapter{
		
	@Override
	public Object adaptInput(Object input) {
		// TODO Auto-generated method stub
		return input;
	}
	
	@Override
	public Object adaptOutput(Object output) {
		return output;
	}
	
	@Override
	public void setTestingData(Object testingData) {
		//Default is to do nothing
	}

	@Override
	public Object unboxInput(Object input) {
		// TODO Auto-generated method stub
		return input;
	}
}
