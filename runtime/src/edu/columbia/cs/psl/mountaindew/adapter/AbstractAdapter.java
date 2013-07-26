package edu.columbia.cs.psl.mountaindew.adapter;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;

public interface AbstractAdapter {
	
	public void setProcessor(MetamorphicInputProcessor processor);
	
	public MetamorphicInputProcessor getProcessor();
		
	public Object adaptInput(Object input, Object[] propertyParams);
	
	public Object adaptOutput(Object output);
	
	public void setTestingData(Object testData);

}
