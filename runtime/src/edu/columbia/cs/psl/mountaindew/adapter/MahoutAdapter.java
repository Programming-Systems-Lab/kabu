package edu.columbia.cs.psl.mountaindew.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mahout.classifier.ConfusionMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.Vector;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

public class MahoutAdapter extends  AbstractAdapter{
	
	private Map<Vector, String> nameMap = new HashMap<Vector, String>();
	
	private List<String> nameList = new ArrayList<String>();

	@Override
	public Object unboxInput(Object input) {
		// TODO Auto-generated method stub
		if (Collection.class.isAssignableFrom(input.getClass())) {
			List inputList = new ArrayList((Collection) input);
			
			if (NamedVector.class.isAssignableFrom(inputList.get(0).getClass())) {
				List<NamedVector> nvList = (List<NamedVector>)inputList;
				int dataNum = nvList.size();
				int fieldNum = nvList.get(0).size();
				double[][] ret = new double[dataNum][fieldNum];
				
				for (int i = 0; i < dataNum; i++) {
					NamedVector tmpVec = nvList.get(i);
					Vector delegateVec = tmpVec.getDelegate();
					this.nameMap.put(delegateVec, tmpVec.getName());
					this.nameList.add(tmpVec.getName());
					for (int j = 0; j < fieldNum; j++) {
						ret[i][j] = delegateVec.get(j);
					}
				}
				return ret;
			}
		}
		
		return null;
	}

	@Override
	public Object adaptInput(Object transInput) {
		// TODO Auto-generated method stub
		double[][] inputRep = (double[][])transInput;
		List<NamedVector> retList = new ArrayList<NamedVector>();
		
		NamedVector nv;
		DenseVector tmpVec;
		String tmpName;
		for (int i = 0; i < inputRep.length; i++) {
			tmpVec = new DenseVector(inputRep[i]);
			tmpName = this.nameMap.get(tmpVec);
			
			nv = new NamedVector(tmpVec, tmpName);
			//System.out.println("Check trans nv: " + nv);
			retList.add(nv);
		}
		
		
		return retList;
	}

	@Override
	public Object adaptOutput(HashMap<String, Object>stateRecorder, Object outputModel, Object... testingData) {
		// TODO Auto-generated method stub
		recordState(stateRecorder, outputModel);
		if (ConfusionMatrix.class.isAssignableFrom(outputModel.getClass())) {
			ConfusionMatrix cMatrix = (ConfusionMatrix) outputModel;
			Matrix outputMatrix = cMatrix.getMatrix();
			
			double[][] ret = new double[outputMatrix.rowSize()][outputMatrix.columnSize()];
			
			for (int i = 0; i < outputMatrix.rowSize(); i++) {
				for (int j = 0; j < outputMatrix.columnSize(); j++) {
					ret[i][j] = outputMatrix.get(i, j);
				}
			}
			return ret;
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
		double[][] realInput = (double[][])transInput;
		this.nameMap = new HashMap<Vector, String>();
		Vector tmpVector;
		for (int i = 0; i < realInput.length; i++) {
			tmpVector = new DenseVector(realInput[i]);
			this.nameMap.put(tmpVector, this.nameList.get(i));
			
			//System.out.println("Check name vec: " + this.nameList.get(i) + " " + tmpVector);
		}
	}
}
