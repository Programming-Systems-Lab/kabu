package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ToolRunner;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.benchmark.utils.ExtractReuters;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.canopy.CanopyDriver;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.apache.mahout.clustering.iterator.ClusterWritable;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.clustering.kmeans.Kluster;
import org.apache.mahout.clustering.kmeans.RandomSeedGenerator;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.StringTuple;
import org.apache.mahout.common.distance.CosineDistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.common.distance.SquaredEuclideanDistanceMeasure;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.utils.clustering.ClusterDumper;
import org.apache.mahout.utils.vectors.VectorHelper;
import org.apache.mahout.vectorizer.DictionaryVectorizer;
import org.apache.mahout.vectorizer.DocumentProcessor;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;
import org.apache.mahout.vectorizer.tfidf.TFIDFConverter;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.metamorphic.struct.Word;

public class KMeansTextExample {
	
	private static String clusterFinalPattern = "clusters-[0-9]+-final";
	
	private Configuration conf;
	
	private FileSystem fs;
	
	int kNum = 1;
	
	public void setConfiguration(Configuration conf) {
		this.conf = conf;
	}
	
	public void setFileSystem(FileSystem fs) {
		this.fs = fs;
	}
	
	private void dumpClusters(String baseDir) {		
		String clusterRoot = baseDir + "/vec/clusters";
		File clusterRootFile = new File(clusterRoot);
		File[] clusterChilds = clusterRootFile.listFiles();
		String finalClusterDir = null;
		for (int i = 0; i < clusterChilds.length; i++) {
			if (clusterChilds[i].getName().matches(clusterFinalPattern)) {
				finalClusterDir = clusterChilds[i].getAbsolutePath();
			}
		}
		
		
		String dict = baseDir + "/vec/dictionary.file-0";
		String clusterFile = finalClusterDir + "/part-r-00000";
		String outputFile = baseDir + "/clusterdump/cluster.txt";
		String clusteredPoints = baseDir + "/vec/clusters/clusteredPoints";
		String[] args = new String[]{"-d", dict, "-dt", "sequencefile", "-i", clusterFile, "-n", "20", "-b", "100", "-o", outputFile, "-p", clusteredPoints};
		
		try {
			ToolRunner.run(conf, new ClusterDumper(), args);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
	
	private void createTFIDF(String baseDir) {
		int chunkSize = 200;
	    int minDf = 1;
	    int maxDFPercent = 99;
	    int norm = 2;
	    int reduceTasks = 1;
	    boolean sequentialAccessOutput = true;
		
		try {
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
	
	private void createCentroids(String baseDir, String matricDir) {
		System.out.println("Initialize cluster centroids");
	    //String vecDir = baseDir + "/vec";
	    //String centorids = baseDir + "/centroids";
	    
	    //Path tfidfPath = new Path(vecDir, "tfidf-vectors");
	    //Path centroids = new Path(vecDir, "centroids");
	    
	    Path tfidfPath = new Path(baseDir + matricDir);
	    //Path vecDir = tfidfPath.getParent();
	    String vecDir = baseDir + "/" + tfidfPath.getParent().getName();
	    
	    Path centroids = new Path(vecDir, "centroids");
	    System.out.println("Confirm vec path: " + (new File(vecDir)).getAbsolutePath());
	    System.out.println("Confrim centroids path: " + (new File(centroids.toString())).getAbsolutePath());
	    System.out.println("Confirm matric path: " + (new File(tfidfPath.toString())).getAbsolutePath());
	    
	    try {
	    	HadoopUtil.delete(conf, centroids);
	    	CanopyDriver.run(tfidfPath, centroids, new SquaredEuclideanDistanceMeasure(), 250, 120, false, 20, false);
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    }
	}
	
	public void writeKluster(String baseDir) {
		List<Vector> vectors = new ArrayList<Vector>();
		try {
			String tfidfFile = baseDir + "/vec/tfidf-vectors/part-r-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(tfidfFile), conf);
			
			Text key = new Text();
			VectorWritable val = new VectorWritable();
			while(reader.next(key, val)) {
				System.out.println(key.toString() + ": " + val);
				vectors.add(val.get());
			}
			reader.close();
			
			Path path = new Path(baseDir + "/vec/centroids/part-r-00000");
			SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, path, Text.class, ClusterWritable.class);
			
			for (int i = 0; i < kNum; i++) {
				Vector vec = vectors.get(i);
				System.out.println("Print vec: " + vec.toString());
				Kluster kluster = new Kluster(vec, i, new EuclideanDistanceMeasure());
				ClusterWritable cw = new ClusterWritable();
				cw.setValue(kluster);
				writer.append(new Text(kluster.getIdentifier()), cw);
			}
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
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
	
	private void printCentroids(String baseDir) {
		try {
			//String clusterFile = baseDir + "/vec/centroids/clusters-0-final/part-r-00000";
			String clusterFile = baseDir + "/vec/centroids/part-r-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(clusterFile), conf);
			
			Text key = new Text();
			ClusterWritable val = new ClusterWritable();
			
			while(reader.next(key, val)) {
				System.out.println("Int: " + key + " cluster: " + val.toString());
			}
			reader.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printClusters(String baseDir) {
		try {
			String clusterFile = baseDir + "/vec/clusters/clusters-2-final/part-r-00000";
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(clusterFile), conf);
			
			IntWritable key = new IntWritable();
			ClusterWritable val = new ClusterWritable();
			
			while(reader.next(key, val)) {
				System.out.println("Int: " + key + " cluster: " + val.getValue().toString());
			}
			reader.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void executeKMeans(String baseDir, String matricDir) {
		System.out.println("Start to execte KMeans algorithm");
		
		Path tfidfPath = new Path(baseDir + matricDir);
		Path vecDir = tfidfPath.getParent();
		
		System.out.println("Confirm vec root: " + vecDir.getName());
		
		
		String vecDirString = baseDir + "/" + vecDir.getName();
		String centroids = vecDirString + "/centroids";
		String clusters = vecDirString + "/clusters";
		
		
		Path clusterOutput = new Path(clusters);
		
		System.out.println("Check cluster output: " + clusterOutput.toString());
		File centroidDir = new File(centroids);
		System.out.println("Cluster centroids: " + centroidDir.getAbsolutePath());
		File[] centroidChilds = centroidDir.listFiles();
		String finalCentroid = null;
		/*for (int i = 0; i < centroidChilds.length; i++) {
			System.out.println("Check cluster child files: " + centroidChilds[i].getName());
			if (centroidChilds[i].getName().matches(clusterFinalPattern)) {
				System.out.println("Got the file: " + centroidChilds[i].getAbsolutePath());
				finalCentroid = centroidChilds[i].getAbsolutePath();
				break;
			}
		}*/
		
		finalCentroid = centroids + "/part-r-00000";
		
		try {
			HadoopUtil.delete(conf, clusterOutput);
			KMeansDriver.run(conf, tfidfPath, new Path(finalCentroid), clusterOutput, new SquaredEuclideanDistanceMeasure(), 0.01, 10, true, 0, false);
		} catch (Exception ex) {
			ex.printStackTrace();
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
	
	private List<Vector> getResult(String baseDir) {
		List<Vector> clusterResult = new ArrayList<Vector>();
		String vecDir = baseDir + "/vec";
    	Path clusterOutput = new Path(vecDir, "clusters");
    	
    	try {
    		SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(clusterOutput, Cluster.CLUSTERED_POINTS_DIR + "/part-m-00000"), conf);
        	
        	IntWritable key = new IntWritable();
        	WeightedVectorWritable value = new WeightedVectorWritable();
        	
        	while (reader.next(key, value)) {
        		System.out.println("Check clustered result key val " + key + " " + value.getVector());
        		clusterResult.add(value.getVector());
        	}
        	
        	reader.close();
        	
        	return clusterResult;
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	return null;
	}
		
	@Metamorphic
	private List<Vector> driveKMeans(String[] dirInfo) {
		//dirInfo[0]: baseDirectory, dirInfo[1]:tfidf, tf or matrix
		//this.createCentroids(dirInfo[0], dirInfo[1]);
		this.writeKluster(dirInfo[0]);
		this.executeKMeans(dirInfo[0], dirInfo[1]);
		return this.getResult(dirInfo[0]);
	}
	
	public static void main(String args[]) {
		int minSupport = 2;
	    int minDf = 5;
	    int maxDFPercent = 95;
	    int maxNGramSize = 1;
	    int minLLRValue = 50;
	    int reduceTasks = 1;
	    int chunkSize = 5;
	    int norm = 2;
	    boolean sequentialAccessOutput = true;
	    
	    String baseDir = "kmeans";
	    //String baseDir = "kmeans_sandbox/20130804004350194";
	    Configuration conf;
	    FileSystem fs;
	    
	    try {
	    	conf = new Configuration();
	    	fs = FileSystem.get(conf);
	    	
	    	KMeansTextExample kt = new KMeansTextExample();
	    	kt.setConfiguration(conf);
	    	kt.setFileSystem(fs);
	    	
	    	kt.convertFilesToSeq(baseDir);
	    	
	    	kt.tokenizeSeq(baseDir);
	    	System.out.println("Check tokenized document");
	    	kt.printTokenize(baseDir);
	    	
	    	kt.createTFIDF(baseDir);
	    	System.out.println("Check tfidf");
	    	kt.printTFIDF(baseDir);
	    	
	    	System.out.println("Check dictionary");
	    	kt.printDictionary(baseDir);
	    	
	    	String[] dirInfo = new String[]{baseDir, "/vec/tfidf-vectors"};
	    	//String[] dirInfo = new String[]{"kmeans_sandbox/2013080221482866", "/vec/tfidf-vectors"};
	    	
	    	List<Vector> resultList = kt.driveKMeans(dirInfo);
	    	
	    	for (Vector tmpVec: resultList) {
	    		System.out.println("Check result vec: " + tmpVec.asFormatString());
	    	}
	    	
	    	/*kt.printTFIDF(baseDir);
	    	kt.printCentroids(baseDir);
	    	kt.printClusters(baseDir);
	    	kt.dumpClusters(baseDir);*/
	    	/*kt.createCentroids(baseDir);
	    	kt.executeKMeans(baseDir);
	    	
	    	kt.printCentroids(baseDir);
	    	
	    	String vecDir = baseDir + "/vec";
	    	Path clusterOutput = new Path(vecDir, "clusters");
	    	SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(clusterOutput, Cluster.CLUSTERED_POINTS_DIR + "/part-m-00000"), conf);
	    	
	    	IntWritable key = new IntWritable();
	    	WeightedVectorWritable value = new WeightedVectorWritable();
	    	
	    	while (reader.next(key, value)) {
	    		System.out.println("Check clustered result" + value.getVector());
	    	}
	    	
	    	reader.close();
	    	
	    	kt.dumpClusters(baseDir);*/
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    }
	}
	
	
  /*public static void main(String args[]) throws Exception {
    
    int minSupport = 2;
    int minDf = 5;
    int maxDFPercent = 95;
    int maxNGramSize = 1;
    int minLLRValue = 50;
    int reduceTasks = 1;
    int chunkSize = 5;
    int norm = 2;
    boolean sequentialAccessOutput = true;
    
    String sgmFilePath = "kmeans/input/reuters";
    String convertTextPath = "kmeans/input/reuters_ready";
    
    String inputFilePath = "kmeans/input";
    String seqFileDirectory = "kmeans/seq";
    String clusterDirectory = "kmeans/vec";
    
    File sgmFiles = new File(sgmFilePath);
    File convertTextFiles = new File(convertTextPath);
    File inputFiles = new File(inputFilePath);
    File seqFiles = new File(seqFileDirectory);
    File clusters = new File(clusterDirectory);
    
    if (!sgmFiles.exists()) {
    	System.out.println("Please give valid sgm file directory");
    	return ;
    }
    
    if (!convertTextFiles.exists()) {
    	System.out.println("Please give valid text file directory for conversion");
    	return ;
    }
    
    if (!inputFiles.exists()) {
    	System.out.println("Please give valid input file directory");
    }
    
    if (!seqFiles.exists()) {
    	System.out.println("Please give valid seq file directory for conversion");
    	return ;
    }
    
    if (!clusters.exists()) {
    	System.out.println("Please give valid cluste file directory for conversion");
    	return ;
    }
    
    System.out.println("Confirm raw data directory: " + sgmFiles.getAbsolutePath());
    System.out.println("Confirm converted text file directory: " + convertTextFiles.getAbsolutePath());
    System.out.println("Confirm input file directory: " + inputFiles.getAbsolutePath());
    System.out.println("Confirm sequence file directory: " + seqFiles.getAbsolutePath());
    System.out.println("Confirm cluster file directory: " + clusters.getAbsolutePath());
    
    //convertTextFiles = new File(convertTextFiles.getAbsolutePath() + "-tmp");
    
    System.out.println("Start to convert sgm files");
    
    ExtractReuters extractor = new ExtractReuters(sgmFiles, convertTextFiles);
    extractor.extract();
    
    System.out.println("Complete sgm-to-text conversion");
    
    //convertTextFiles.renameTo(convertTextFiles);
    
    //String[] seqParams = new String[]{"-i", convertTextFiles.getAbsolutePath(), "-o", seqFiles.getAbsolutePath(), "-c",  "UTF-8", "-chunk",  "5"};
    String[] seqParams = new String[]{"-i", inputFiles.getAbsolutePath(), "-o", seqFiles.getAbsolutePath(), "-c",  "UTF-8", "-chunk",  "5"};
    
    //Convert raw data into sequence file format
    System.out.println("Start to convert text files to sequence files");
    ToolRunner.run(new SequenceFilesFromDirectory(), seqParams);
    //equenceFilesFromDirectory seqdirectory = new SequenceFilesFromDirectory();
    //seqdirectory.run(seqParams);
    System.out.println("Complete text-to-seq conversion");
        
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);

    //Convert sequence files into token array and put them in output/tokenized-documents
    HadoopUtil.delete(conf, new Path(clusters.getAbsolutePath()));
    Path tokenizedPath = new Path(clusters.getAbsolutePath(),
        DocumentProcessor.TOKENIZED_DOCUMENT_OUTPUT_FOLDER);
    
    System.out.println("Start to tokenize seq files");
    //MyAnalyzer analyzer = new MyAnalyzer();
    DirichletAnalyzer analyzer = new DirichletAnalyzer();
    DocumentProcessor.tokenizeDocuments(new Path(seqFileDirectory), analyzer
        .getClass().asSubclass(Analyzer.class), tokenizedPath, conf);
    System.out.println("Complete tokenization of seq files");
    
    //Read token arrays and convert them into term frequency vectors, put them into output/tf-vectors
    System.out.println("Start to create term frequency files");
    DictionaryVectorizer.createTermFrequencyVectors(tokenizedPath,
      new Path(clusters.getAbsolutePath()), DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER,
      conf, minSupport, maxNGramSize, minLLRValue, 2, true, reduceTasks,
      chunkSize, sequentialAccessOutput, false);
    System.out.println("Complete creation of term frequency files");
    
    Pair<Long[], List<Path>> dfData = TFIDFConverter.calculateDF(
    		new Path(clusters.getAbsolutePath(), DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER),
    	    new Path(clusters.getAbsolutePath()), conf, chunkSize);   
    
    //Convert term frequency vectors to TFIDF-vectors, put them into output/tfidf-vectors
    System.out.println("Start to create tfidf files");
    TFIDFConverter.processTfIdf(
      new Path(clusters.getAbsolutePath() , DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER),
      new Path(clusters.getAbsolutePath()), conf, dfData, minDf,
      maxDFPercent, norm, true, sequentialAccessOutput, false, reduceTasks);
    System.out.println("Complete creation of tfidf files");
    
    Path vectorsFolder = new Path(clusters.getAbsolutePath(), "tfidf-vectors");
    Path centroids = new Path(clusters.getAbsolutePath(), "centroids");
    Path clusterOutput = new Path(clusters.getAbsolutePath(), "clusters");
    
    System.out.println("Initialize cluster centroids");
    //RandomSeedGenerator.buildRandom(conf, vectorsFolder, centroids, 20, new SquaredEuclideanDistanceMeasure());
    //CanopyDriver.run(vectorsFolder, centroids, new SquaredEuclideanDistanceMeasure(), 250, 120, runClustering, clusterClassificationThreshold, runSequential)
    CanopyDriver.run(vectorsFolder, centroids, new SquaredEuclideanDistanceMeasure(), 250, 120, false, 20, false);
    
    System.out.println("Execute KMeans algorithm");
    KMeansDriver.run(conf, vectorsFolder, new Path(centroids, "clusters-0-final"), clusterOutput, new SquaredEuclideanDistanceMeasure(), 0.01, 10, true, 0, false);
    
    SequenceFile.Reader reader = new SequenceFile.Reader(fs,
        new Path(clusterOutput, Cluster.CLUSTERED_POINTS_DIR
                                + "/part-m-00000"), conf);
    
    IntWritable key = new IntWritable();
    WeightedVectorWritable val = new WeightedVectorWritable();
    while (reader.next(key, val)) {
    	System.out.println(val.toString() + " belongs to " + key.toString());
    }
    
    reader.close();
  }*/
}
