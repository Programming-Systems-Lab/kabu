package edu.columbia.cs.psl.mountaindew.adapter;

import java.util.List;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;

public class DefaultAdapter extends AbstractAdapter{
		
	@Override
	public Object adaptInput(Object input) {
		// TODO Auto-generated method stub
		return input;
	}
	
	@Override
	public Object adaptOutput(Object outputModel, Object...testingData) {
		return outputModel;
	}
	
	@Override
	public Object unboxInput(Object input) {
		// TODO Auto-generated method stub
		return input;
	}

	@Override
	public List<Object> skipColumn(Object input) {
		// TODO Auto-generated method stub
		return null;
	}
}
