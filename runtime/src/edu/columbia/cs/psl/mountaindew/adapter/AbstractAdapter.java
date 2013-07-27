package edu.columbia.cs.psl.mountaindew.adapter;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;

public abstract class AbstractAdapter {
	
	private MetamorphicInputProcessor processor;
	
	public void setProcessor(MetamorphicInputProcessor processor) {
		this.processor = processor;
	}
	
	public MetamorphicInputProcessor getProcessor() {
		return this.processor;
	}
		
	public abstract Object unboxInput(Object input);
	
	public abstract Object adaptInput(Object transInput);
	
	public abstract Object adaptOutput(Object output);
	
	public abstract void setTestingData(Object testData);
}
