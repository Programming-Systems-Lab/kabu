package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.apache.mahout.clustering.lda.cvb.InMemoryCollapsedVariationalBayes0;
import org.apache.mahout.clustering.lda.cvb.ModelTrainer;
import org.apache.mahout.clustering.lda.cvb.TopicModel;
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

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.metamorphic.struct.Word;

public class SimpleLDAExample {
	
	//private static String baseDir = "/Users/mike/Desktop/reuters_ws";
	private int numTopics = 5;
	private double doc_topic_smooth = 0.0001;
	private double term_topic_smooth = 0.0001;
	private int maxIter = 5;
	private int iterationBlockSize = 10;
	private double convergenceDelta = 0.0001f;
	private float testFraction = 0.1f;
	private int numTrainThreads = 1;
	private int numUpdateThreads = 1;
	private int maxIterPerDoc = 10;;
	private int numReduceTasks = 10;
	private boolean backfillPerplexity = true;
	private double threshold = 0.00001;
	private long seed = System.nanoTime() % 10000;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSS");
	
	private Configuration conf;
	
	private FileSystem fs;
		
	public void setConfiguration(Configuration conf) {
		this.conf = conf;
	}
	
	public void setFileSystem(FileSystem fs) {
		this.fs = fs;
	}
	
	private void writeDataByToolRunner(String baseDir, Configuration conf, FileSystem fs) throws IOException {
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
	
	private void convertFilesToSeq(String baseDir) {
		//String inputString = baseDir + "/input";
		//String seqDirString = baseDir + "/seq";
		String baseInputDir = baseDir + "/input";
		String seqDirString = baseDir + "/seq";
		
		File inputDir = new File(baseInputDir);
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
	
	private void tokenizeSeq(String baseDir) {		
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
	
	private void createTF(String baseDir) {
		int minSupport = 2;
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
	
	private void createTFIDF(String baseDir) {
		int chunkSize = 200;
	    int minDf = 1;
	    int maxDFPercent = 99;
	    int norm = 2;
	    int reduceTasks = 1;
	    boolean sequentialAccessOutput = true;
		
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
	
	private void createRowIdVec(String baseDir) {
		String vecRootString = baseDir + "/vec";
		
		String rowIdVecDir = baseDir + "/" + "rowid_vec";
		
		//String[] arg = new String[] {"-Dmapred.input.dir=" + vecRootString + "/tfidf-vectors/part-r-00000", "-Dmapred.output.dir=" + rowIdVecDir};
		String[] arg = new String[] {"-Dmapred.input.dir=" + vecRootString + "/tf-vectors/part-r-00000", "-Dmapred.output.dir=" + rowIdVecDir};
		
		try {
			System.out.println("Start to convert row id");
			
			ToolRunner.run(new RowIdJob(), arg);
			
			System.out.println("Converting row id complets");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Converting row id fails");
		}
	}
	
	private int getNumTerms(Path dictionaryPath) throws IOException {
        Text key = new Text();
        IntWritable value = new IntWritable();
        int maxTermId = -1;
        SequenceFile.Reader reader = null;
        for (FileStatus stat : fs.globStatus(dictionaryPath)) {
            reader = new SequenceFile.Reader(fs, stat.getPath(), conf);
            while (reader.next(key, value)) {
                maxTermId = Math.max(maxTermId, value.get());
            }
            reader.close();
        }
        return maxTermId + 1;
    }
	
	private void executeDirichlet(String baseDir) {
		String[] arg = null;
		
		//String tfidfDir = baseDir + "/vec/tfidf-vectors";
		//String rowIdDir = baseDir + "/rowid_vec/matrix";
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
		
		try {			
			HadoopUtil.delete(conf, new Path(topicOutputDir));
			HadoopUtil.delete(conf, new Path(docOutputDir));
			HadoopUtil.delete(conf, new Path(tmpDir));
			
			int numTerm = this.getNumTerms(new Path(dicFilePath));
			//int numTerm = 2000;
			System.out.println("Total terms: " + numTerm);			
			arg = new String[]{"--input", rowIdDir, 
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
			System.out.println("Check seed for lda: " + this.seed);
			int result = CVB0Driver.run(conf, 
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
			
			System.out.println("CVB0 result: " + result);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		
		System.out.println("Dirichlet execution completes");
	}
	
	private void ldaVectorDump(String dictionary, String input, String output) throws IOException {
		//String topicOutputDir = baseDir + "/doc_output";
		//String topicTermVectorDumpPath = baseDir + "/topicdump/dumpfile";
		int vectorSize = this.getNumTerms(new Path(dictionary));
		System.out.println("Vector size: " + vectorSize);
		String[] arg = new String[] {"--input", input, 
				"--dictionary", dictionary, 
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
	
	private void topicDump(String baseDir) {
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
	
	private void printLDATopics(String baseDir) throws Exception {
		String dicFilePath = baseDir + "/vec/dictionary.file-0";
		String rowIdDir = baseDir + "/rowid_vec/matrix";
		String topicOutputDir = baseDir + "/topic_output";
		int numWords = 20;
		String opDicType = "sequencefile";
		String[] arg = new String[]{"--dict", dicFilePath, "-i", topicOutputDir, "-w", String.valueOf(numWords), "-dt", opDicType};
		LDAPrintTopics.main(arg);
	}
	
	private Map<String, List<Word>> getResult(String baseOutputDir) {
		try {
			String topicFile = baseOutputDir + "/topic_output/part-m-00000";
			String dicFilePath = baseOutputDir + "/vec/dictionary.file-0";
			
			String[] dictionary = VectorHelper.loadTermDictionary(conf, dicFilePath);
			
			/*for (int i = 0; i < dictionary.length; i++) {
				System.out.println("Word key vale: " + i + dictionary[i]);
			}*/
			
			int maxEntries = this.getNumTerms(new Path(dicFilePath));
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
	
	private void printTokenize(String baseOutputDir) {
		try {
			String tokenFile = baseOutputDir + "/vec/tokenized-documents/part-m-00000";
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
	
	private void printTF(String baseDir) {
		try {
			String tfidfFile = baseDir + "/vec/tf-vectors/part-r-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(tfidfFile), conf);
			
			Text key = new Text();
			VectorWritable val = new VectorWritable();
			while(reader.next(key, val)) {
				System.out.println(key.toString() + ": " + val);
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printTFGeneral(String filePath) {
		try {
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(filePath), conf);
			
			Text key = new Text();
			VectorWritable val = new VectorWritable();
			while(reader.next(key, val)) {
				System.out.println(key.toString() + ": " + val);
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void multiplyTF(String baseDir,String destDir) {
		try {
			String tfidfFile = baseDir + "/vec/tf-vectors/part-r-00000";
			String tfidfFile2 = destDir + "/vec/tf-vectors/part-r-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(tfidfFile), conf);
			
			SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, new Path(tfidfFile2), Text.class, VectorWritable.class);
			
			Text key = new Text();
			VectorWritable val = new VectorWritable();
			Vector tmpVec;
			while(reader.next(key, val)) {
				//System.out.println(key.toString() + ": " + val);
				tmpVec = val.get().times(2);
				writer.append(key, new VectorWritable(tmpVec));
			}
			reader.close();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void multiplyMatrix(String baseDir, String destDir) {
		try {
			String oriFile = baseDir + "/rowid_vec/matrix";
			String transFile = destDir + "/rowid_vec/matrix";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(oriFile), conf);
			
			SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf , new Path(transFile), IntWritable.class, VectorWritable.class);
			
			IntWritable key = new IntWritable();
			VectorWritable val = new VectorWritable();
			Vector tmpVec;
			while(reader.next(key, val)) {
				tmpVec = val.get().times(2);
				writer.append(key, new VectorWritable(tmpVec));
			}
			reader.close();
			writer.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printDFCount(String baseDir) {
		try {
			String tfidfFile = baseDir + "/vec/df-count/part-r-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(tfidfFile), conf);
			
			IntWritable key = new IntWritable();
			LongWritable val = new LongWritable();
			while(reader.next(key, val)) {
				System.out.println(key + ": " + val);
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printTFIDF(String baseDir) {
		try {
			String tfidfFile = baseDir + "/vec/tfidf-vectors/part-r-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(tfidfFile), conf);
			
			Text key = new Text();
			VectorWritable val = new VectorWritable();
			while(reader.next(key, val)) {
				System.out.println(key.toString() + ": " + val);
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printMatrix(String baseDir) {
		try {
			String matrixFile = baseDir + "/rowid_vec/matrix";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(matrixFile), conf);
			
			IntWritable key = new IntWritable();
			VectorWritable val = new VectorWritable();
			while(reader.next(key, val)) {
				System.out.println("Key: " + key.toString());
				Vector tmpVal = val.get();
				System.out.println("Val: " + tmpVal.asFormatString());
				//System.out.println("Val change: " + tmpVal.times(2).asFormatString());
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printDictionary(String baseDir) {
		try {
			String dicFile = baseDir + "/vec/dictionary.file-0";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(dicFile), conf);
			
			Text key = new Text();
			IntWritable val = new IntWritable();
			
			while(reader.next(key, val)) {
				System.out.println("Term: " + key + " Index: " + val.get());
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printFrequency(String baseDir) {
		try {
			String dicFile = baseDir + "/vec/frequency.file-0";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(dicFile), conf);
			
			IntWritable key = new IntWritable();
			LongWritable val = new LongWritable();
			
			while(reader.next(key, val)) {
				System.out.println("Key: " + key + " Val: " + val);
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printWordcount(String baseDir) {
		try {
			String wordcountFile = baseDir + "/vec/wordcount/part-r-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(wordcountFile), conf);
			
			Text key = new Text();
			LongWritable val = new LongWritable();
			
			while(reader.next(key, val)) {
				System.out.println("Term: " + key + " Count: " + val.get());
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Metamorphic
	private Map<String, List<Word>> driveLDA(String baseDir) {		
		Map<String, List<Word>> topicMap = null;
		try {
			this.executeDirichlet(baseDir);
			topicMap = this.getResult(baseDir);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		if (topicMap != null) {
			for (String key: topicMap.keySet()) {
				System.out.println("Topic: " + key);
			
				List<Word> wordList = topicMap.get(key);
				for (Word w: wordList) {
				System.out.println("Word: " + w);
				}
			}
		}
		
		return topicMap;
	}
	
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		
		String pattern = "pool-[0-9]+-thread-[0-9]+";

		try {
			SimpleLDAExample ex = new SimpleLDAExample();
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(conf);
			ex.setConfiguration(conf);
			ex.setFileSystem(fs);
			String baseDir = "lda";

			ex.convertFilesToSeq(baseDir);
			
			ex.tokenizeSeq(baseDir);
			System.out.println("Check tokinized document");
			ex.printTokenize(baseDir);
			System.out.println();

			ex.createTFIDF(baseDir);
			System.out.println("Check term frequency");
			ex.printTF(baseDir);
			System.out.println();
			
			System.out.println("Check word count");
			ex.printWordcount(baseDir);
			System.out.println();
			
			System.out.println("Check dictionary");
			ex.printDictionary(baseDir);
			System.out.println();
			
			System.out.println("Check frequency");
			ex.printFrequency(baseDir);
			System.out.println();
			
			System.out.println("Check DF");
			ex.printDFCount(baseDir);
			System.out.println();
			
			
			/*ex.multiplyTF(baseDir);
			ex.printTFGeneral(baseDir + "/vec/tf-vectors/part-r-00000");
			System.out.println();
			ex.printTFGeneral(baseDir + "/vec/tf-vectors/part-r-00001");*/
			
			System.out.println("Check row vector");
			ex.createRowIdVec(baseDir);
			ex.printMatrix(baseDir);
			
			Map<String, List<Word>> topicMap = ex.driveLDA(baseDir);
			//Map<String, List<Word>>topicMap = ex.driveLDA("lda_copy");
			
			/*if (topicMap != null) {
				for (String key: topicMap.keySet()) {
					System.out.println("Topic: " + key);
				
					List<Word> wordList = topicMap.get(key);
					for (Word w: wordList) {
					System.out.println("Word: " + w);
					}
				}
			}*/
			//ex.ldaVectorDump("lda/vec/dictionary.file-0", "lda/topic_output", "lda/topicdump/topicdumpfile");
			//ex.ldaVectorDump("lda/vec/dictionary.file-0", "lda/doc_output", "lda/docdump/docdumpfile");
			//ex.driveLDA("lda_copy");

			/*System.out.println("Dictionary");
			ex.printDictionary("lda");
			System.out.println();
			ex.printDictionary("lda_copy");
			System.out.println();*/
			
			/*System.out.println("TF");
			ex.printTF("lda");
			System.out.println();
			ex.printTF("lda_copy");
			System.out.println();*/
			
			/*System.out.println("Matrix");
			ex.printMatrix("lda");
			System.out.println();
			ex.printMatrix("lda_copy");
			System.out.println();*/
			
			/*System.out.println("DF");
			ex.printDFCount("lda");
			System.out.println();
			ex.printDFCount("lda_copy");
			System.out.println();*/
			
			/*System.out.println("Word count");
			ex.printWordcount("lda");
			System.out.println();
			ex.printWordcount("lda_copy");
			System.out.println();*/
			
			/*System.out.println("Frequency");
			ex.printFrequency("lda");
			System.out.println();
			ex.printFrequency("lda_copy");*/
			
			/*Map<String, List<Word>> topicMap = ex.driveLDA("lda");
			
			if (topicMap != null) {
				for (String key: topicMap.keySet()) {
					System.out.println("Topic: " + key);
				
					List<Word> wordList = topicMap.get(key);
					for (Word w: wordList) {
						System.out.println("Word: " + w);
					}
				}
			}
			
			System.out.println("Result");
			System.out.println(ex.getResult("lda"));*/
			
			/*ex.multiplyMatrix("lda", "lda_copy");
			System.out.println("Original matrix");
			ex.printMatrix("lda");
			System.out.println("Transfomr matrix");
			ex.printMatrix("lda_copy");
			
			Map<String, List<Word>> topicMap2 = ex.driveLDA("lda_copy");
			
			if (topicMap2 != null) {
				for (String key: topicMap2.keySet()) {
					System.out.println("Topic: " + key);
					
					List<Word> wordList = topicMap2.get(key);
					for (Word w: wordList) {
						System.out.println("Word: " + w);
					}
				}
			}
			
			
			System.out.println();
			System.out.println(ex.getResult("lda_copy"));*/
			
			/*System.out.println(ex.getResult("lda"));
			System.out.println(ex.getResult("lda_copy"));*/
			//ex.ldaVectorDump("lda/vec/dictionary.file-0", "lda/topic_output", "lda/topicdump/topicdumpfile");
			//ex.ldaVectorDump("lda_copy/vec/dictionary.file-0", "lda_copy/topic_output", "lda_copy/topicdump/topicdumpfile");
			
			//ex.ldaVectorDump(conf, baseDir + "/vec/dictionary.file-0", baseDir + "/topic_output", baseDir + "/topicdump/topicdumpfile");
			//ex.ldaVectorDump(conf, "lda_copy" + "/vec/dictionary.file-0", "lda_copy" + "/topic_output", "lda_copy" + "/topicdump/topicdumpfile");
			//ex.ldaVectorDump(conf, baseDir + "/vec/dictionary.file-0", baseDir + "/doc_output", baseDir + "/docdump/docdumpfile");
			
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		
		/*try {
			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
			for (Thread tmp: threadSet) {
				System.out.println("Check thread: " + tmp.getName() + " " + tmp.isDaemon());
				
				if (tmp.getName().matches(pattern)) {
					System.out.println("Got thread: " + tmp.getName());
					tmp.interrupt();
				}
			}
		} catch (Exception ex) {
			Thread.currentThread().interrupt();
			ex.printStackTrace();
		}*/
	}
}
