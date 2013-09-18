package edu.columbia.cs.psl.mountaindew.adapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;

public abstract class AbstractAdapter {
	
	protected Object oriInput;
	
	protected Object oriOutput;
	
	protected Object transInput;
	
	protected Object transOutput;
	
	private Object defaultTestingData;
	
	private List<Object>skipList;
	
	private double[][] complementMap;
	
	//Key: Class name, Val: Set of field names
	protected HashMap<String, HashSet<String>> stateDefinition;
				
	public abstract Object unboxInput(Object input);
	
	public abstract Object adaptInput(Object transInput);
	
	public abstract Object adaptOutput(HashMap<String, Object> stateRecorder, Object outputModel, Object...testingData);
	
	protected void recordState(HashMap<String, Object> stateRecorder, Object outputModel) {
		System.out.println("Check stateDefinition: " + stateDefinition);
		System.out.println("Check outputModel name: " + outputModel.getClass().getName());
		if (stateDefinition.containsKey(outputModel.getClass().getName())) {
			this.registerInterestedFieldValues(outputModel, stateRecorder);
		}
	}
		
	public void setData(Object oriInput, Object oriOutput, Object transInput, Object transOutput) {
		this.oriInput = oriInput;
		this.oriOutput = oriOutput;
		this.transInput = transInput;
		this.transOutput = transOutput;
	}
	
	public void setDefaultTestingData(Object defaultTestingData) {
		this.defaultTestingData = defaultTestingData;
	}
	
	public Object getDefaultTestingData() {
		return this.defaultTestingData;
	}
	
	public List<Object> getSkipList() {
		return this.skipList;
	}
	
	public void setSkipList(List<Object> skipList) {
		this.skipList = skipList;
	}
	
	public abstract List<Object> skipColumn(Object input);
	
	public void setupComplementMap(Object input) {
		if (this.skipList == null)
			return ;
		
		double[][] inputInDouble = (double[][])input;
		int dataNum = inputInDouble.length;
		int columnNum = inputInDouble[0].length;
		this.complementMap = new double[dataNum][columnNum];
		
		for (int i = 0; i < dataNum; i++) {
			for (int j = 0; j < columnNum; j++) {
				if (this.skipList.contains(j)) {
					complementMap[i][j] = inputInDouble[i][j];
				} else {
					complementMap[i][j] = -1;
				}
			}
		}
	}
	
	public void complementTransformInput(Object transInputObj) {
		if (this.complementMap == null)
			return ;
		
		double[][] transInput = (double[][])transInputObj;
		//Put the non-numeric value back
		for (int i = 0; i < transInput.length; i++) {
			double[] row = transInput[i];
			for (int j = 0; j < row.length; j++) {
				
				if (this.complementMap[i][j] != -1) {
					transInput[i][j] = complementMap[i][j];
				}
			}
		}
	}
		
	public void setStateDefinition(HashMap<String, HashSet<String>> stateDefinition) {
		this.stateDefinition = stateDefinition;
		
		System.out.println("Adapter get new state definition: " + this.stateDefinition);
	}
	
	public HashMap<String, HashSet<String>> getStateDefinition() {
		return this.stateDefinition;
	}
	
	private void registerInterestedFieldValues(Object outputObj, HashMap<String, Object>stateRecorder) {
		Class outputClass = outputObj.getClass();
		String outputClassName = outputClass.getName();
		
		Field tmpField;
		String classFieldCombo;
		
		try {
			for (String fieldName: this.stateDefinition.get(outputClassName)) {
				//tmpField = outputClass.getField(fieldName);
				tmpField = outputClass.getDeclaredField(fieldName);
				tmpField.setAccessible(true);
				
				classFieldCombo = outputClassName + ":" + fieldName;
				stateRecorder.put(classFieldCombo, tmpField.get(outputObj));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
