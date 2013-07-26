package edu.columbia.cs.psl.mountaindew.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;


public class WekaAdapter implements AbstractAdapter{
	
	private MetamorphicInputProcessor processor;
	
	private List<Integer> skipList = new ArrayList<Integer>();
	
	private Instances testingData;
		
	public void setProcessor(MetamorphicInputProcessor processor) {
		this.processor = processor;
	}
	
	public MetamorphicInputProcessor getProcessor() {
		return this.processor;
	}
		
	public Object adaptInput(Object input, Object[] propertyParams) {
		Class objClazz = input.getClass();
		
		if (objClazz == Instances.class) {
			Instances inputInstances = (Instances)input;
			
			//Check Attribute, now we have no processor to process string
			//System.out.println("Attribute number: " + inputInstances.numAttributes());
			Attribute tmpAttribute;
			for (int i = 0; i < inputInstances.numAttributes(); i++) {
				tmpAttribute = inputInstances.attribute(i);
				
				System.out.println("Check attribute: " + tmpAttribute.name());
				System.out.println("Check attribute idx: " + tmpAttribute.type());
				
				if (!tmpAttribute.isNumeric())
					this.skipList.add(i);
			}
			
			int classifierIdx = inputInstances.classIndex();
		
			Enumeration e = inputInstances.enumerateInstances();
			
			Object tmpObj;
			Instance tmpInstance;
			double[] vals;
			double[] newVals;
			while (e.hasMoreElements()) {
				tmpObj = e.nextElement();				
				tmpInstance = (Instance)tmpObj;
				vals = tmpInstance.toDoubleArray();
				newVals = new double[vals.length];
				
				for (int i = 0; i < vals.length; i++) {
					if (this.shouldSkipped(i) || i == classifierIdx) {
						newVals[i] = vals[i];
					} else {
						newVals[i] = this.processor.apply(vals[i], propertyParams);
					}
				}
				tmpInstance.setMAttValues(newVals);	
			}
			
			/*System.out.println("Traverse new instances");
			e = inputInstances.enumerateInstances();
			while(e.hasMoreElements()) {
				tmpInstance = (Instance)e.nextElement();
				vals = tmpInstance.toDoubleArray();
				System.out.println("Check result: " + vals[0] + " " + vals[1] + " " + vals[2] + " " + vals[3] + " " + vals[4]);
			}*/
			return inputInstances;
		} else {
			return this.processor.apply(input, propertyParams);
		}
	}
	
	@Override
	public Object adaptOutput(Object output) {
		
		if (SMO.class.isAssignableFrom(output.getClass())) {
			SMO s = (SMO)output;
			try {
				Evaluation e = new Evaluation(this.testingData);
				e.evaluateModel(s, this.testingData);
				return adaptOutput(e);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (Evaluation.class.isAssignableFrom(output.getClass())) {
			Evaluation e = (Evaluation)output;
			return e.confusionMatrix();
		}
		return null;
	}
	
	private boolean shouldSkipped(int i) {
		return this.skipList.contains(i);
	}
	
	@Override
	public void setTestingData(Object testingData) {
		if (Instances.class.isAssignableFrom(testingData.getClass())) {
			this.testingData = (Instances)testingData;
		}
	}
}
