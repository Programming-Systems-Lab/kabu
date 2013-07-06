package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.clustering.kmeans.Kluster;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.common.distance.SquaredEuclideanDistanceMeasure;
import org.apache.mahout.common.distance.WeightedDistanceMeasure;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.reflections.Reflections;

public class SimpleKMeansExample {
	
	public static final double[][] points = {{1, 1}, {2, 1}, {1, 2}, {2, 2}, {3, 3}, {8, 8}, {9, 8}, {8, 9}, {9, 9}};
	
	public static void writePointsToFile(List<Vector> points, String fileName, FileSystem fs, Configuration conf) throws IOException {
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
	
	public static List<Vector> getPoints(double[][] raw) {
		List<Vector> points = new ArrayList<Vector>();
		
		for (int i = 0; i < raw.length; i++) {
			double[] fr = raw[i];
			Vector vec = new RandomAccessSparseVector(fr.length);
			vec.assign(fr);
			points.add(vec);
		}
		
		return points;
	}
	
	public static void deleteFiles(File dir) {
		if (dir.isDirectory()) {
			File[] childs = dir.listFiles();
			for (File tmpFile: childs) {
				deleteFiles(tmpFile);
			}
		}
		dir.delete();
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
		
		List<Vector> vectors = getPoints(points);
		
		File testData = new File("testdata");
		
		if (!testData.exists()) {
			testData.mkdir();
		}
		
		testData = new File("testdata/points");
		
		if (!testData.exists()) {
			testData.mkdir();
		}
		
		File testDataOutput = new File("testdata/output");
		
		if (testDataOutput.exists()) {
			System.out.println("Start to clean output files");
			deleteFiles(testDataOutput);
		}
		
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
		
		
		/*File jarFile = new File(jarName);
		if (!jarFile.exists()) {
			System.out.println("Jar file does not exist");
			return ;
		} else {
			System.out.println("Confrim jar name: " + jarFile.getAbsolutePath());
		}
		
		packageName = packageName.replaceAll("\\.", "/");
		System.out.println("Check packageName: " + packageName);
		
		try {
			JarInputStream jarStream = new JarInputStream(new FileInputStream(jarName));
			JarEntry jarEntry;
			ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
			
			while(true) {
				jarEntry = jarStream.getNextJarEntry();
				if (jarEntry == null) {
					System.out.println("Find no jar entry");
					break;
				}
				
				if (jarEntry.getName().startsWith(packageName) && jarEntry.getName().endsWith(".class")) {
					System.out.println("Add jar entry: " + jarEntry.getName());
					classList.add(jarEntry.getClass());
				}
			}
			
			for (int i = 0; i < classList.size(); i++) {
				System.out.println("Find class: " + classList.get(i).getName());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}*/

		
		/*Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		
		writePointsToFile(vectors, "testdata/points/file1", fs, conf);
		
		Path path = new Path("testdata/clusters/part-00000");
		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, path, Text.class, Kluster.class);
		
		for (int i = 0; i < k; i++) {
			Vector vec = vectors.get(i);
			Kluster kluster = new Kluster(vec, i, new EuclideanDistanceMeasure());
			writer.append(new Text(kluster.getIdentifier()), kluster);
		}
		writer.close();
		
		KMeansDriver.run(conf, new Path("testdata/points"), new Path("testdata/clusters"), new Path("testdata/output"), new EuclideanDistanceMeasure(), 0.001, 10, true, 0, false);
		
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path("testdata/output/" + Cluster.CLUSTERED_POINTS_DIR + "/part-m-00000"), conf);
		
		IntWritable key = new IntWritable();
		WeightedVectorWritable value = new WeightedVectorWritable();
		
		while(reader.next(key, value)) {
			System.out.println(value.toString() + " belongs to cluster " + key.toString());
		}
		
		reader.close();*/
	}

}
