package edu.columbia.cs.psl.mountaindew.adapter;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;

public class DefaultAdapter implements AbstractAdapter{
	
	private MetamorphicInputProcessor processor;
		
	@Override
	public void setProcessor(MetamorphicInputProcessor processor) {
		this.processor = processor;
	}

	@Override
	public MetamorphicInputProcessor getProcessor() {
		// TODO Auto-generated method stub
		return this.processor;
	}
	
	@Override
	public Object adaptInput(Object input, Object[] propertyParams) {
		// TODO Auto-generated method stub
		return this.processor.apply(input, propertyParams);
	}
	
	@Override
	public Object adaptOutput(Object output) {
		return output;
	}
	
	@Override
	public void setTestingData(Object testingData) {
		//Default is to do nothing
	}
	

}
