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
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.clustering.kmeans.RandomSeedGenerator;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.distance.CosineDistanceMeasure;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.vectorizer.DictionaryVectorizer;
import org.apache.mahout.vectorizer.DocumentProcessor;
import org.apache.mahout.vectorizer.tfidf.TFIDFConverter;

public class NewsKMeansClustering {
	
  public static void main(String args[]) throws Exception {
    
    int minSupport = 5;
    int minDf = 5;
    int maxDFPercent = 99;
    int maxNGramSize = 1;
    int minLLRValue = 50;
    int reduceTasks = 1;
    int chunkSize = 200;
    int norm = -1;
    boolean sequentialAccessOutput = true;
    
    String[] seqParams = new String[]{"-i", "kmeans/input/reuters/", "-o", 
    		"kmeans/reuters-seqfiles", "-c",  "UTF-8", "-chunk",  String.valueOf(chunkSize)};
    
    ToolRunner.run(new SequenceFilesFromDirectory(), seqParams);
        
    String inputDir = "kmeans/reuters-seqfiles";
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);

    String outputDir = "kmeans/newClusters";
    HadoopUtil.delete(conf, new Path(outputDir));
    Path tokenizedPath = new Path(outputDir,
        DocumentProcessor.TOKENIZED_DOCUMENT_OUTPUT_FOLDER);
    MyAnalyzer analyzer = new MyAnalyzer();
    DocumentProcessor.tokenizeDocuments(new Path(inputDir), analyzer
        .getClass().asSubclass(Analyzer.class), tokenizedPath, conf);

    DictionaryVectorizer.createTermFrequencyVectors(tokenizedPath,
      new Path(outputDir), DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER,
      conf, minSupport, maxNGramSize, minLLRValue, 2, true, reduceTasks,
      chunkSize, sequentialAccessOutput, false);
    Pair<Long[], List<Path>> dfData = TFIDFConverter.calculateDF(
    		new Path(outputDir, DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER),
    	    new Path(outputDir), conf, chunkSize);   
    TFIDFConverter.processTfIdf(
      new Path(outputDir , DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER),
      new Path(outputDir), conf, dfData, minDf,
      maxDFPercent, norm, true, sequentialAccessOutput, false, reduceTasks);
    Path vectorsFolder = new Path(outputDir, "tfidf-vectors");
    Path centroids = new Path(outputDir, "centroids");
    Path clusterOutput = new Path(outputDir, "clusters");
    
    RandomSeedGenerator.buildRandom(conf, vectorsFolder, centroids, 20,
      new CosineDistanceMeasure());
    KMeansDriver.run(conf, vectorsFolder, centroids, clusterOutput,
      new CosineDistanceMeasure(), 0.01, 5, true, 0, false);
    
    SequenceFile.Reader reader = new SequenceFile.Reader(fs,
        new Path(clusterOutput, Cluster.CLUSTERED_POINTS_DIR
                                + "/part-m-00000"), conf);
    
    IntWritable key = new IntWritable();
    WeightedVectorWritable val = new WeightedVectorWritable();
    while (reader.next(key, val)) {
    	System.out.println(val.toString() + " belongs to " + key.toString());
    }
    
    reader.close();
  }
}
