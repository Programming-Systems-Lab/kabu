package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class MahoutExample {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws TasteException 
	 */
	public static void main(String[] args) throws IOException, TasteException {
		// TODO Auto-generated method stub
		System.out.println("In the main");
		
		File dataFile = new File("data/knntest.csv");
		
		if (!dataFile.exists()) {
			System.out.println("File does not exist");
			return ;
		}
		System.out.println("Data file exists: " + dataFile.getAbsolutePath());
		
		DataModel model = new FileDataModel(new File("data/knntest.csv"));
		UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
		
		System.out.println("Similarity: " + similarity.userSimilarity(1, 4));
		UserNeighborhood neightborhood = new NearestNUserNeighborhood(5, similarity, model);
		
		for (long tmp: neightborhood.getUserNeighborhood(1)) {
			System.out.println("Traverse neighborhood for user 1: " + tmp);
		}
		
		Recommender recommender = new GenericUserBasedRecommender(model, neightborhood, similarity);
		
		List<RecommendedItem> recommendations = recommender.recommend(1, 2);
		
		System.out.println("Before recommendation");
		
		System.out.println("Recommendation list: " + recommendations.size());
		
		for (RecommendedItem recommendation: recommendations) {
			System.out.println("In the loop");
			System.out.println(recommendation);
		}

	}

}
