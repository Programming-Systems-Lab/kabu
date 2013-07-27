package edu.columbia.cs.psl.mountaindew.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;


public class WekaAdapter extends AbstractAdapter{
	
	private List<Integer> skipList = new ArrayList<Integer>();
	
	private Instances testingData;
	
	private double[][] complementMap;
	
	private String instancesName;
	
	private int capacity;
	
	private FastVector attrVector;
	
	private int classIdx;
	
	@Override
	public Object unboxInput(Object input) {
		Class objClazz = input.getClass();
		
		if (objClazz == Instances.class) {
			Instances inputInstances = (Instances)input;
			int instanceNum = inputInstances.numInstances();
			int attrNum = inputInstances.numAttributes();
			this.instancesName = inputInstances.relationName();
			this.capacity = inputInstances.numInstances();
			this.classIdx = inputInstances.classIndex();
			this.attrVector = new FastVector();
			double[][] instancesRep = new double[instanceNum][attrNum];
			this.complementMap = new double[instanceNum][attrNum];
			
			Attribute tmpAttribute;
			for (int i = 0; i < inputInstances.numAttributes(); i++) {
				tmpAttribute = inputInstances.attribute(i);
				attrVector.addElement(tmpAttribute);
				
				if (!tmpAttribute.isNumeric()) {
					this.skipList.add(i);
				}
			}
			this.skipList.add(inputInstances.classIndex());
			
			Enumeration e = inputInstances.enumerateInstances();
			Object tmpObj;
			Instance tmpInstance;
			double[] vals;
			double[] newVals;
			
			int count = 0;
			while(e.hasMoreElements()) {
				tmpObj = e.nextElement();
				tmpInstance = (Instance)tmpObj;
				vals = tmpInstance.toDoubleArray();
				newVals = new double[vals.length];
				
				for (int i = 0; i < vals.length; i++) {
					if (this.shouldSkipped(i)) {
						newVals[i] = 0;
						complementMap[count][i] = vals[i];
					} else {
						newVals[i] = vals[i];
					}
				}
				
				instancesRep[count++] = newVals; 
			}
			
			/*for (int i = 0;i < instanceNum; i++) {
				for (int j = 0; j < attrNum; j++) {
					System.out.println("Check i: " + i + " j: " + j + " " + instancesRep[i][j]);
				}
			}*/
			
			return instancesRep;
		} else {
			return input;
		}
	}
	
	@Override
	public Object adaptInput(Object transInputObj) {
		double[][] transInput = (double[][])transInputObj;
		//Put the non-numeric value back
		for (int i = 0; i < transInput.length; i++) {
			double[] row = transInput[i];
			for (int j = 0; j < row.length; j++) {
				System.out.println("Transinput " + i + " " + j + " " + transInput[i][j]);
				System.out.println("ComplementMap " + i + " " + j + " " + complementMap[i][j]);
				transInput[i][j] = transInput[i][j] + complementMap[i][j];
			}
		}
		
		for (int i = 0;i < transInput.length; i++) {
			double[] row = transInput[i];
			for (int j = 0; j < row.length; j++) {
				System.out.println("Check i: " + i + " j: " + j + " " + transInput[i][j]);
			}
		}
		
		Instances ret = new Instances(this.instancesName, this.attrVector, this.capacity);
		ret.setClassIndex(this.classIdx);
		Instance tmpInstance;
		for (int i = 0; i < transInput.length; i++) {
			tmpInstance = new Instance(transInput[i].length);
			tmpInstance.setMAttValues(transInput[i]);
			ret.add(tmpInstance);
		}
		
		return ret;
	}
	

			
	/*public Object adaptInput(Object input, Object[] propertyParams) {
		Class objClazz = input.getClass();
		
		if (objClazz == Instances.class) {
			Instances inputInstances = (Instances)input;
			
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
						newVals[i] = this.getProcessor().apply(vals[i], propertyParams);
					}
				}
				tmpInstance.setMAttValues(newVals);	
			}

			return inputInstances;
		} else {
			return this.getProcessor().apply(input, propertyParams);
		}
	}*/
	
	@Override
	public Object adaptOutput(Object output) {
		
		if (Classifier.class.isAssignableFrom(output.getClass())) {
			Classifier c = (Classifier)output;
			try {
				Evaluation e = new Evaluation(this.testingData);
				e.evaluateModel(c, this.testingData);
				return adaptOutput(e);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (Evaluation.class.isAssignableFrom(output.getClass())) {
			Evaluation e = (Evaluation)output;
			System.out.println("Check confusion matrix");
			double[][] confusionMatrix = e.confusionMatrix();
			for (int i = 0; i < confusionMatrix.length; i++) {
				for (int j = 0;j < confusionMatrix[i].length; j++) {
					System.out.println("Content " + i + " " + j + " " + confusionMatrix[i][j]);
				}	
			}
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
