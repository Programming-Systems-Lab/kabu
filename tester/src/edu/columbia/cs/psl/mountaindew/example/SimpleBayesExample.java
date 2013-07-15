package edu.columbia.cs.psl.mountaindew.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.classifier.naivebayes.test.TestNaiveBayesDriver;
import org.apache.mahout.classifier.naivebayes.training.TrainNaiveBayesJob;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.utils.SplitInput;
import org.apache.mahout.utils.vectors.io.SequenceFileVectorWriter;
import org.apache.mahout.utils.vectors.io.VectorWriter;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;


public class SimpleBayesExample {
	
	public List<NamedVector> readCSV(String inputPath) {
		File csvPath = new File(inputPath);
		
		if (!csvPath.exists()) {
			System.out.println("Raw data file does not exist");
			return null;
		}
		
		FileSystem fs = null;
		SequenceFile.Writer writer;
		Configuration conf = new Configuration();
		Path path;
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(inputPath));
			List<NamedVector> vectors = new ArrayList<NamedVector>();
			String tmpString = null;
			while ((tmpString = br.readLine()) != null) {
				String[] sp = tmpString.split(",");
				double[] doubleVal = new double[sp.length - 1];
				
				for (int i = 0 ; i < sp.length - 1; i++) {
					doubleVal[i] = Double.valueOf(sp[i]);
				}
				Vector vec = new DenseVector(doubleVal.length);
				vec.assign(doubleVal);
				
				NamedVector nVec = new NamedVector(vec, sp[sp.length - 1]);
				
				vectors.add(nVec);
			}
			return vectors;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
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
			while (true) {
				line = br.readLine();
				if (line == null) {
					break;
				}
				
				String[] tokens = line.split("\t", 3);
				
				if (tokens.length != 3) {
					System.err.println("Tokenization error. Skip line: " + line);
					continue;
				}
				
				String category = tokens[0];
				String id = tokens[1];
				String message = tokens[2];
				
				key.set("/" + category + "/" + id);
				value.set(message);
				writer.append(key, value);
				
				count++;
			}
			br.close();
			System.out.println("Total entries to write: " + count);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	private void writeVec(Configuration conf, FileSystem fs, List<NamedVector>vecList, String vecFilePath) throws IOException {		
		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, new Path(vecFilePath), Text.class, VectorWritable.class);
		//VectorWriter vw = new SequenceFileVectorWriter(writer);
		//VectorWritable vec = new VectorWritable();
		VectorWritable writable = new VectorWritable();
		for (NamedVector v: vecList) {
			writable.set(v.getDelegate());
			writer.append(new Text(v.getName()), writable);
		}
		
