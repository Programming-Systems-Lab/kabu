package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;
import java.util.Map;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.evaluation.CrossValidation;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;

public class JMLClassifierExample {
	
	public Dataset loadData(String dataPath) {
		Dataset dataset = null;
		try {
			dataset = FileHandler.loadDataset(new File(dataPath), 4, ",");
			return dataset;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public Map<Object, PerformanceMeasure> evalClassifier(Classifier classifier, Dataset data) {
		CrossValidation cv = new CrossValidation(classifier);
		return cv.crossValidation(data);
	}
	
	@Metamorphic
	public KNearestNeighbors trainKNN(Dataset dataset) {
		KNearestNeighbors knn = new KNearestNeighbors(5);
		knn.buildClassifier(dataset);
		return knn;
	}
	
	public static void main(String args[]) {
		JMLClassifierExample ml = new JMLClassifierExample();
		
		String dataPath = "data/iris_short.data";
		Dataset data = ml.loadData(dataPath);
		System.out.println(data);
		
		KNearestNeighbors knn = ml.trainKNN(data);
		Map<Object, PerformanceMeasure> p = ml.evalClassifier(knn, data);
		System.out.println(p);
		
		for (int i = 0; i < data.size(); i++) {
			Instance tmpInstance = data.get(i);
			
			for (int j = 0; j < tmpInstance.noAttributes(); j++) {
				System.out.print(tmpInstance.get(j) + ",");
			}
			System.out.println("\n");
		}		
	}

}
