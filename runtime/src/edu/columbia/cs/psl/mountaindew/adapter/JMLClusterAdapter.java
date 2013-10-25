package edu.columbia.cs.psl.mountaindew.adapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.clustering.evaluation.SumOfSquaredErrors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.EuclideanDistance;

public class JMLClusterAdapter extends AbstractAdapter{
	
	private Map<Instance, Object> instMap = new HashMap<Instance, Object>();
	
	private List<Object> instList = new ArrayList<Object>();

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
		if (Clusterer.class.isAssignableFrom(outputModel.getClass())) {
			Clusterer clusterer = (Clusterer)outputModel;
			
			Dataset finalData = null;
			
			if (testingData.length != 0) {
				finalData = (Dataset)testingData[0];
			} else {
				finalData = (Dataset)this.getDefaultTestingData();
			}
			
			Dataset[] clusterResult = clusterer.cluster(finalData);
			
			return this.adaptOutput(clusterResult);
		} else if (outputModel.getClass().isArray()) {
			Object tmpObj = Array.get(outputModel, 0);
			
			if (Dataset.class.isAssignableFrom(tmpObj.getClass())) {
				Dataset[] realModel = (Dataset[]) outputModel;
				
				System.out.println("Enter adapter: " + realModel);
				
				ArrayList<Integer> clusterNumList = new ArrayList<Integer>();
				
				for (int i = 0; i < realModel.length; i++) {
					clusterNumList.add(realModel[i].size());
				}
				
				Collections.sort(clusterNumList);
				
				ClusterEvaluation sse = new SumOfSquaredErrors(new EuclideanDistance());
				double evalResult = sse.score(realModel);
				
				Map<String, Object> newFieldMap = new HashMap<String, Object>();
				newFieldMap.put("ClusterSummary", clusterNumList);
				newFieldMap.put("EvaluationResult", evalResult);
				
				//this.expandStateDefinition(newFieldMap, stateRecorder);

				return newFieldMap;
			}
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