		writer.close();
	}
	
	private void trainBayes(Configuration conf, FileSystem fs, String vecFilePath, String trainDir, String modelDir, String labelIndexFile) throws IOException {
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(vecFilePath), conf);
		
		Text key = new Text();
		VectorWritable vw = new VectorWritable();
		
		Map<String, List<VectorWritable>> vecMap = new HashMap<String, List<VectorWritable>>();

		while(reader.next(key, vw)) {
			System.out.println("Key: " + key + " Value: " + vw.get().asFormatString());
			
			if (vecMap.keySet().contains(key.toString())) {
				vecMap.get(key.toString()).add(vw);
			} else {
				List<VectorWritable> vecList = new ArrayList<VectorWritable>();
				vecList.add(vw);
				vecMap.put(key.toString(), vecList);
			}
		}
		
		File tmpDir;
		//BufferedWriter bw;
		SequenceFile.Writer writer;
		for (String keyString: vecMap.keySet()) {
			tmpDir = new File(trainDir + "/" + keyString);
			tmpDir.mkdir();
			
			File tmpFile = new File(tmpDir.getAbsolutePath() + "/data");
			writer = new SequenceFile.Writer(fs, conf, new Path(tmpFile.getAbsolutePath()), LongWritable.class, VectorWritable.class);
			int recNum = 0;
			for (VectorWritable tmpVW: vecMap.get(keyString)) {
				//bw = new BufferedWriter(new FileWriter(tmpFile));
				//bw.append(tmpVW.get().asFormatString() + "\n");
				//bw.close();
				writer.append(new LongWritable(recNum++), tmpVW);
			}
			writer.close();
		}
		
		//String modelDir = baseDir + "/" + "model";
		//String labelIndexFile = baseDir + "/" + "labelIndex/indexfile";
		String[] arg = new String[] {"-i", trainDir, "-o", modelDir, "-li", labelIndexFile, "-ow"};
		
		try {
			ToolRunner.run(conf, new TrainNaiveBayesJob(), arg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeDataByToolRunner(Configuration conf, FileSystem fs, String baseDir) throws IOException {
		String[] arg = null;
		
		String input = baseDir + "/" + "input" + "/points";
		String seqDir = baseDir + "/" + "seq";
		arg = new String[] {"-i", input, "-o", seqDir};

		try {
			ToolRunner.run(conf, new SequenceFilesFromDirectory(), arg);
			//SequenceFilesFromDirectory sffd = new SequenceFilesFromDirectory();
			//sffd.setConf(conf);
			//sffd.run(arg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("SeqFromDir completes");
		
		String vecDir = baseDir + "/" + "vec";
		arg = new String[] {"-i", seqDir, "-o", vecDir, "-lnorm", "-nv", "-wt", "tfidf"};
		//arg = new String[] {"-i", seqDir, "-o", vecDir, "-wt", "tfidf", "-seq"};
		
		try {
			ToolRunner.run(conf, new SparseVectorsFromSequenceFiles(), arg);
			//SparseVectorsFromSequenceFiles svfsf = new SparseVectorsFromSequenceFiles();
			//svfsf.setConf(conf);
			//svfsf.run(arg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Seq2Vector completes");
				
		/*SequenceFile.Reader reader = 
				new SequenceFile.Reader(fs, new Path(vecDir + "/tfidf-vectors/" + "part-r-00000"), conf);
		Text key = new Text();
		VectorWritable value = new VectorWritable();
		while(reader.next(key, value)) {
			Vector vec = value.get();
			System.out.println("Check vec: " + vec);
		}*/
		
		String trainVecDir = baseDir + "/" + "train";
		String testVecDir = baseDir + "/" + "test";
		String tfidfVecDir = vecDir + "/" + "tfidf-vectors";
		arg = new String[]{"-i", tfidfVecDir, "--trainingOutput", 
				trainVecDir, "--testOutput", testVecDir,
				"--randomSelectionPct", "20", "--overwrite", "--sequenceFiles", "-xm", "sequential"};
		
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
		String labelIndexFile = baseDir + "/" + "labelIndex/indexfile";
		//String labelIndexFile = baseDir + "/" + "labelIndex";
		String[] arg = new String[] {"-i", trainVecDir, "-o", modelDir, "-li", labelIndexFile, "-el", "-ow"};
		
		try {
			ToolRunner.run(conf, new TrainNaiveBayesJob(), arg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	private void testModelByToolRunner(Configuration conf, FileSystem fs, String baseDir) {
		String testVecDir = baseDir + "/test";
		String modelDir = baseDir + "/model";
		String labelIndexFile = baseDir + "/labelIndex/indexfile";
		String testResultDir = baseDir + "/testresult";
		
		String[] arg = new String[] {"-i", testVecDir, "-m", modelDir, "-l", labelIndexFile, "-ow", "-o", testResultDir};
		
		try {
			ToolRunner.run(conf, new TestNaiveBayesDriver(), arg); 
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
		String dataSource = "/input/tweets.tsv";
		String outputDir = "/seq";
		
		try {
			conf = new Configuration();
			fs = FileSystem.get(conf);
			
			SimpleBayesExample ex = new SimpleBayesExample();
			List<NamedVector> vecList = ex.readCSV("bayes/input/points.csv");
			ex.writeVec(conf, fs, vecList, "bayes/vec/file1");
			ex.trainBayes(conf, fs, "bayes/vec/file1", "bayes/train", "bayes/model", "bayes/labelIndex/indexfile");
			//ex.writeSeqData(conf, fs, baseDir + dataSource, baseDir + outputDir);
			//ex.writeDataByToolRunner(conf, fs, baseDir);
			//ex.trainModelByToolRunner(conf, fs, baseDir);
			//ex.testModelByToolRunner(conf, fs, baseDir);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
