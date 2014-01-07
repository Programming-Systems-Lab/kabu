package edu.columbia.cs.psl.mountaindew.example;

import java.util.Arrays;

import org.encog.mathutil.rbf.BasicRBF;
import org.encog.mathutil.rbf.GaussianFunction;
import org.encog.mathutil.rbf.RBFEnum;
import org.encog.ml.BasicML;
import org.encog.ml.MLClassification;
import org.encog.ml.MLCluster;
import org.encog.ml.MLInputOutput;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.kmeans.KMeansClustering;
import org.encog.ml.svm.*;
import org.encog.ml.svm.training.SVMSearchTrain;
import org.encog.neural.rbf.RBFNetwork;
import org.encog.neural.rbf.training.SVDTraining;
import org.encog.neural.som.SOM;
import org.encog.neural.som.training.basic.BasicTrainSOM;
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodRBF;
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodRBF1D;
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodSingle;
import org.encog.util.csv.CSVFormat;
import org.encog.util.simple.EncogUtility;
import org.encog.util.simple.TrainingSetUtil;

public class EncogExample {
	
	public static final double[][] DATA = { { 28, 15, 22 }, { 16, 15, 32 },
        { 32, 20, 44 }, { 1, 2, 3 }, { 3, 2, 1 } };
	
	public SVM trainSVM(BasicMLDataSet set) {
		SVM svm = new SVM(set.size(), false);
		SVMSearchTrain sst = new SVMSearchTrain(svm, set);
		
		do {
			sst.iteration();
		} while (sst.getError() > 0.01);
		
		System.out.println("Check svm error: " + svm.calculateError(set));
		
		/*System.out.println("Check instance: " + (svm instanceof SVM));
		
		EncogUtility.trainToError(svm, set, 0.01);
		EncogUtility.evaluate(svm, set);*/
		return svm;
	}
	
	public SOM trainSOM(BasicMLDataSet set) {
		SOM som = new SOM(set.getInputSize(), 3);
		som.reset();
		
		BasicTrainSOM train = new BasicTrainSOM(som, 0.0001, set, new NeighborhoodSingle());
		int iteration = 0;
		do {
			train.iteration();
			iteration++;
		} while(iteration < 500);
		
		System.out.println("Check SOM error: " + som.calculateError(set));
		return som;
	}
	
	public RBFNetwork trainRBF(BasicMLDataSet set) {
		RBFNetwork network = new RBFNetwork(set.getInputSize(), 10, 1, RBFEnum.Gaussian);
		SVDTraining train = new SVDTraining(network, set);
		int iteration = 0;
		do {
			train.iteration();
			iteration++;
		} while(iteration < 10000);
		
		System.out.println("RBF network error: " + train.getError());
		
		return network;
	}
	
	public void evaluateClassifier(MLClassification model, BasicMLDataSet set) {
		for (MLDataPair pair: set.getData()) {
			System.out.println("Input: " + pair.getInputArray()[0]);
			System.out.println("Actual output: " + model.classify(pair.getInput()));
			System.out.println("Ideal output: " + pair.getIdealArray()[0]);
		}
	}
	
	public void evaluateRegression(MLRegression model, BasicMLDataSet set) {
		for (MLDataPair pair: set.getData()) {
			System.out.println("Input: " + pair.getInputArray()[0]);
			System.out.println("Actual output: " + model.compute(pair.getInput()).getData(0));
			System.out.println("Ideal output: " + pair.getIdealArray()[0]);
		}
	}
	
	public static void main(String args[]) {
		String dataPath = "data/iris.csv";
		
		MLDataSet trainingSet = TrainingSetUtil.loadCSVTOMemory(
				CSVFormat.ENGLISH, dataPath, true, 4, 1);
		BasicMLDataSet set = new BasicMLDataSet(trainingSet);
		
		/*BasicMLDataSet set = new BasicMLDataSet();
		
		for (double[] data: EncogExample.DATA) {
			BasicMLData d = new BasicMLData(data);
			set.add(d);
		}*/
		
		/*for (MLDataPair pair: set.getData()) {
			System.out.println(pair);
		}*/
		
		EncogExample ee = new EncogExample();
		
		/*MLClassification model = ee.trainSVM(set);
		ee.evaluateModel(model, set);*/
		
		MLClassification model = ee.trainSOM(set);
		ee.evaluateClassifier(model, set);
		
		/*MLRegression model = ee.trainRBF(set);
		ee.evaluateRegression(model, set);*/
		
		/*KMeansClustering kmeans = new KMeansClustering(3, set);
		kmeans.iteration(100);
		
		int i = 1;
        for (final MLCluster cluster : kmeans.getClusters()) {
                System.out.println("*** Cluster " + (i++) + " ***");
                final MLDataSet ds = cluster.createDataSet();
                final MLDataPair pair = BasicMLDataPair.createPair(
                                ds.getInputSize(), ds.getIdealSize());
                for (int j = 0; j < ds.getRecordCount(); j++) {
                        ds.getRecord(j, pair);
                        System.out.println(Arrays.toString(pair.getInputArray()));
                }
        }*/
	}

}
