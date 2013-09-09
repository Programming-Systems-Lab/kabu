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
	
	//private List<Integer> skipList = new ArrayList<Integer>();
	
	private double[][] complementMap;
	
	private String instancesName;
	
	private int capacity;
	
	private FastVector attrVector;
	
	private int classIdx;
	
	@Override
	public List<Object> skipColumn(Object input) {
		Class objClass = input.getClass();
		List<Object> skipList = new ArrayList<Object>();
		
		if (objClass == Instances.class) {
			Instances inputInstances = (Instances)input;
			
			Attribute tmpAttribute;
			
			for (int i = 0; i < inputInstances.numAttributes(); i++) {
				tmpAttribute = inputInstances.attribute(i);
				
				if (!tmpAttribute.isNumeric())
					skipList.add(i);
			}
			
			if (!skipList.contains(inputInstances.classIndex()))
				skipList.add(inputInstances.classIndex());
			
			return skipList;
		}
		return null;
	}
	
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
			
			/*Attribute tmpAttribute;
			for (int i = 0; i < inputInstances.numAttributes(); i++) {
				tmpAttribute = inputInstances.attribute(i);
				attrVector.addElement(tmpAttribute);
				
				if (!tmpAttribute.isNumeric()) {
					this.skipList.add(i);
				}
			}
			this.skipList.add(inputInstances.classIndex());*/
			
			Attribute tmpAttribute;
			for (int i = 0; i < inputInstances.numAttributes(); i++) {
				tmpAttribute = inputInstances.attribute(i);
				attrVector.addElement(tmpAttribute);
			}

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
					/*if (this.getSkipList().contains(i)) {
						complementMap[count][i] = vals[i];
					}*/
					newVals[i] = vals[i];
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
		/*for (int i = 0; i < transInput.length; i++) {
			double[] row = transInput[i];
			for (int j = 0; j < row.length; j++) {
				
				if (this.complementMap[i][j] != -1) {
					transInput[i][j] = complementMap[i][j];
				}
			}
		}*/
		
		Instances ret = new Instances(this.instancesName, this.attrVector, this.capacity);
		ret.setClassIndex(this.classIdx);
		Instance tmpInstance;
		for (int i = 0; i < transInput.length; i++) {
			tmpInstance = new Instance(transInput[i].length);
			tmpInstance.setMAttValues(transInput[i]);
			//tmpInstance = new Instance(transInput[i].length, transInput[i]);
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
	public Object adaptOutput(Object outputModel, Object...testingData) {
		
		Instances finalData;
		if (Classifier.class.isAssignableFrom(outputModel.getClass())) {
			Classifier c = (Classifier)outputModel;
			
			Object tmpObj = null;
			
			if (testingData.length != 0)
				tmpObj = testingData[0];
			
			if (Instances.class.isAssignableFrom(tmpObj.getClass()))
				finalData = (Instances)tmpObj;
			else
				finalData = (Instances)this.getDefaultTestingData();
			
			try {
				Evaluation e = new Evaluation(finalData);
				e.evaluateModel(c, finalData);
				return adaptOutput(e);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (Evaluation.class.isAssignableFrom(outputModel.getClass())) {
			Evaluation e = (Evaluation)outputModel;
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
	
	/*private boolean shouldSkipped(int i) {
		return this.skipList.contains(i);
	}*/
}
