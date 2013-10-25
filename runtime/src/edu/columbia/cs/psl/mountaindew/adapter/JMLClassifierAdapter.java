package edu.columbia.cs.psl.mountaindew.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.evaluation.CrossValidation;
import net.sf.javaml.classification.evaluation.EvaluateDataset;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

public class JMLClassifierAdapter extends AbstractAdapter{
	
	private HashMap<Instance, Object> instMap = new HashMap<Instance, Object>();
	private List<Object> instList = new ArrayList<Object>();

	@Override
	public Object unboxInput(Object input) {
		System.out.println("Check input class:" + input.getClass().getName());
		if (Dataset.class.isAssignableFrom(input.getClass())) {			
			Dataset dataset = (Dataset)input;
			int dataNum = dataset.size();
			int attrNum = dataset.noAttributes();
			double[][] ret = new double[dataNum][attrNum];
			
			System.out.println("Test dataset: " + dataset);
			
			Instance tmpInstance;
			for (int i = 0; i < dataset.size(); i++) {
				tmpInstance = dataset.get(i);
				
				for (int j = 0; j < tmpInstance.noAttributes(); j++) {
					ret[i][j] = tmpInstance.value(j);
				}
				instMap.put(tmpInstance, tmpInstance.classValue());
				instList.add(tmpInstance.classValue());
			}
			
			return ret;
		}
		return null;
	}

	@Override
	public Object adaptInput(Object transInputObj) {
		double[][] transInput = (double[][])transInputObj;
		
		Dataset ds = new DefaultDataset();
		Instance tmpInstance;
		for (int i = 0; i < transInput.length; i++) {
			tmpInstance = new DenseInstance(transInput[i]);
			tmpInstance.setClassValue(this.instMap.get(tmpInstance));
			ds.add(tmpInstance);
		}
		
		return ds;
	}

	@Override
	public Object adaptOutput(Object outputModel, Object... testingData) {
		if (Classifier.class.isAssignableFrom(outputModel.getClass())) {
			Classifier outputClassifier = (Classifier)outputModel;
			Object tmpObj = null;
			Dataset finalData;
			
			if (testingData.length != 0) {
				tmpObj = testingData[0];
			}
			
			if (Dataset.class.isAssignableFrom(tmpObj.getClass()))
				finalData = (Dataset)tmpObj;
			else
				finalData = (Dataset)this.getDefaultTestingData();
			
			return adaptOutput(EvaluateDataset.testDataset(outputClassifier, finalData));
		} else if (Map.class.isAssignableFrom(outputModel.getClass())) {
			Map resultMap = (Map)outputModel;
			Entry tmpEntry = (Entry)resultMap.entrySet().iterator().next();
			
			if (!PerformanceMeasure.class.isAssignableFrom(tmpEntry.getValue().getClass())) {
				return null;
			}
			
			//Cannot compare performance measure here, because it does not override the eauls....
			Map<Object, PerformanceMeasure> confusionMap = (Map<Object, PerformanceMeasure>)resultMap;
			Map<Object, String> confusionStringMap = new HashMap<Object, String>();
			
			for (Object key: confusionMap.keySet()) {
				confusionStringMap.put(key, confusionMap.get(key).toString());
			}
			
			Set<String> newFieldNames = new HashSet<String>();
			Map<String, Object> newFieldMap = new HashMap<String, Object>();
			newFieldMap.put("ConfusionStringMap", confusionStringMap);
			
			return confusionStringMap;
		}
		
		return null;
	}

	@Override
	public List<Object> skipColumn(Object input) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void complementTransformInput(Object transInput) {
		double[][]ret = (double[][])transInput;
		
		for (int i = 0; i < ret.length; i++) {
			Instance tmpInst = new DenseInstance(ret[i]);
			this.instMap.put(tmpInst, this.instList.get(i));
		}
		
	}

}
