package edu.columbia.cs.psl.mountaindew.adapter;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;

public class DefaultAdapter implements AbstractAdapter{
	
	private MetamorphicInputProcessor processor;
	
	private Object[] propertyParams;
	
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
	
	public Object adaptOutput(Object output) {
		return output;
	}

	

}
