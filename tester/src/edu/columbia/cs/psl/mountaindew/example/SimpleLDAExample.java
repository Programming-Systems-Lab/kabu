package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.clustering.lda.cvb.CVB0Driver;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.utils.SplitInput;
import org.apache.mahout.utils.vectors.RowIdJob;
import org.apache.mahout.utils.vectors.VectorDumper;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;

public class SimpleLDAExample {
	
	private static String baseDir = "/Users/mike/Desktop/reuters_ws";
	private static int numTopics = 20;
	private static double doc_topic_smooth = 0.0001;
	private static double term_topic_smooth = 0.0001;
	private static int maxIter = 20;
	private static int iterationBlockSize = 10;
	private static double convergenceDelta = 0.0f;
	private static long seed = System.nanoTime() % 10000;
	private static float testFraction = 0;
	private static int numTrainThreads = 4;
	private static int numUpdateThreads = 1;
	private static int maxIterPerDoc = 10;;
	private static int numReduceTasks = 10;
	private static boolean backfillPerplexity = false;
	
	private void writeDataByToolRunner(Configuration conf, FileSystem fs) throws IOException {
		String[] arg = null;
		
		//Convert raw data to seq files
		String input = baseDir + "/input";
		String seqDir = baseDir + "/seq";
		arg = new String[] {"-i", input, "-o", seqDir, "-c", "UTF-8", "-chunk", "5"};

		try {
			ToolRunner.run(conf, new SequenceFilesFromDirectory(), arg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("SeqFromDir completes");
		
		//Convert seq files to vectors
		String vecDir = baseDir + "/" + "vec";
		arg = new String[] {"-i", seqDir, "-o", vecDir, "--maxDFPercent", "85", "--namedVector", "--analyzerName", "org.apache.lucene.analysis.WhitespaceAnalyzer"};
		
		try {
			ToolRunner.run(conf, new SparseVectorsFromSequenceFiles(), arg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Seq2Vector completes");
		
		String rowIdVecDir = baseDir + "/" + "rowid_vec";
		arg = new String[] {"-Dmapred.input.dir=" + vecDir + "/tfidf-vectors/part-r-00000", "-Dmapred.output.dir=" + rowIdVecDir};
		
		try {
			ToolRunner.run(new RowIdJob(), arg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Row ID conversion completes");
	}
	
	private void executeDirichlet(Configuration conf, FileSystem fs) {
		String[] arg = null;
		
		String tfidfDir = baseDir + "/vec/tfidf-vectors";
		String topicOutputDir = baseDir + "/topic_output";
		String docOutputDir = baseDir + "/doc_output";
		String dicFilePath = baseDir + "/vec/dictionary.file-0";
		String tmpDir = baseDir + "/tmp";
		/*arg = new String[] {"-i", tfidfDir, 
				"-o", outputDir, 
				"-k", "20", "-ow", 
				"-x", "20", 
				"-a0", "2", 
				"-md", "org.apache.mahout.clustering.dirichlet.models.DistanceMeasureClusterDistribution", 
				"-mp", "org.apache.mahout.math.DenseVector", 
				"-dm", "org.apache.mahout.common.distance.ConsineDistanceMeasure"};*/
		
		File testFile;
		
		testFile = new File(tfidfDir);
		if (!testFile.exists()) {
			testFile.mkdir();
		}
		
		testFile = new File(topicOutputDir);
		if (!testFile.exists()) {
			testFile.mkdir();
		}
		
		testFile = new File(docOutputDir);
		if (!testFile.exists()) {
			testFile.mkdir();
		}
		
		testFile = new File(dicFilePath);
		if (!testFile.exists()) {
			testFile.mkdir();
		}
		
		testFile = new File(tmpDir);
		if (!testFile.exists()) {
			testFile.mkdir();
		}
		
		try {
			CVB0Driver.run(conf, 
					new Path(tfidfDir), 
					new Path(topicOutputDir), 
					numTopics, 
					0, 
					doc_topic_smooth, 
					term_topic_smooth, 
					maxIter, 
					iterationBlockSize, 
					convergenceDelta, 
					new Path(dicFilePath), 
					new Path(docOutputDir), 
					new Path(tmpDir), seed, testFraction, numTrainThreads, numUpdateThreads, maxIterPerDoc, numReduceTasks, backfillPerplexity);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		System.out.println("Dirichlet execution completes");
		
	}
	
	private void ldaVectorDump() {
		String topicOutputDir = baseDir + "/topic_output";
		String dicFilePath = baseDir + "/vec/dictionary.file-0";
		
		for(int k=0;k<numTopics;k++){
	        System.out.println("Dumping topic \t"+k);
	        String partFile="part-m-0000"+k;
	        if(k>=10)
	            partFile="part-m-000"+k;

	        String output="topic"+k;
	        String[] topicTermDumperArg = {"-s", topicOutputDir +"/"+partFile, "-dt", "sequencefile", "-d", 
	            dicFilePath, "-o",output,  "-c", };  

	        try {
	        	VectorDumper.main(topicTermDumperArg);
	        } catch (Exception ex) {
	        	ex.printStackTrace();
	        }

	    }
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Configuration conf = null;
		FileSystem fs = null;

		
		try {
			conf = new Configuration();
			fs = FileSystem.get(conf);

			SimpleLDAExample ex = new SimpleLDAExample();
			ex.writeDataByToolRunner(conf, fs);
			ex.executeDirichlet(conf, fs);
			//ex.ldaVectorDump();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
