package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;
import java.io.IOException;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class WekaSMOExample {
	
	public Instances loadData(String dataPath) {
		ArffLoader dataLoader = new ArffLoader ();
		try {
			dataLoader.setFile(new File(dataPath));
			
			Instances data = dataLoader.getDataSet();
			data.setClassIndex(data.numAttributes() - 1);
			
			return data;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	@Metamorphic
	public SMO trainSMOModel(Instances data) {
		SMO smoDriver = new SMO();
		String[] options = new String[]{"-t"};
		try {
			smoDriver.setOptions(options);
			smoDriver.buildClassifier(data);
			
			System.out.println("Check smo object: " + smoDriver.toString());
			
			return smoDriver;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public void evalModel(SMO smoDriver, Instances data) {
		Evaluation eval = null;
		try {
			eval = new Evaluation(data);
			eval.evaluateModel(smoDriver, data);
			System.out.println(eval.toSummaryString("Result\n", false));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String dataPath = "data/iris.arff";
		
		WekaSMOExample smoEx = new WekaSMOExample();
		Instances data = smoEx.loadData(dataPath);
		
		if (data == null) {
			System.err.println("Load no data");
			return ;
		}
		
		SMO smo = smoEx.trainSMOModel(data);
		
		if (smo == null) {
			System.err.println("Fail to train model");
			return ;
		}
		
		smoEx.evalModel(smo, data);
	}

}
