package edu.columbia.cs.psl.mountaindew.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.ConstantValueEncoder;
import org.apache.mahout.vectorizer.encoders.Dictionary;
import org.apache.mahout.vectorizer.encoders.FeatureVectorEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;

import com.google.common.base.Splitter;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;

public class SimpleLRExample {
	
	private static final int FEATURES = 10000;  
	private static Multiset<String> overallCounts;
	
	private static void countWords(Analyzer analyzer, Collection<String>words, Reader in ) throws IOException {
		TokenStream ts = analyzer.tokenStream("text", in);
		ts.addAttribute(CharTermAttribute.class);
		
		while (ts.incrementToken()) {
			String s = ts.getAttribute(CharTermAttribute.class).toString();
			words.add(s);
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File base = new File("/Users/mikefhsu/Desktop/20news-bydate/20news-bydate-train");
		overallCounts = HashMultiset.create();
		
		Map<String, Set<Integer>> traceDictionary =
				new TreeMap<String, Set<Integer>>();
		FeatureVectorEncoder encoder = new StaticWordValueEncoder("body");
		encoder.setProbes(2);
		encoder.setTraceDictionary(traceDictionary);
		
		FeatureVectorEncoder bias = new ConstantValueEncoder("Intercept");
		bias.setTraceDictionary(traceDictionary);
		
		FeatureVectorEncoder lines = new ConstantValueEncoder("Lines");
		lines.setTraceDictionary(traceDictionary);
		
		FeatureVectorEncoder logLines = new ConstantValueEncoder("LogLines");
		logLines.setTraceDictionary(traceDictionary);
		
		Dictionary newsGroups = new Dictionary();
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		
		//20 for categories and 10 for features
		OnlineLogisticRegression learningAlg = 
				new OnlineLogisticRegression(20, FEATURES, new L1()).
				alpha(1).
				stepOffset(1000).decayExponent(0.9).lambda(3.0e-5).learningRate(20);
		
		List<File> files = new ArrayList<File>();
		
		for (File newsgroup: base.listFiles()) {
			System.out.println("Newsgroup name: " + newsgroup.getName());
			if (!newsgroup.isDirectory())
				continue;
			
			newsGroups.intern(newsgroup.getName());
			files.addAll(Arrays.asList(newsgroup.listFiles()));
		}
		
		Collections.shuffle(files);
		System.out.println("Training files: " + files.size());
		
		double averageLL = 0.0;
		double averageCorrect = 0.0;
		double averageLineCount = 0.0;
		int k = 0;
		double step = 0.0;
		int[] bumps = new int[]{1, 2, 5};
		double lineCount = 0;
		
		Splitter onColon = Splitter.on(":").trimResults();
		
		for (File file: files) {
			//System.out.println("Handle file: " + file.getName());
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String ng = file.getParentFile().getName();
			int actual = newsGroups.intern(ng);
			Multiset<String> words = ConcurrentHashMultiset.create();
			
			String line = reader.readLine();
			while(line != null && line.length() > 0) {
				if (line.startsWith("Lines:")) {
					String count = Iterables.get(onColon.split(line), 1);
					
					try {
						lineCount = Integer.parseInt(count);
						averageLineCount += (lineCount - averageLineCount) / Math.min(k+1, 1000);
					} catch(Exception ex) {
						ex.printStackTrace();
						lineCount = averageLineCount;
					}
				}
				
				boolean countHeader = (line.startsWith("From:") || 
						line.startsWith("Subject:") ||
						line.startsWith("Keywords:") ||
						line.startsWith("Summary:"));
				
				do {
					StringReader in = new StringReader(line);
					
					if (countHeader) {
						countWords(analyzer, words, in);
					}
					
					line = reader.readLine();
				} while(line.startsWith(" "));
			}
			
			countWords(analyzer, words, reader);
			reader.close();
			
			Vector v = new RandomAccessSparseVector(FEATURES);
			bias.addToVector("", 1, v);
			
			lines.addToVector("", lineCount/30, v);
			
			logLines.addToVector("", Math.log(lineCount + 1), v);
			
			for (String word: words.elementSet()) {
				encoder.addToVector(word, Math.log(1 + words.count(word)), v);
			}
			
			double mu = Math.min(k + 1, 200);
			double ll = learningAlg.logLikelihood(actual, v);
			averageLL = averageLL + (ll - averageLL)/mu;
			
			Vector p = new DenseVector(20);
			learningAlg.classifyFull(p, v);
			int estimated = p.maxValueIndex();
			
			int correct = (estimated == actual? 1: 0);
			averageCorrect = averageCorrect + (correct - averageCorrect)/mu;
			
			//System.out.println("Vector before training: " + v);
			learningAlg.train(actual, v);
			//System.out.println("Vector after training: " + v);
	
			k++;
			
			int bump = bumps[(int)Math.floor(step)%bumps.length];
			int scale = (int)Math.pow(10, Math.floor(step/bumps.length));
			
			if (k % (bump * scale) == 0) {
				step += 0.25;
				System.out.printf("%10d %10.3f %10.3f %10.2f %s %s\n",
			               k, ll, averageLL, averageCorrect * 100, ng, 
			               newsGroups.values().get(estimated));
			}
			learningAlg.close();
		}
	}

}
