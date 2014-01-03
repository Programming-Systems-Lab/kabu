package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
//import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.functions.LeastMedSq;
//import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.VotedPerceptron;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.Bagging;
//import weka.classifiers.meta.Dagging;
import weka.classifiers.meta.Decorate;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class WekaClassifierExample {
	
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
			
			//System.out.println("Check smo object: " + smoDriver.toString());
			
			return smoDriver;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	@Metamorphic
	public LogitBoost trainLB(Instances data) {
		LogitBoost lb = new LogitBoost();
		try {
			lb.setUseResampling(true);
			lb.buildClassifier(data);
			return lb;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	@Metamorphic
	public J48 trainJ48Model(Instances data) {
		J48 tree = new J48();
		String[] options = new String[]{"-t"};
		try {
			tree.setOptions(options);
			tree.buildClassifier(data);
			
			return tree;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	@Metamorphic
	public BayesNet trainBayesModel(Instances data) {
		BayesNet net = new BayesNet();
		//String[] options = new String[]{"-t"};
		try {
			//net.setOptions(options);
			net.buildClassifier(data);
			
			return net;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	@Metamorphic
	public NaiveBayes trainNaiveBayesModel(Instances data) {
		NaiveBayes nb = new NaiveBayes();
		//String[] options = new String[]{"-t"};
		try {
			//nb.setOptions(options);
			nb.buildClassifier(data);
			
			return nb;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	@Metamorphic
	public LinearRegression trainLR(Instances data) {
		LinearRegression lr = new LinearRegression();
		String[] options = new String[]{"-t"};
		try {
			lr.buildClassifier(data);
			
			return lr;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/*@Metamorphic
	public LibSVM trainSVM(Instances data) {
		LibSVM svm = new LibSVM();
		//String[] options = new String[]{"-t"};
		try {
			svm.buildClassifier(data);
			
			return svm;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}*/
	
	@Metamorphic
	public Logistic trainLogistic(Instances data) {
		Logistic log = new Logistic();
		String[] options = new String[]{"-t"};
		try {
			log.setOptions(options);
			log.buildClassifier(data);
			return log;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	@Metamorphic
	public IBk trainKnn(Instances data) {
		IBk ibk = new IBk();
		String[] options = new String[]{"-t"};
		try {
			//ibk.setOptions(options);
			ibk.buildClassifier(data);
			
			return ibk;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	@Metamorphic
	public DecisionTable trainDT(Instances data) {
		DecisionTable dt = new DecisionTable();
		String[] options = new String[]{"-t"};
		try {
			dt.setOptions(options);
			dt.buildClassifier(data);
			
			return dt;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	@Metamorphic
	public Decorate trainDecorate(Instances data) {
		Decorate decorate = new Decorate();
		try {
			decorate.buildClassifier(data);
			return decorate;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/*@Metamorphic
	public Dagging trainDagging(Instances data) {
		Dagging dagging = new Dagging();
		try {
			String[] options = new String[]{"-F", "5"};
			dagging.setOptions(options);
			dagging.buildClassifier(data);
			return dagging;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}*/
	
	@Metamorphic
	public Bagging trainBagging(Instances data) {
		Bagging bagDriver = new Bagging();
		try {
			bagDriver.setCalcOutOfBag(true);
			bagDriver.buildClassifier(data);
			return bagDriver;
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
		return null;
	}
	
	public void evalModel(Classifier classifier, Instances data) {
		Evaluation eval = null;
		try {			
			eval = new Evaluation(data);
			eval.evaluateModel(classifier, data);
			
			double[][] confMatrix = eval.confusionMatrix();
			for (int i = 0; i < confMatrix.length; i++) {				
				for (int j = 0; j < confMatrix[i].length; j++) {
					System.out.println("Check content: " + i + " " + j + " " + confMatrix[i][j]);
				}
			}
			
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
		
		WekaClassifierExample wcEx = new WekaClassifierExample();
		Instances data = wcEx.loadData(dataPath);
		
		if (data == null) {
			System.err.println("Load no data");
			return ;
		}
		
		/*Bagging bag = wcEx.trainBagging(data);
		if (bag == null) {
			System.err.println("Fail to train bag");
		}
		wcEx.evalModel(bag, data);*/
		
		/*LibSVM libsvm = wcEx.trainSVM(data);
		if (libsvm == null) {
			System.out.println("Fail to train SVM");
		}
		wcEx.evalModel(libsvm, data);*/
		
		/*Decorate decorate = wcEx.trainDecorate(data);
		if (decorate == null) {
			System.err.println("Fail to train decorate");
		}
		wcEx.evalModel(decorate, data);*/
		
		/*Dagging dagging = wcEx.trainDagging(data);
		if (dagging == null) {
			System.err.println("Fail to train Dagging");
		}
		wcEx.evalModel(dagging, data);*/
		
		/*SMO smo = wcEx.trainSMOModel(data);
		if (smo == null) {
			System.err.println("Fail to train smo model");
			return ;
		}
		wcEx.evalModel(smo, data);*/
		
		LogitBoost lb = wcEx.trainLB(data);
		if (lb == null) {
			System.err.println("Fail to train LogitBoost");
		}
		wcEx.evalModel(lb, data);
		
		/*J48 tree = wcEx.trainJ48Model(data);
		if (tree == null) {
			System.err.println("Fail to train j48 model");
		}
		wcEx.evalModel(tree, data);*/
		
		/*BayesNet net = wcEx.trainBayesModel(data);
		if (net == null) {
			System.err.println("Fail to train Bayesnet");
		}
		wcEx.evalModel(net, data);*/
		
		/*NaiveBayes nb = wcEx.trainNaiveBayesModel(data);
		if (nb == null) {
			System.err.println("Fail to train Naive Bayes");
		}
		wcEx.evalModel(nb, data);*/
		
		/*Logistic log = wcEx.trainLogistic(data);
		if (log == null) {
			System.err.println("Fail to train GP");
		}
		wcEx.evalModel(log, data);*/
		
		/*IBk ibk = wcEx.trainKnn(data);
		if (ibk == null) {
			System.err.println("Fail to train ibk");
		}
		wcEx.evalModel(ibk, data);*/
		
		/*DecisionTable dt = wcEx.trainDT(data);
		if (dt == null) {
			System.err.println("Fail to train decision table");
		}
		wcEx.evalModel(dt, data);*/
	}

}
