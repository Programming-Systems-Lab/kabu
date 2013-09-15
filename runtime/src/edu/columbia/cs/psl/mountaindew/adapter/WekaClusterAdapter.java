package edu.columbia.cs.psl.mountaindew.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class WekaClusterAdapter extends AbstractAdapter{

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
					newVals[i] = vals[i];
				}
				
				instancesRep[count++] = newVals; 
			}
			
			return instancesRep;
		} else {
			return input;
		}
	}
	
	@Override
	public Object adaptInput(Object transInputObj) {		
		double[][] transInput = (double[][])transInputObj;
		
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
	
	@Override
	public Object adaptOutput(HashMap<String, Object>stateRecorder, Object outputModel, Object...testingData) {
		recordState(stateRecorder, outputModel);
		
		Instances finalData;
		if (Clusterer.class.isAssignableFrom(outputModel.getClass())) {
			Clusterer c = (Clusterer)outputModel;
			
			Object tmpObj = null;
			
			if (testingData.length != 0)
				tmpObj = testingData[0];
			
			if (Instances.class.isAssignableFrom(tmpObj.getClass()))
				finalData = (Instances)tmpObj;
			else
				finalData = (Instances)this.getDefaultTestingData();
			
			try {
				ClusterEvaluation e = new ClusterEvaluation();
				e.setClusterer(c);
				e.evaluateClusterer(finalData);
				return adaptOutput(stateRecorder, e);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (ClusterEvaluation.class.isAssignableFrom(outputModel.getClass())) {
			ClusterEvaluation e = (ClusterEvaluation)outputModel;
			
			System.out.println("Check cluster assignment");
			double[] clusterAssignment = e.getClusterAssignments();
			
			//Construct cluster array with data number
			int[] clusterWDataNumber = new int[e.getNumClusters()];
			for (int i = 0; i < clusterAssignment.length; i++) {
				System.out.println("Cluster assignment: " + i + " " + clusterAssignment[i]);
				clusterWDataNumber[(int)clusterAssignment[i]]++;
			}
			
			//Sort or the cluster idx might change
			Arrays.sort(clusterWDataNumber);
			System.out.println("Check data number in cluster in incremental sequence");
			for (int i = 0; i < clusterWDataNumber.length; i++) {
				System.out.println(clusterWDataNumber[i]);
			}
			
			return clusterWDataNumber;
		}
		return null;
	}
}
