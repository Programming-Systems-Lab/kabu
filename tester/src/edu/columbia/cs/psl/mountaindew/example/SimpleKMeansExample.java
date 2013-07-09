package edu.columbia.cs.psl.mountaindew.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.serializer.Serialization;
import org.apache.hadoop.io.serializer.WritableSerialization;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.apache.mahout.clustering.iterator.ClusterWritable;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.clustering.kmeans.Kluster;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.reflections.Reflections;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

@Metamorphic
public class SimpleKMeansExample {
	
	public final double[][] points = {{1, 1}, {2, 1}, {1, 2}, {2, 2}, {3, 3}, {8, 8}, {9, 8}, {8, 9}, {9, 9}};
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSS");
	
	private String oriString = "ori";
	
	private String transString = "trans";
	
	private String testdataRoot = "testdata";
	
	private FileSystem fs;
	
	private Configuration conf;
	
	private int kNum;
	
	public SimpleKMeansExample(FileSystem fs, Configuration conf, int kNum) {
		this.fs = fs;
		this.conf = conf;
		this.kNum = kNum;
	} 
		
	public String getTimemark() {
		Date date = new Date();
		return formatter.format(date);
	}
	
	public static String toPath(String className) {
		StringBuffer sb = new StringBuffer(className);
		
		for (int i = 0;i < sb.length(); i++) {
			if (sb.charAt(i) == '.') {
				sb.setCharAt(i, '/');
			}
		}
		
		sb.append(".class");
		return sb.toString();
	}
	
	public List<Vector> readCSV(String inputPath) {
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
			List<Vector> vectors = new ArrayList<Vector>();
			String tmpString = null;
			while ((tmpString = br.readLine()) != null) {
				String[] sp = tmpString.split(",");
				double[] doubleVal = new double[sp.length];
				
				for (int i = 0 ; i < sp.length; i++) {
					doubleVal[i] = Double.valueOf(sp[i]);
				}
				
				/*try {
					Class.forName("org.apache.mahout.math.Vector");
					System.out.println("Got the Vector class");
				} catch (ClassNotFoundException ex) {
					System.out.println("Find no Vector class");
				}
				
				String targetPath = toPath("org.apache.mahout.math.Vector");
				System.out.println("Target path: " + targetPath);
				URL u = ClassLoader.getSystemResource(targetPath);
				System.out.println("URL: " + u);
				
				targetPath = toPath("org.apache.mahout.math.AbstractVector");
				System.out.println("Target path: " + targetPath);
				u = ClassLoader.getSystemResource(targetPath);
				System.out.println("URL: " + u);
				
				targetPath = toPath("org.apache.mahout.math.RandomAccessSparseVector");
				System.out.println("Target path: " + targetPath);
				u = ClassLoader.getSystemResource(targetPath);
				System.out.println("URL: " + u);
				
				String classPath = System.getProperty("java.class.path");
				System.out.println("Class path: " + classPath);*/
				
				Vector vec = new RandomAccessSparseVector(doubleVal.length);
				vec.assign(doubleVal);
				
				vectors.add(vec);
			}
			return vectors;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public void writePointsToFile(List<Vector> points, String fileName) throws IOException {
		Path path = new Path(fileName);
		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, path, LongWritable.class, VectorWritable.class);
		long recNum = 0;
		VectorWritable vec = new VectorWritable();
				
		for (Vector point: points) {
			vec.set(point);
			writer.append(new LongWritable(recNum++), vec);
		}
		
		writer.close();
	}
	
	public List<Vector> getPoints(double[][] raw) {
		List<Vector> points = new ArrayList<Vector>();
		
		for (int i = 0; i < raw.length; i++) {
			double[] fr = raw[i];
			Vector vec = new RandomAccessSparseVector(fr.length);
			vec.assign(fr);
			points.add(vec);
		}
		
		return points;
	}
	
	public void deleteFiles(File dir) {
		if (dir.isDirectory()) {
			File[] childs = dir.listFiles();
			for (File tmpFile: childs) {
				deleteFiles(tmpFile);
			}
		}
		dir.delete();
	}
	
	public Set<Class<? extends org.apache.mahout.common.distance.DistanceMeasure>> getAllMeasureClass() {
		String pacakgeName = "org.apache.mahout.common.distance";
		String jarName = "lib/mahout-core-0.7.jar";
		
		Reflections reflections = new Reflections(pacakgeName);
		Set<Class<? extends org.apache.mahout.common.distance.DistanceMeasure>> measureClass = 
				reflections.getSubTypesOf(org.apache.mahout.common.distance.DistanceMeasure.class);
		return measureClass;
	}
	
	public void getAllMeasures() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String packageName = "org.apache.mahout.common.distance";
		String jarName = "lib/mahout-core-0.7.jar";
		
