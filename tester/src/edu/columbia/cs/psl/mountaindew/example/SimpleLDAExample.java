package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.GenericWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ToolRunner;
import org.apache.lucene.analysis.Analyzer;
import org.apache.mahout.clustering.classify.WeightedPropertyVectorWritable;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.apache.mahout.clustering.lda.cvb.CVB0Driver;
import org.apache.mahout.clustering.lda.LDAPrintTopics;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.IntPairWritable;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.StringTuple;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.utils.clustering.ClusterDumper;
import org.apache.mahout.utils.vectors.RowIdJob;
import org.apache.mahout.utils.vectors.VectorDumper;
import org.apache.mahout.utils.vectors.VectorHelper;
import org.apache.mahout.vectorizer.DictionaryVectorizer;
import org.apache.mahout.vectorizer.DocumentProcessor;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;
import org.apache.mahout.vectorizer.tfidf.TFIDFConverter;

import edu.columbia.cs.psl.metamorphic.struct.Word;

public class SimpleLDAExample {
	
	//private static String baseDir = "/Users/mike/Desktop/reuters_ws";
	private static String baseDir = "lda";
	private static int numTopics = 5;
	private static double doc_topic_smooth = 0.0001;
	private static double term_topic_smooth = 0.0001;
	private static int maxIter = 5;
	private static int iterationBlockSize = 10;
	private static double convergenceDelta = 0.0f;
	private static float testFraction = 0.1f;
	private static int numTrainThreads = 4;
	private static int numUpdateThreads = 1;
	private static int maxIterPerDoc = 10;;
	private static int numReduceTasks = 10;
	private static boolean backfillPerplexity = true;
	private static double threshold = 0.00001;
	
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
		arg = new String[] {"-i", seqDir, 
				"-o", vecDir, 
				"-wt", "tfidf", 
				"--minSupport", "2", 
				"--minDF", "1", 
				"--analyzerName", "org.apache.lucene.analysis.WhitespaceAnalyzer"};
		
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
	
	private void convertFilesToSeq(Configuration conf, FileSystem fs) {
		String inputString = baseDir + "/input";
		String seqDirString = baseDir + "/seq";
		
		File inputDir = new File(inputString);
		File seqDir = new File(seqDirString);
		
		if (!inputDir.exists()) {
			System.err.println("Input directory does not exists. Please provide a valid one: " + inputDir.getAbsolutePath());
			return ;
		}
		
		if (!seqDir.exists()) {
			System.out.println("Seqfile directory does not exist. Create one");
			seqDir.mkdir();
		}
		
		System.out.println("Confirm input directory: " + inputDir.getAbsolutePath());
		System.out.println("Confirm seqfile directory: " + seqDir.getAbsolutePath());
		
		String[] arg = new String[] {"-i", inputDir.getAbsolutePath(), "-o", seqDir.getAbsolutePath(), "-c", "UTF-8", "-chunk", "5"};
		
		try {
			ToolRunner.run(conf, new SequenceFilesFromDirectory(), arg);
			System.out.println("Sequenfile conversion completes");
		} catch(Exception ex) {
			ex.printStackTrace();
			System.err.println("Sequencefile conversion fails");
		}
	}
	
	private void tokenizeSeq(Configuration conf, FileSystem fs) {		
		String seqDirString = baseDir + "/seq";
		String vecRootString = baseDir + "/vec";
		
		File seqDir = new File(seqDirString);
		File vecRoot = new File(vecRootString);
		
		if (!seqDir.exists()) {
			System.err.println("Seqfile directory does not exist. Please give a valud one" + seqDir.getAbsolutePath());
			return ;
		}
		
		try {
			HadoopUtil.delete(conf, new Path(vecRoot.getAbsolutePath()));
		} catch(Exception ex) {
			ex.printStackTrace();
			System.err.println("Deletion of vector root directory fails");
		}
		
		System.out.println("Confirm seqfile directory: "+ seqDir.getAbsolutePath());
		System.out.println("Confirm vector root directory: "+ vecRoot.getAbsolutePath());
		
		Path tokenizePath = new Path(vecRoot.getAbsolutePath(), DocumentProcessor.TOKENIZED_DOCUMENT_OUTPUT_FOLDER);
		System.out.println("Start to toeknize documents");
		DirichletAnalyzer analyzer = new DirichletAnalyzer();
		try {
			DocumentProcessor.tokenizeDocuments(new Path(seqDir.getAbsolutePath()),
					analyzer.getClass().asSubclass(Analyzer.class), 
					tokenizePath, 
					conf);
			System.out.println("Tokenizing documents complets");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Tokenizing documents fails");
		}
	}
	
