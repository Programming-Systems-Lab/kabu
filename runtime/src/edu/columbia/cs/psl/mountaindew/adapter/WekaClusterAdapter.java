package edu.columbia.cs.psl.mountaindew.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.InstanceComparator;
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
			
			System.out.println("For testing purpose:");
			for (int i = 0; i < clusterAssignment.length; i++) {
				System.out.println(clusterAssignment[i]);
			}
			
			//Construct a map to record which cluster contains which data
			HashMap<Integer, ArrayList<Integer>> clusterMap = new HashMap<Integer, ArrayList<Integer>>();
			
			int tmpClusterAssignment;
			for (int i = 0; i < clusterAssignment.length; i++) {
				tmpClusterAssignment = (int)clusterAssignment[i];
				if (!clusterMap.keySet().contains(tmpClusterAssignment)) {
					ArrayList<Integer> tmpClusterList = new ArrayList<Integer>();
					tmpClusterList.add(i);
					
					clusterMap.put(tmpClusterAssignment, tmpClusterList);
				} else {
					clusterMap.get(tmpClusterAssignment).add(i);
				}
			}
			
			//Sort the list in clusterMap by data number in the list
			ArrayList<ArrayList<Integer>> clusterRankList = new ArrayList<ArrayList<Integer>>();
			for (Integer tmpKey: clusterMap.keySet()) {
				clusterRankList.add(clusterMap.get(tmpKey));
			}
			
			Collections.sort(clusterRankList, new Comparator<ArrayList<Integer>>() {
				@Override
				public int compare(ArrayList<Integer> o1, ArrayList<Integer>o2) {
					if (o1.size() < o2.size())
						return -1;
					else if (o1.size() == o2.size())
						return 0;
					else
						return 0;
				}
			});
			
			System.out.println("Check cluster rank list: " + clusterRankList);
			
			ArrayList<Integer> clusterNumList = new ArrayList<Integer>();
			for (ArrayList<Integer>tmpList: clusterRankList) {
				clusterNumList.add(tmpList.size());
			}
			
			//Add the artifact into State
			Map<String, Object> newFieldMap = new HashMap<String, Object>();
			newFieldMap.put("ClusterSummary", clusterNumList);
			this.expandStateDefinition(newFieldMap, stateRecorder);
			
			return clusterNumList;
		}
		return null;
	}
}
