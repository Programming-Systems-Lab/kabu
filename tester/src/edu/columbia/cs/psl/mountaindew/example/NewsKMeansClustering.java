package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ToolRunner;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.benchmark.utils.ExtractReuters;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.canopy.CanopyDriver;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.clustering.kmeans.RandomSeedGenerator;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.distance.CosineDistanceMeasure;
import org.apache.mahout.common.distance.SquaredEuclideanDistanceMeasure;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.utils.clustering.ClusterDumper;
import org.apache.mahout.vectorizer.DictionaryVectorizer;
import org.apache.mahout.vectorizer.DocumentProcessor;
import org.apache.mahout.vectorizer.tfidf.TFIDFConverter;

public class NewsKMeansClustering {
	
  public static void main(String args[]) throws Exception {
    
    int minSupport = 2;
    int minDf = 5;
    int maxDFPercent = 95;
    int maxNGramSize = 2;
    int minLLRValue = 50;
    int reduceTasks = 1;
    int chunkSize = 200;
    int norm = 2;
    boolean sequentialAccessOutput = true;
    
    String sgmFilePath = "kmeans/input/reuters";
    String convertTextPath = "kmeans/input/reuters_ready";
    String seqFileDirectory = "kmeans/reuters-seqfiles";
    String clusterDirectory = "kmeans/newClusters";
    
    File sgmFiles = new File(sgmFilePath);
    File convertTextFiles = new File(convertTextPath);
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
    System.out.println("Confirm sequence file directory: " + seqFiles.getAbsolutePath());
    System.out.println("Confirm cluster file directory: " + clusters.getAbsolutePath());
    
    //convertTextFiles = new File(convertTextFiles.getAbsolutePath() + "-tmp");
    
    System.out.println("Start to convert sgm files");
    
    ExtractReuters extractor = new ExtractReuters(sgmFiles, convertTextFiles);
    extractor.extract();
    
    System.out.println("Complete sgm-to-text conversion");
    
    //convertTextFiles.renameTo(convertTextFiles);
    
    String[] seqParams = new String[]{"-i", convertTextFiles.getAbsolutePath(), "-o", seqFiles.getAbsolutePath(), "-c",  "UTF-8", "-chunk",  "5"};
    
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
    MyAnalyzer analyzer = new MyAnalyzer();
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
    /*KMeansDriver.run(conf, vectorsFolder, centroids, clusterOutput,
      new SquaredEuclideanDistanceMeasure(), 0.5, 10, true, 0, false);*/
    KMeansDriver.run(conf, vectorsFolder, new Path(centroids, "clusters-0-final"), clusterOutput, new SquaredEuclideanDistanceMeasure(), 0.01, 10, true, 0, false);
    //KMeansDriver.run(conf, vectorsFolder, new Path(centroids, "cluster-1-final"), clusterOutput, new SquaredEuclideanDistanceMeasure(), 0.5, 10, true, 0, true);
    
    SequenceFile.Reader reader = new SequenceFile.Reader(fs,
        new Path(clusterOutput, Cluster.CLUSTERED_POINTS_DIR
                                + "/part-m-00000"), conf);
    
    IntWritable key = new IntWritable();
    WeightedVectorWritable val = new WeightedVectorWritable();
    while (reader.next(key, val)) {
    	System.out.println(val.toString() + " belongs to " + key.toString());
    }
    
    reader.close();
    /*ClusterDumper cDumper = new ClusterDumper();
    cDumper.run(arg0)*/
  }
}
