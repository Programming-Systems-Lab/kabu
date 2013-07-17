package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.GenericWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.clustering.classify.WeightedPropertyVectorWritable;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.apache.mahout.clustering.lda.cvb.CVB0Driver;
import org.apache.mahout.clustering.lda.LDAPrintTopics;
import org.apache.mahout.common.IntPairWritable;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.utils.clustering.ClusterDumper;
import org.apache.mahout.utils.vectors.RowIdJob;
import org.apache.mahout.utils.vectors.VectorDumper;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;

public class SimpleLDAExample {
	
	private static String baseDir = "/Users/mike/Desktop/reuters_ws";
	private static int numTopics = 20;
	private static double doc_topic_smooth = 0.0001;
	private static double term_topic_smooth = 0.0001;
	private static int maxIter = 1;
	private static int iterationBlockSize = 10;
	private static double convergenceDelta = 0.0f;
	private static float testFraction = 0.1f;
	private static int numTrainThreads = 4;
	private static int numUpdateThreads = 1;
	private static int maxIterPerDoc = 10;;
	private static int numReduceTasks = 10;
	private static boolean backfillPerplexity = true;
	
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
	
	private static int getNumTerms(Configuration conf, Path dictionaryPath) throws IOException {
        FileSystem fs = dictionaryPath.getFileSystem(conf);
        Text key = new Text();
        IntWritable value = new IntWritable();
        int maxTermId = -1;
        for (FileStatus stat : fs.globStatus(dictionaryPath)) {
            SequenceFile.Reader reader = new SequenceFile.Reader(fs, stat.getPath(), conf);
            while (reader.next(key, value)) {
                maxTermId = Math.max(maxTermId, value.get());
            }
        }
        return maxTermId + 1;
    }
	
	private void executeDirichlet(Configuration conf, FileSystem fs) {
		String[] arg = null;
		
		//String tfidfDir = baseDir + "/vec/tfidf-vectors";
		String rowIdDir = baseDir + "/rowid_vec/matrix";
		String topicOutputDir = baseDir + "/topic_output";
		String docOutputDir = baseDir + "/doc_output";
		String dicFilePath = baseDir + "/vec/dictionary.file-0";
		String tmpDir = baseDir + "/temp";
		/*arg = new String[] {"-i", tfidfDir, 
				"-o", outputDir, 
				"-k", "20", "-ow", 
				"-x", "20", 
				"-a0", "2", 
				"-md", "org.apache.mahout.clustering.dirichlet.models.DistanceMeasureClusterDistribution", 
				"-mp", "org.apache.mahout.math.DenseVector", 
				"-dm", "org.apache.mahout.common.distance.ConsineDistanceMeasure"};*/
		
		File testFile;
		
		testFile = new File(rowIdDir);
		if (!testFile.exists()) {
			System.err.println("No matrix file: " + rowIdDir);
			return ;
		}
		
		/*testFile = new File(topicOutputDir);
		if (!testFile.exists()) {
			testFile.mkdir();
		}*/
		
		/*testFile = new File(docOutputDir);
		if (!testFile.exists()) {
			testFile.mkdir();
		}*/
		
		testFile = new File(dicFilePath);
		if (!testFile.exists()) {
			System.err.println("No dictionary file: " + dicFilePath);
			return ;
		}
		
		testFile = new File(tmpDir);
		if (!testFile.exists()) {
			testFile.mkdir();
		}
		
		try {
			int numTerm = this.getNumTerms(conf, new Path(dicFilePath));
			//int numTerm = 2000;
			long seed = System.nanoTime() % 10000;
			CVB0Driver.run(conf, 
					new Path(rowIdDir), 
					new Path(topicOutputDir), 
					numTopics, 
					numTerm, 
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
	
	private void ldaVectorDump(Configuration conf) throws IOException {
		String topicOutputDir = baseDir + "/doc_output";
		String dicFilePath = baseDir + "/vec/dictionary.file-0";
		String topicTermVectorDumpPath = baseDir + "/topicdump/dumpfile";
		int vectorSize = this.getNumTerms(conf, new Path(dicFilePath));
		/*String[] arg = new String[] {"--input", topicOutputDir, 
				"--dictionary", dicFilePath, 
				"--output", topicTermVectorDumpPath, 
				"--dictionaryType", "sequencefile", 
				"--vectorSize", String.valueOf(vectorSize),};*/
		
		String[] arg = new String[] {"--input", topicOutputDir, 
				"--dictionary", dicFilePath,
				"--output", topicTermVectorDumpPath, 
				"--dictionaryType", "sequencefile", 
				"--printKey", "True",};
		
		try {
			VectorDumper.main(arg);
			System.out.println("Vector dumping completes");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
		/*for(int k=0;k<numTopics;k++){
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

	    }*/
	}
	
	private void topicDump() {
		String topicOutputDir = baseDir + "/topic_output";
		String dicFilePath = baseDir + "/vec/dictionary.file-0";
		String topicTermVectorDumpPath = baseDir + "/topicdump/dumpfile_test";
		
		//String[] topicTermDumperArg = {"--input", topicOutputDir, "--output", topicTermVectorDumpPath,  "--dictionary", 
        //        dicFilePath, "-dt", "sequencefile" ,"--vectorSize", "25" ,"-sort", "testsortVectors" };
		
		//b for topics and n for words
		String[] topicTermDumperArg = {"--input", topicOutputDir, "--output", topicTermVectorDumpPath,  "--dictionary", 
                dicFilePath, "-dt", "sequencefile" ,"-b", "20" ,"-n", "20"};
		try {
			ToolRunner.run(new Configuration(), new ClusterDumper(), topicTermDumperArg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printLDATopics() throws Exception {
		String dicFilePath = baseDir + "/vec/dictionary.file-0";
		String rowIdDir = baseDir + "/rowid_vec/matrix";
		String topicOutputDir = baseDir + "/topic_output";
		int numWords = 20;
		String opDicType = "sequencefile";
		String[] arg = new String[]{"--dict", dicFilePath, "-i", topicOutputDir, "-w", String.valueOf(numWords), "-dt", opDicType};
		LDAPrintTopics.main(arg);
	}
	
	private void printResult(Configuration conf, FileSystem fs) {
		try {
			String topicFile = baseDir + "/doc_output/part-m-00000";
			
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(topicFile), conf);
			//Text key = new Text();
			IntWritable key = new IntWritable();
			//IntPairWritable key = new IntPairWritable();
			//IntWritable val = new IntWritable();
			WeightedVectorWritable val = new WeightedVectorWritable();
			//VectorWritable val = new VectorWritable();
			//WeightedPropertyVectorWritable val = new WeightedPropertyVectorWritable();
			
			while(reader.next(key, val)) {
				System.out.println(key.toString() + ": " + val.getVector());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
			//ex.writeDataByToolRunner(conf, fs);
			//ex.executeDirichlet(conf, fs);
			//ex.ldaVectorDump(conf);
			ex.printResult(conf, fs);
			//ex.topicDump();
			//ex.printLDATopics();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