	private void createTF(Configuration conf, FileSystem fs) {
		int minSupport = 1;
	    int maxNGramSize = 1;
	    int minLLRValue = 50;
	    int reduceTasks = 1;
	    int chunkSize = 5;
	    boolean sequentialAccessOutput = true;
	    
	    String vecRootString = baseDir + "/vec";
	    File vecRoot = new File(vecRootString);
	    
	    Path tokenizePath = new Path(vecRoot.getAbsolutePath(), DocumentProcessor.TOKENIZED_DOCUMENT_OUTPUT_FOLDER);
		
		System.out.println("Start to create term frequency files");
		try {
			DictionaryVectorizer.createTermFrequencyVectors(tokenizePath,
					new Path(vecRoot.getAbsolutePath()), DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER, 
					conf, minSupport, maxNGramSize, minLLRValue, 2, true, reduceTasks, chunkSize, sequentialAccessOutput, false);
			System.out.println("Creating term frequency file completes");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Creating term frequencey file fails");
		}
	}
	
	private void createTFIDF(Configuration conf, FileSystem fs) {
		int chunkSize = 200;
	    int minDf = 1;
	    int maxDFPercent = 99;
	    int norm = 2;
	    int reduceTasks = 1;
	    boolean sequentialAccessOutput = true;
		
		String vecRootString = baseDir + "/vec";
		File vecRoot = new File(vecRootString);
		
		try {
			/*Pair<Long[], List<Path>> dfData = TFIDFConverter.calculateDF(
		    		new Path(vecRoot.getAbsolutePath(), DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER),
		    	    new Path(vecRoot.getAbsolutePath()), conf, chunkSize);
			
			System.out.println("Start to create tfidf files");
		    TFIDFConverter.processTfIdf(
		      new Path(vecRoot.getAbsolutePath() , DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER),
		      new Path(vecRoot.getAbsolutePath()), 
		      conf, 
		      dfData, 
		      minDf,
		      maxDFPercent, 
		      norm, 
		      true, 
		      sequentialAccessOutput, 
		      false, 
		      reduceTasks);
		    System.out.println("Complete creation of tfidf files");*/
			
			//Convert seq files to vectors
			String vecDir = baseDir + "/" + "vec";
			String seqDir = baseDir + "/" + "seq";
			String[] arg = new String[] {"-i", seqDir, 
					"-o", vecDir, 
					"-wt", "tfidf", 
					"--minSupport", "2", 
					"--minDF", "1", 
					"--analyzerName", "edu.columbia.cs.psl.mountaindew.example.DirichletAnalyzer"};
			
			try {
				ToolRunner.run(conf, new SparseVectorsFromSequenceFiles(), arg);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			System.out.println("Seq2Vector completes");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Creating tfidf files fails");
		}
	}
	
	private void createRowIdVec(Configuration conf, FileSystem fs) {
		String vecRootString = baseDir + "/vec";
		
		String rowIdVecDir = baseDir + "/" + "rowid_vec";
		String[] arg = new String[] {"-Dmapred.input.dir=" + vecRootString + "/tfidf-vectors/part-r-00000", "-Dmapred.output.dir=" + rowIdVecDir};
		
		try {
			System.out.println("Start to convert row id");
			
			ToolRunner.run(new RowIdJob(), arg);
			
			System.out.println("Converting row id complets");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Converting row id fails");
		}
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
	
	private void executeDirichlet(Configuration conf, FileSystem fs, String rowIdDir) {
		String[] arg = null;
		
		//String tfidfDir = baseDir + "/vec/tfidf-vectors";
		//String rowIdDir = baseDir + "/rowid_vec/matrix";
		String targetInput = rowIdDir;
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
		
		testFile = new File(targetInput);
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
		
		try {
			HadoopUtil.delete(conf, new Path(topicOutputDir));
			HadoopUtil.delete(conf, new Path(docOutputDir));
			HadoopUtil.delete(conf, new Path(tmpDir));
			
			int numTerm = this.getNumTerms(conf, new Path(dicFilePath));
			//int numTerm = 2000;
			System.out.println("Total terms: " + numTerm);
			long seed = System.nanoTime() % 10000;
			
			arg = new String[]{"--input", targetInput, 
					"--output", topicOutputDir, 
					"--num_topics", String.valueOf(numTopics),
					"--num_terms", String.valueOf(numTerm),
					"--doc_topic_smoothing", String.valueOf(doc_topic_smooth),
					"--term_topic_smoothing", String.valueOf(term_topic_smooth),
					"--maxIter", String.valueOf(maxIter),
					"--dictionary", dicFilePath,
					"--doc_topic_output", docOutputDir,
					"--topic_model_temp_dir", tmpDir};
			//CVB0Driver.main(arg);
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
					new Path(tmpDir), seed, testFraction, 
					numTrainThreads, numUpdateThreads, 
					maxIterPerDoc, numReduceTasks, backfillPerplexity);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		System.out.println("Dirichlet execution completes");		
	}
	
	private void ldaVectorDump(Configuration conf, String input, String output) throws IOException {
		//String topicOutputDir = baseDir + "/doc_output";
		String dicFilePath = baseDir + "/vec/dictionary.file-0";
		//String topicTermVectorDumpPath = baseDir + "/topicdump/dumpfile";
		int vectorSize = this.getNumTerms(conf, new Path(dicFilePath));
		System.out.println("Vector size: " + vectorSize);
		String[] arg = new String[] {"--input", input, 
				"--dictionary", dicFilePath, 
				"--output", output, 
				"--dictionaryType", "sequencefile", 
				"--vectorSize", String.valueOf(vectorSize),
				"--printKey", "True"};
		
		/*String[] arg = new String[] {"--input", topicOutputDir, 
				"--dictionary", dicFilePath,
				"--output", topicTermVectorDumpPath, 
				"--dictionaryType", "sequencefile", 
				"--printKey", "True",
				"--vectorSize", String.valueOf(vectorSize)};*/
		
		try {
			VectorDumper.main(arg);
			System.out.println("Vector dumping completes: " + new File(output).getAbsolutePath());
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
	
	private Map<String, List<Word>> getResult(Configuration conf, FileSystem fs) {
		try {
			String topicFile = baseDir + "/topic_output/part-m-00000";
			String dicFilePath = baseDir + "/vec/dictionary.file-0";
			
			String[] dictionary = VectorHelper.loadTermDictionary(conf, dicFilePath);
			
			/*for (int i = 0; i < dictionary.length; i++) {
				System.out.println("Word key vale: " + i + dictionary[i]);
			}*/
			
			int maxEntries = this.getNumTerms(conf, new Path(dicFilePath));
			boolean sort = false;
			
			Map<String, List<Word>> topicMap = new HashMap<String, List<Word>>();
			
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(topicFile), conf);
			
			SequenceFileIterable<Writable, Writable> iterable = new SequenceFileIterable<Writable, Writable>(new Path(topicFile), true, conf);
			Iterator<Pair<Writable, Writable>> iterator = iterable.iterator();
			while(iterator.hasNext()) {
				Pair<Writable, Writable> record = iterator.next();
				Writable keyWritable = record.getFirst();
				Writable valWritable = record.getSecond();
				
				VectorWritable valVecWritable = (VectorWritable)valWritable;
				Vector valVector = valVecWritable.get();
				
				List<Word> wordList = new ArrayList<Word>();
				Word tmpWord;
				double tmpVal;
				
				for (int i = 0; i < valVector.size(); i++) {					
					tmpWord = new Word(dictionary[i], valVector.get(i));
					wordList.add(tmpWord);
				}
				
				topicMap.put(keyWritable.toString(), wordList);
				
				String jsonStyle = VectorHelper.vectorToJson(valVecWritable.get(), dictionary, maxEntries, sort);
				//System.out.println(keyWritable.toString());
				//System.out.println(valWritable.toString());
				System.out.println(jsonStyle);
			}
			
			reader.close();
			return topicMap;
			/*IntWritable key = new IntWritable();
			VectorWritable val = new VectorWritable();
			
			System.out.println("Before printing");
			while(reader.next(key, val)) {
				//System.out.println(key.toString() + ": " + val.toString());
				System.out.println(key.toString());
				System.out.println(val.get());
			}*/
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private void printTokenize(Configuration conf, FileSystem fs) {
		try {
			String tokenFile = baseDir + "/vec/tokenized-documents/part-m-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(tokenFile), conf);
			
			Text key = new Text();
			StringTuple val = new StringTuple();
			while(reader.next(key, val)) {
				System.out.println(key.toString() + ": " + val);
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printTF(Configuration conf, FileSystem fs) {
		try {
			String tfidfFile = baseDir + "/vec/tf-vectors/part-r-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(tfidfFile), conf);
			
			Text key = new Text();
			VectorWritable val = new VectorWritable();
			while(reader.next(key, val)) {
				System.out.println(key.toString() + ": " + val);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printDFCount(Configuration conf, FileSystem fs) {
		try {
			String tfidfFile = baseDir + "/vec/df-count/part-r-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(tfidfFile), conf);
			
			IntWritable key = new IntWritable();
			LongWritable val = new LongWritable();
			while(reader.next(key, val)) {
				System.out.println(key + ": " + val);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printTFIDF(Configuration conf, FileSystem fs) {
		try {
			String tfidfFile = baseDir + "/vec/tfidf-vectors/part-r-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(tfidfFile), conf);
			
			Text key = new Text();
			VectorWritable val = new VectorWritable();
			while(reader.next(key, val)) {
				System.out.println(key.toString() + ": " + val);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printMatrix(Configuration conf, FileSystem fs) {
		try {
			String matrixFile = baseDir + "/rowid_vec/matrix";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(matrixFile), conf);
			
			IntWritable key = new IntWritable();
			VectorWritable val = new VectorWritable();
			while(reader.next(key, val)) {
				System.out.println("Key: " + key.toString());
				Vector tmpVal = val.get();
				System.out.println("Val: " + tmpVal.asFormatString());
				System.out.println("Val change: " + tmpVal.times(2).asFormatString());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printDictionary(Configuration conf, FileSystem fs) {
		try {
			String dicFile = baseDir + "/vec/dictionary.file-0";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(dicFile), conf);
			
			Text key = new Text();
			IntWritable val = new IntWritable();
			
			while(reader.next(key, val)) {
				System.out.println("Term: " + key + " Index: " + val.get());
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printWordcount(Configuration conf, FileSystem fs) {
		try {
			String wordcountFile = baseDir + "vec/wordcount/part-r-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(wordcountFile), conf);
			
			Text key = new Text();
			LongWritable val = new LongWritable();
			
			while(reader.next(key, val)) {
				System.out.println("Term: " + key + "Count: " + val.get());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private Map<String, List<Word>> driveLDA(String rowIdDir) {
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(conf);
			this.convertFilesToSeq(conf, fs);
			this.tokenizeSeq(conf, fs);
			this.printTokenize(conf, fs);
			this.createTFIDF(conf, fs);
			this.createRowIdVec(conf, fs);
			
			this.executeDirichlet(conf, fs, rowIdDir);
			Map<String, List<Word>> topicMap = this.getResult(conf, fs);
			return topicMap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(conf);
			
			SimpleLDAExample ex = new SimpleLDAExample();
			String rowIdDir = "lda/rowid_vec/matrix";
			//Map<String, List<Word>> topicMap = ex.driveLDA(rowIdDir);
			/*ex.convertFilesToSeq(conf, fs);
			ex.tokenizeSeq(conf, fs);
			ex.printTokenize(conf, fs);
			ex.createTFIDF(conf, fs);
			ex.printDFCount(conf, fs);
			ex.printTF(conf, fs);
			ex.printTFIDF(conf, fs);
			ex.printDictionary(conf, fs);
			
			ex.createRowIdVec(conf, fs);
			ex.printMatrix(conf, fs);
			ex.executeDirichlet(conf, fs, rowIdDir);
			Map<String, List<Word>> topicMap = ex.getResult(conf, fs);*/
			
			/*for (String key: topicMap.keySet()) {
				System.out.println("Topic: " + key);
				
				List<Word> wordList = topicMap.get(key);
				for (Word w: wordList) {
					System.out.println("Word: " + w);
				}
			}*/
			
			System.out.println("Execution ends");
			
			//ex.ldaVectorDump(conf, baseDir + "/topic_output", baseDir + "/topicdump/topicdumpfile");
			//ex.ldaVectorDump(conf, baseDir + "/doc_output", baseDir + "/docdump/docdumpfile");
			ex.printMatrix(conf, fs);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Execution ends final");
	}

}
