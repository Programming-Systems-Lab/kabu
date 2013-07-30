package edu.columbia.cs.psl.mountaindew.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.classifier.AbstractVectorClassifier;
import org.apache.mahout.classifier.ClassifierResult;
import org.apache.mahout.classifier.ConfusionMatrix;
import org.apache.mahout.classifier.ResultAnalyzer;
import org.apache.mahout.classifier.naivebayes.AbstractNaiveBayesClassifier;
import org.apache.mahout.classifier.naivebayes.BayesUtils;
import org.apache.mahout.classifier.naivebayes.NaiveBayesModel;
import org.apache.mahout.classifier.naivebayes.StandardNaiveBayesClassifier;
import org.apache.mahout.classifier.naivebayes.test.TestNaiveBayesDriver;
import org.apache.mahout.classifier.naivebayes.training.TrainNaiveBayesJob;
import org.apache.mahout.classifier.sgd.ModelSerializer;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.utils.SplitInput;
import org.apache.mahout.utils.vectors.io.SequenceFileVectorWriter;
import org.apache.mahout.utils.vectors.io.VectorWriter;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;


public class MahoutBayesExample {
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSS");
	
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
				
				/*DenseVector vec = new DenseVector(doubleVal.length);
				
				for (int i = 0; i < vec.size(); i++) {
					vec.set(i, doubleVal[i]);
				}*/
				
				DenseVector vec = new DenseVector(doubleVal);
				
				
				System.out.println("Check vec: " + vec);
				
				NamedVector nVec = new NamedVector(vec, sp[sp.length - 1]);
				
				System.out.println("Check namedVec: " + nVec);
				
