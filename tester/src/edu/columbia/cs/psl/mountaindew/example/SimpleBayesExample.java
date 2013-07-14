package edu.columbia.cs.psl.mountaindew.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.classifier.naivebayes.training.TrainNaiveBayesJob;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.utils.SplitInput;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;


public class SimpleBayesExample {
	
	private void writeSeqData(Configuration conf, FileSystem fs, String inputFile, String outputDirString) {
		File inputData = new File(inputFile);
		File outputDir = new File(outputDirString);
		File outputFile = new File(outputDirString + "/" + "chunk-0");
		
		if (!inputData.exists()) {
			System.err.println(inputData.getName() + " does not exist.");
			return ;
		}
		
		if (!outputDir.exists()) {
			System.out.println("Starts to create " + outputFile.getAbsolutePath());
			outputDir.mkdir();
		}
		
		BufferedReader br = null;
		SequenceFile.Writer writer;
		Text key = new Text();
		Text value = new Text();
		
		int count = 0;
		
		try {
			br = new BufferedReader(new FileReader(inputData));
			writer = new SequenceFile.Writer(fs, conf, 
					new Path(outputFile.getAbsolutePath()), Text.class, Text.class);
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t", 3);
				
				if (tokens.length != 3) {
					System.err.println("Tokenization error. Skip line: " + line);
					continue;
				}
				
				String category = tokens[0];
				String id = tokens[1];
				String message = tokens[2];
				
				key.set(category + "/" + id);
				value.set(message);
				writer.append(key, value);
				
				count++;
			}
			
			System.out.println("Total entries to write: " + count);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	private void writeDataByToolRunner(Configuration conf, FileSystem fs, String baseDir) throws IOException {
		String[] arg = null;
		
		String input = baseDir + "/" + "input";
		String seqDir = baseDir + "/" + "seq";
		arg = new String[] {"-i", input, "-o", seqDir};

		try {
			//ToolRunner.run(conf, new SequenceFilesFromDirectory(), arg);
			SequenceFilesFromDirectory sffd = new SequenceFilesFromDirectory();
			sffd.setConf(conf);
			sffd.run(arg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("SeqFromDir completes");
		
		String vecDir = baseDir + "/" + "vec";
		arg = new String[] {"-i", seqDir, "-o", vecDir, "-lnorm", "-nv", "-wt", "tfidf"};
		//arg = new String[] {"-i", seqDir, "-o", vecDir, "-wt", "tfidf", "-seq"};
		
		try {
			//ToolRunner.run(conf, new SparseVectorsFromSequenceFiles(), arg);
			SparseVectorsFromSequenceFiles svfsf = new SparseVectorsFromSequenceFiles();
			svfsf.setConf(conf);
			svfsf.run(arg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Seq2Vector completes");
				
		SequenceFile.Reader reader = 
				new SequenceFile.Reader(fs, new Path(vecDir + "/tfidf-vectors/" + "part-r-00000"), conf);
		Text key = new Text();
		VectorWritable value = new VectorWritable();
		while(reader.next(key, value)) {
			Vector vec = value.get();
			System.out.println("Check vec: " + vec);
		}
		
		String trainVecDir = baseDir + "/" + "train";
		String testVecDir = baseDir + "/" + "test";
		String tfidfVecDir = vecDir + "/" + "tfidf-vectors";
		arg = new String[]{"-i", tfidfVecDir, "--trainingOutput", 
				trainVecDir, "--testOutput", testVecDir,
				"--randomSelectionPct", "40", "--overwrite", "--sequenceFiles", "-xm", "sequential"};
		
		try {
			ToolRunner.run(conf, new SplitInput(), arg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Split completes");
	}
	
	private void trainModelByToolRunner(Configuration conf, FileSystem fs, String baseDir) {
		String trainVecDir = baseDir + "/" + "train";
		String modelDir = baseDir + "/" + "model";
		String labelIndexFile = baseDir + "/" + "labelIndex" + "/indexfile";
		String[] arg = new String[] {"-i", trainVecDir, "-o", modelDir, "-li", labelIndexFile, "-el", "-ow"};
		
		try {
			ToolRunner.run(conf, new TrainNaiveBayesJob(), arg);
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
		String baseDir = "bayes";
		String dataSource = "/input/tweets.csv";
		String outputDir = "/seq";
		
		try {
			conf = new Configuration();
			fs = FileSystem.get(conf);
			
			SimpleBayesExample ex = new SimpleBayesExample();
			//ex.writeSeqData(conf, fs, baseDir + dataSource, baseDir + outputDir);
			ex.writeDataByToolRunner(conf, fs, baseDir);
			ex.trainModelByToolRunner(conf, fs, baseDir);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