		Reflections reflections = new Reflections(packageName);
		Set<Class<? extends org.apache.mahout.common.distance.DistanceMeasure>> measureTypes = reflections.getSubTypesOf(org.apache.mahout.common.distance.DistanceMeasure.class);
		System.out.println("Measure set size: " + measureTypes.size());
		Vector v1 = new RandomAccessSparseVector(2);
		Vector v2 = new RandomAccessSparseVector(2);
		v1.assign(new double[]{1, 1});
		v2.assign(new double[]{2, 2});
		
		for (Class<? extends org.apache.mahout.common.distance.DistanceMeasure> tmpClass: measureTypes) {
			System.out.println(tmpClass.getName());
			
			if (Modifier.isAbstract(tmpClass.getModifiers()))
				continue;
			
			Method[] methodList = tmpClass.getDeclaredMethods();
			Object tmpObj = tmpClass.newInstance();
			
			Outer: for (Method tmpMethod: methodList) {
				if (tmpMethod.getName().equals("distance")) {
					System.out.println("In the distance method");
					Type[] inputType = tmpMethod.getGenericParameterTypes();
					
					if (inputType.length == 2) {
						
						for (Type type: inputType) {
							if (!type.toString().equals("Vector"))
								continue Outer;
						}
						Object ret = tmpMethod.invoke(tmpObj, v1, v2);
						
						double retVal = Double.MIN_VALUE;
						
						if (ret.getClass().isAssignableFrom(Number.class)) {
							retVal = (Double)ret;
						}
						System.out.println("Measure result: " + retVal);
					}
				}
			}
			
		}
	}
	
	public void retrieveAllMeasures() {
		/*for (Class<? extends DistanceMeasure> tmpClass: getAllMeasureClass()) {
		
		if (Modifier.isAbstract(tmpClass.getModifiers()))
			continue;
		String className = tmpClass.getName();
		className = className.substring(className.lastIndexOf(".") + 1, className.length());
		
		System.out.println("Use " + className + " for clustering");
		File classDir = new File("testdata/clusters/" + className);
		File classOutputDir = new File("testdata/output/" + className);
		
		if (classDir.exists()) {
			deleteFiles(classDir);
		}
		classDir.mkdir();
		
		if (classOutputDir.exists()) {
			deleteFiles(classOutputDir);
		}
		classOutputDir.mkdir();
		
		Path path = new Path(classDir.getCanonicalPath() + "/" + "part-00000");
		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, path, Text.class, Kluster.class);
		
		for (int i = 0; i < k; i++) {
			Vector vec = vectors.get(i);
			Kluster kluster = new Kluster(vec, i, tmpClass.newInstance());
			writer.append(new Text(kluster.getIdentifier()), kluster);
		}
		writer.close();
		
		KMeansDriver.run(conf, new Path("testdata/points"), new Path(classDir.getCanonicalPath()), 
				new Path(classOutputDir.getCanonicalPath()), tmpClass.newInstance(), 0.001, 10, true, 0, false);
		
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(classOutputDir.getCanonicalPath() + "/" + Cluster.CLUSTERED_POINTS_DIR + "/part-m-00000"), conf);
		
		IntWritable key = new IntWritable();
		WeightedVectorWritable value = new WeightedVectorWritable();
		
		while(reader.next(key, value)) {
			System.out.println(value.toString() + " belongs to cluster " + key.toString());
		}
		
		reader.close();
	}*/
	}
	
	@Metamorphic
	public ArrayList<Vector> driveKMeans(List<Vector> vectors) throws IOException, InterruptedException, ClassNotFoundException {		
		String dateString = formatter.format(new Date());
		String vecDir = testdataRoot + "/points/" + dateString + "/";
		String vecFile = vecDir + "file1";
		String clusterDir = testdataRoot + "/clusters/" + dateString + "/";
		String outputDir = testdataRoot + "/output/" + dateString + "/";
		
		System.out.println("Vectors: " + vectors);
		System.out.println("VecFile: " + vecFile);
		WritableSerialization s = new WritableSerialization();
		Serialization tmps = (Serialization)s;
		
		//Write in-memory data to vector file
		this.writePointsToFile(vectors, vecFile);
		//Initiate cluster centroid
		this.writeKluster(vectors, clusterDir);
		//Do KMeans
		return this.execKMeans(vecDir, clusterDir, outputDir);
	}
	
	public void writeKluster(List<Vector> vectors, String targetDir) throws IOException {
		Path path = new Path(targetDir + "/part-00000");
		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, path, Text.class, Kluster.class);
		
		for (int i = 0; i < kNum; i++) {
			Vector vec = vectors.get(i);
			System.out.println("Print vec: " + vec.toString());
			Kluster kluster = new Kluster(vec, i, new EuclideanDistanceMeasure());
			writer.append(new Text(kluster.getIdentifier()), kluster);
		}
		writer.close();
	}
	
	public ArrayList<Vector> execKMeans(String vecDir, String clusterDir, String outputDir) throws IOException, InterruptedException, ClassNotFoundException {
		Path vecPath = new Path(vecDir);
		Path clusterPath = new Path(clusterDir);
		Path outputPath = new Path(outputDir);
		
		KMeansDriver.run(conf, vecPath, clusterPath, outputPath, new EuclideanDistanceMeasure(), 0.001, 10, true, 0, false); 
		
		SequenceFile.Reader reader = 
				new SequenceFile.Reader(fs, new Path(outputPath.toString() + "/" + Cluster.CLUSTERED_POINTS_DIR + "/part-m-00000"), conf);
		
		IntWritable key = new IntWritable();
		WeightedVectorWritable value = new WeightedVectorWritable();

		while(reader.next(key, value)) {
			System.out.println(value.toString() + " belongs to cluster " + key.toString());
		}
		reader.close();
		
		//Get centroid vectors
		File outputDirFile = new File(outputPath.toString());
		File[] outputChild = outputDirFile.listFiles();
		
		String filePattern = "clusters-[0-9]*-final";
		
		String targetPath = null;
		
		for (int i = 0 ; i < outputChild.length; i++) {
			if (outputChild[i].getName().matches(filePattern)) {
				System.out.println("Get one file: " + outputChild[i].getName());
				targetPath = outputChild[i].getAbsolutePath();
				break;
			}
		}
		
		reader = new SequenceFile.Reader(fs, new Path(targetPath + "/" + "part-r-00000"), conf);
		IntWritable cKey = new IntWritable();
		ClusterWritable cw = new ClusterWritable();
		ArrayList<Vector> centroidVectors = new ArrayList<Vector>();
		
		while(reader.next(key, cw)) {
			System.out.println(cKey.toString() + ": " + cw.getValue().getCenter());
			centroidVectors.add(cw.getValue().getCenter());
		}
		
		return centroidVectors;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws InterruptedException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		int k = 2;
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		SimpleKMeansExample ex = new SimpleKMeansExample(fs, conf, k);

		File testData = new File("testdata");
		
		if (!testData.exists()) {
			testData.mkdir();
		}
		
		testData = new File("testdata/points");
		
		if (!testData.exists()) {
			testData.mkdir();
		}
		
		File testDataInput = new File("testdata/input");
		
		if (!testDataInput.exists()) {
			//System.out.println("Start to clean input files");
			//deleteFiles(testDataInput);
			testDataInput.mkdir();
		}
		
		File testDataOutput = new File("testdata/output");
		
		if (testDataOutput.exists()) {
			System.out.println("Start to clean output files");
			ex.deleteFiles(testDataOutput);
		}
		testDataOutput.mkdir();
		
		//List<Vector> vectors = getPoints(points);
		List<Vector> vectors = ex.readCSV("testdata/input/points.csv");
		
		if (vectors == null)
			return ;
		

		
		/*String vectorFile = "testdata/points/" + oriString + "/file1";
		writePointsToFile(vectors, vectorFile, fs, conf);*/
		

		ArrayList<Vector> centroids = ex.driveKMeans(vectors);
		
		for(Vector centroid: centroids) {
			System.out.println("Centroid: " + centroid);
		}
				

		/*String targetDir = "testdata/clusters/" + oriString;
		ex.writeKluster(vectors, targetDir);
		
		ex.execKMeans(new Path("testdata/points/" + oriString), 
				new Path("testdata/clusters/" + oriString), 
				new Path("testdata/output/" + oriString));
		
		
		File outputDir = new File("testdata/output/" + oriString);
		File[] outputChild = outputDir.listFiles();
		
		String filePattern = "clusters-[0-9]*-final";
		
		String targetPath = null;
		
		for (int i = 0 ; i < outputChild.length; i++) {
			if (outputChild[i].getName().matches(filePattern)) {
				System.out.println("Get one file: " + outputChild[i].getName());
				targetPath = outputChild[i].getAbsolutePath();
				break;
			}
		}
		
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(targetPath + "/" + "part-r-00000"), conf);
		IntWritable key = new IntWritable();
		ClusterWritable cw = new ClusterWritable();
		
		while(reader.next(key, cw)) {
			System.out.println(key.toString() + ": " + cw.getValue().getCenter());
			vectors.add(cw.getValue().getCenter());
		}
		
		writePointsToFile(vectors, "testdata/points/" + transString + "/file1" , fs, conf);
		
		targetDir = "testdata/clusters/" + transString;
		ex.writeKluster(vectors, targetDir);
		
		ex.execKMeans(new Path("testdata/points/" + transString), 
				new Path("testdata/clusters/" + transString), 
				new Path("testdata/output/" + transString));*/
		
	}

}