				vectors.add(nVec);
			}
			return vectors;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public Matrix readMatrix(String inputPath) {
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
				DenseVector vec = new DenseVector(doubleVal.length);
				//vec.assign(doubleVal);
				
				for (int i = 0; i < vec.size(); i++) {
					vec.set(i, doubleVal[i]);
				}
				
				System.out.println("Check vec: " + vec);
				
				NamedVector nVec = new NamedVector(vec, sp[sp.length - 1]);
				
				System.out.println("Check namedVec: " + nVec);
				
				vectors.add(nVec);
			}
			
			NamedVector tmpVec;
			Matrix dataMatrix = new DenseMatrix(vectors.size(), vectors.get(0).getDelegate().size());
			for (int i = 0; i < dataMatrix.rowSize(); i++) {
				tmpVec = vectors.get(i);
				//dataMatrix.set(tmpVec.getName(), this.convertVectorToArray(tmpVec.getDelegate()));
				
			}
			
			Map<String, Integer> labelBindings = dataMatrix.getRowLabelBindings();
			for (String key: labelBindings.keySet()) {
				System.out.println("Label: " + key + "Idx: " + labelBindings.get(key));
			}
			
			return dataMatrix;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private double[] convertVectorToArray(Vector vec) {
		double[] ret = new double[vec.size()];
		
		for (int i = 0; i < vec.size(); i++) {
			ret[i] = vec.get(i);
		}
		
		return ret;
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
		
		Map<String, List<Vector>> classMap = new HashMap<String, List<Vector>>();
		for (NamedVector v: vecList) {
			if (classMap.containsKey(v.getName())) {
				classMap.get(v.getName()).add(v.getDelegate());
			} else {
				List<Vector> vectorList = new ArrayList<Vector>();
				vectorList.add(v.getDelegate());
				classMap.put(v.getName(), vectorList);
			}
		}
		
		Text keyText;
		for (String key: classMap.keySet()) {
			keyText = new Text("/" + key + "/");
			
			for (Vector v: classMap.get(key)) {
				writer.append(keyText, new VectorWritable(v));
			}
		}
		
		writer.close();
		
		/*VectorWritable writable;
		for (NamedVector v: vecList) {
			writer.append(new Text("/" + v.getName() + "/"), new VectorWritable(v.getDelegate()));
		}
		
		writer.close();*/
	}
	
	/*private void writeMatrix(Configuration conf, FileSystem fs, Matrix dataMatrix, String vecFilePath) throws IOException {		
		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, new Path(vecFilePath), Text.class, VectorWritable.class);
		
		Map<String, List<Vector>> classMap = new HashMap<String, List<Vector>>();
		
		Map<String, Integer> labelBindings = dataMatrix.getRowLabelBindings();
		
		Text keyText;
		for (String label: labelBindings.keySet()) {
			keyText = new Text("/" + label +"/");

		}
		
		for (NamedVector v: vecList) {
			if (classMap.containsKey(v.getName())) {
				classMap.get(v.getName()).add(v.getDelegate());
			} else {
				List<Vector> vectorList = new ArrayList<Vector>();
				vectorList.add(v.getDelegate());
				classMap.put(v.getName(), vectorList);
			}
		}
		
		Text keyText;
		for (String key: classMap.keySet()) {
			keyText = new Text("/" + key + "/");
			
			for (Vector v: classMap.get(key)) {
				writer.append(keyText, new VectorWritable(v));
			}
		}
		
		writer.close();
	}*/
	
	private void trainBayes(Configuration conf, FileSystem fs, String vecFilePath, String tmpDirString, String modelDir) throws IOException {
		/*SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(vecFilePath), conf);
		
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
		SequenceFile.Writer writer;
		for (String keyString: vecMap.keySet()) {
			tmpDir = new File(trainDir + "/" + keyString);
			tmpDir.mkdir();
			
			File tmpFile = new File(tmpDir.getAbsolutePath() + "/data");
			writer = new SequenceFile.Writer(fs, conf, new Path(tmpFile.getAbsolutePath()), LongWritable.class, VectorWritable.class);
			int recNum = 0;
			for (VectorWritable tmpVW: vecMap.get(keyString)) {
				writer.append(new LongWritable(recNum++), tmpVW);
			}
			writer.close();
		}*/
		
		//String modelDir = baseDir + "/" + "model";
		//String labelIndexFile = baseDir + "/" + "labelIndex/indexfile";
		//String[] arg = new String[] {"-i", trainDir, "-o", modelDir, "-li", labelIndexFile, "-ow"};
		
		File tmpDir = new File(tmpDirString);
		
		if (tmpDir.exists()) {
			FileUtil.fullyDelete(tmpDir);
		}
		
		String[] arg = new String[] {"--input", vecFilePath, "--output", modelDir, "-el", "--tempDir", tmpDirString};
		
		try {
			//ToolRunner.run(conf, new TrainNaiveBayesJob(), arg);
			TrainNaiveBayesJob trainNaiveBayes = new TrainNaiveBayesJob();
			trainNaiveBayes.setConf(conf);
			trainNaiveBayes.run(arg);
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
	
	private void predictByModel(Configuration conf, String modelPath, DenseVector toPredict) {
		VectorWritable vw = new VectorWritable(toPredict);
		NaiveBayesModel nModel;
		
		try {
			nModel = NaiveBayesModel.materialize(new Path(modelPath), conf);
			
			AbstractVectorClassifier classifier = new StandardNaiveBayesClassifier(nModel);
			
			Vector predictionResult = classifier.classifyFull(vw.get());
			System.out.println("Prediction result: " + predictionResult);
			System.out.println("Category: " + predictionResult.maxValueIndex());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private Matrix predictByModel(Configuration conf, String modelPath, Matrix dataMatrix) {
		NaiveBayesModel nModel;
		List<Vector> resultList = new ArrayList<Vector>();
		
		for (int i = 0; i < dataMatrix.rowSize(); i++) {
			System.out.println("Check matrix" + dataMatrix.viewRow(i));
		}
		
		try {
			nModel = NaiveBayesModel.materialize(new Path(modelPath), conf);
			AbstractVectorClassifier classifier = new StandardNaiveBayesClassifier(nModel);
			
			//Matrix predictionResult = classifier.classifyFull(dataMatrix);
			//System.out.println("Category num: " + classifier.numCategories());
			Vector tmpVector;
			for (int i = 0; i < dataMatrix.rowSize(); i++) {
				tmpVector = classifier.classifyFull(dataMatrix.viewRow(i));
				System.out.println("Prediction result: " + tmpVector);
				System.out.println("Category: " + tmpVector.maxValueIndex());
				resultList.add(tmpVector);
			}
			
			return this.createPureMatrix(resultList);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
		
	}
	
	private void testModelByToolRunner(Configuration conf, FileSystem fs, String baseDir) {
		String testVecDir = baseDir + "/vec";
		String modelDir = baseDir + "/model";
		String labelIndexFile = baseDir + "/tmp/labelIndex";
		String testResultDir = baseDir + "/testresult";
		
		String[] arg = new String[] {"-i", testVecDir, "-m", modelDir, "-l", labelIndexFile, "-ow", "-o", testResultDir};
		
		try {
			ToolRunner.run(conf, new TestNaiveBayesDriver(), arg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private ConfusionMatrix getModelConfusionMatrix(Configuration conf, FileSystem fs, String baseDir) {
		//String testResult = baseDir + "/testresult/part-m-00000";
		String labelIndexFile = baseDir + "/tmp/labelIndex";
		String testResultDir = baseDir + "/testresult";
		try {
			Map<Integer, String> labelMap = BayesUtils.readLabelIndex(conf, new Path(labelIndexFile));
			SequenceFileDirIterable<Text, VectorWritable> dirIterable = new SequenceFileDirIterable<Text,VectorWritable>(new Path(testResultDir), PathType.LIST, PathFilters.partFilter(), conf);
			
			ResultAnalyzer analyzer = new ResultAnalyzer(labelMap.values(), "DEFAULT");
			
			for (Pair<Text, VectorWritable> pair: dirIterable) {
				int bestIdx = Integer.MIN_VALUE;
				double bestScore = Long.MIN_VALUE;
				
				for (Vector.Element element: pair.getSecond().get()) {
					if (element.get() > bestScore) {
						bestScore = element.get();
						bestIdx = element.index();
					}
				}
				if (bestIdx != Integer.MIN_VALUE) {
					ClassifierResult classifierResult = new ClassifierResult(labelMap.get(bestIdx), bestScore);
					analyzer.addInstance(pair.getFirst().toString(), classifierResult);
				}
			}
			
			return analyzer.getConfusionMatrix();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	private Matrix createPureMatrix(List<Vector> vecList) {
		double[][] realData = new double[vecList.size()][vecList.get(0).size()];
		
		for (int i = 0; i < realData.length; i++) {
			for (int j = 0; j < realData[0].length; j++) {
				realData[i][j] = vecList.get(i).get(j);
			}
		}
		
		Matrix dataMatrix = new DenseMatrix(realData);
		return dataMatrix;
	}
	
	private Matrix createMatrix(List<NamedVector> vecList) {
		double[][] realData = new double[vecList.size()][vecList.get(0).size()];
		
		for (int i = 0; i < realData.length; i++) {
			for (int j = 0; j < realData[0].length; j++) {
				realData[i][j] = vecList.get(i).get(j);
			}
		}
		
		Matrix dataMatrix = new DenseMatrix(realData);
		return dataMatrix;
	}
	
	@Metamorphic
	private ConfusionMatrix driveNaiveBayes(List<NamedVector> vecList) {
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(conf);
			
			Date date = new Date();
			String dirName = this.formatter.format(date);
			
			String baseDir = "bayes/" + dirName;
			
			this.writeVec(conf, fs, vecList, baseDir + "/vec/file1");
			this.trainBayes(conf, fs, baseDir + "/vec/file1", baseDir + "/tmp", baseDir + "/model");
			this.testModelByToolRunner(conf, fs, baseDir);
			ConfusionMatrix resultMatrix = this.getModelConfusionMatrix(conf, fs, baseDir);
			
			/*this.writeVec(conf, fs, vecList, "bayes/vec/file1");
			this.trainBayes(conf, fs, "bayes/vec/file1", "bayes/tmp", "bayes/model");
			ConfusionMatrix resultMatrix = this.getModelConfusionMatrix(conf, fs, "bayes");*/
			
			return resultMatrix;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
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
			
			MahoutBayesExample ex = new MahoutBayesExample();
			List<NamedVector> vecList = ex.readCSV("bayes/input/iris.csv");
			ConfusionMatrix resultMatrix = ex.driveNaiveBayes(vecList);
			
			System.out.println(resultMatrix);
			
			/*ex.writeVec(conf, fs, vecList, "bayes/vec/file1");
			Matrix testData = ex.createMatrix(vecList);
			ex.trainBayes(conf, fs, "bayes/vec/file1", "bayes/tmp", "bayes/model");
			ConfusionMatrix resultMatrix = ex.getModelConfusionMatrix(conf, fs, "bayes");
			System.out.println(resultMatrix);*/
			//ex.testModelByToolRunner(conf, fs, "bayes/");
			
			//ex.predictByModel(conf, "bayes/model", testData);
			
			/*double[] test = new double[]{5.1,3.5,1.4,0.2};
			DenseVector dv = new DenseVector(test.length);
			dv.assign(test);
			System.out.println("Test vec: " + dv);
			ex.predictByModel(conf, "bayes/model", dv);*/
			//ex.writeSeqData(conf, fs, baseDir + dataSource, baseDir + outputDir);
			//ex.writeDataByToolRunner(conf, fs, baseDir);
			//ex.trainModelByToolRunner(conf, fs, baseDir);
			//ex.testModelByToolRunner(conf, fs, baseDir);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
