package edu.columbia.cs.psl.mountaindew.example;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.mahout.math.Vector;

//import weka.classifiers.functions.supportVector.PukTest;

//import edu.columbia.cs.psl.metamorphic.runtime.ConfigLoader;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
//import edu.columbia.cs.psl.mountaindew.runtime.Interceptor;

@Metamorphic
public class SimpleExample extends AbstractExample {
	
	@Metamorphic
	public void dummyMethod() {
		
	}

	@Metamorphic
	public String go(String in,String in2, String[] in3)
	{
//		in ="3";
//		Interceptor.catchParam(in, in3);
		String foobar = "x";
		int foo = 10;
		int bar=200;
		

		return in.toLowerCase();
	}
	
	@Metamorphic
	public int timesThree(int input)
	{
		return input * 3;
	}
	
	@Metamorphic
	public int addThree(int input) {
		return input + 3;
	}
	
	@Metamorphic
	public double standardDeviation(ArrayList<Integer> in)
	{
		double r = 0;
		double mean = 0;
		for(int i : in)
			mean+=i;
		mean = mean / in.size();
		for(int i : in)
			r += Math.pow(i - mean, 2);
		r = r / in.size();
		r = Math.sqrt(r);
		return r;
	}
	
	@Metamorphic
	public ArrayList<Integer> sort(int[] in)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(Integer i : in)
		{
			result.add(i);
		}
		Collections.sort(result);
		
		return result;
	}
	
	@Metamorphic
	public int[] increArrayInPlace(int[] in) {
		for (int i = 0 ; i < in.length; i++) {
			in[i]++;
		}
		return in;
	}
	
	@Metamorphic
	public int[] increArray(int[] in) {
		int[] ret = new int[in.length];
		
		for (int i = 0; i < in.length; i++) {
			ret[i] = in[i] + 1;
		}
		return ret;
	}	
	
	@Metamorphic
	public double selectMax(int[] in) {
		double max = Double.MIN_VALUE;
		
		for (int i = 0; i < in.length; i++) {
			if (in[i] > max) {
				max = in[i];
			}
		}
		
		return max;
	}
	
	@Metamorphic
	public ArrayList<Integer> increAndSort(int[] in) {
		int result[] = increArray(in);
		return sort(result);
	}
	
	@Metamorphic
	public int sum(int[] in) {
		int sum = 0;
		for (int i = 0; i < in.length; i++) {
			sum = sum + in[i];
		}
		
		return sum;
	}
	
	@Metamorphic
	public double[] arrayDiv(int[] in) {
		double result[] = new double[in.length];
		int k = 5;
		
		for (int i = 0; i < in.length; i++) {
			result[i] = ((double)in[i])/k;
		}
		
		return result;
	}
	
	@Metamorphic
	public double[] arrayDiv(List<Integer> in) {
		double result[] = new double[in.size()];
		int k = 5;
		
		for (int i = 0; i < in.size(); i++) {
			Integer tmp = in.get(i);
			result[i] = ((double)tmp)/k;
		}
		
		return result;
	}
	
	@Metamorphic
	public double[] arrayDec(int[] in) {
		double result[] = new double[in.length];
		int k = 4;
		
		for (int i = 0 ; i < in.length; i++) {
			result[i] = in[i] - k;
		}
		
		return result;
	}
	
	@Metamorphic
	public double[] setMinVal(double[] in) {
		int size = in.length;
		double threshold = 3.0;
		
		for (int i = 0; i < size; i++) {
			if (in[i] < threshold)
				in[i] = threshold;
		}
		
		return in;
	}
	
	
	@Metamorphic
	public int occurenceZero(double[] in) {
		int ret = 0;
		
		for (int i = 0 ; i < in.length; i++) {
			if (in[i] == 0) {
				ret++;
			}
		}
		
		return ret;
	}
	
	@Metamorphic
	public int occurenceNonZero(double[] in) {
		int ret = 0;
		
		for (int i = 0; i < in.length; i++) {
			if (in[i] != 0) {
				ret++;
			}
		}
		
		return ret;
	}
	
	@Metamorphic
	public <T> T arrayCopy(T in) {
		T ret = null;
		if (in.getClass().isArray()) {
			ret = (T)Array.newInstance(in.getClass().getComponentType(), Array.getLength(in));
		}
		
		return ret;
	}
	
	@Metamorphic
	public double[] arrayCopy(double[] in) {
		double[] ret = in.clone();
		return ret;
	}
	
	public int partition(double[] in, int start, int end) {
		double pivot = in[start];
		int i = start;
		double tmp;
		for (int j = start+1; j <= end ; j++) {
			if (in[j] <= pivot) {
				i++;
				tmp = in[j];
				in[j] = in[i];
				in[i] = tmp;
			}
		}
		
		tmp = in[start];
		in[start] = in[i];
		in[i] = tmp;
		return i;
	}
	
	public double[] quickSort(double[] in, int start, int end) {
		if (start < end) {
			int pPoint = partition(in, start, end);
			quickSort(in, start, pPoint-1);
			quickSort(in, pPoint+1, end);
		}
		return in;
	}
	
	@Metamorphic
	public double[] quickSortSingle(double[] in) {
		/*if (in.length > 1) {
			int pPoint = partition(in, 0, in.length-1);
			double[] left = Arrays.copyOfRange(in, 0, pPoint);
			left = quickSortSingle(left);
			double[] right = Arrays.copyOfRange(in, pPoint+1, in.length);
			right = quickSortSingle(right);
			double[] result = Arrays.copyOf(left, left.length + right.length + 1);
			result[left.length] = in[pPoint];
			System.arraycopy(right, 0, result, left.length+1, right.length);
			
			return result;
		}*/
		quickSort(in, 0, in.length - 1);
		return in;
	}
	
	@Metamorphic
	public double[] quickSortSingleMutant(double[] in) {
		quickSort(in, 0, in.length - 1);
		Random r = new Random();
		int mutantPoint = r.nextInt(in.length);
		
		in[mutantPoint] = r.nextInt(100);
		
		return in;
	}
	
	@Metamorphic
	public double[] sort(double[] in) {
		Arrays.sort(in);
		return in;
	}
	
	@Metamorphic
	public int[] sortInt(int[] in) {
		Arrays.sort(in);
		return in;
	}
	
	@Metamorphic
	public double getLength(int[] in) {
		return Double.valueOf(String.valueOf(in.length));
	}
	
	@Metamorphic
	public double foo(int[] in) {
		double ret = 0;
		for (int i = 0; i < in.length; i++) {
			ret = ret + Double.valueOf(in[i]);
		}
		return ret;
	}
	
	public static void main(String[] args) {
		SimpleExample s = new SimpleExample();
//		s.selectMax(new int[]{3, 4, 5});
//		String[] barzzz = {"aa","bb"};
//		System.out.println("Max memory: " + Runtime.getRuntime().maxMemory());
//		Interceptor.catchParam(args, args);
//		SimpleExample ex = new SimpleExample();
//		System.out.println(ex.addThree(0));
//		System.out.println(ex.addThree(1));
//		System.out.println(ex.addThree(2,null));
		
//		System.out.println(ex.timesThree(2));
//		System.out.println(ex.timesThree(3));
//		System.out.println(ex.timesThree(4));
		
//		System.out.println(ex.sort(new int[] {3,4,5}));
//		System.out.println(ex.increArray(new int[] {7, 8, 9}));
//		System.out.println(ex.sum(new int[] {7, 8, 9}));
//		System.out.println(ex.increArrayInPlace(new int[] {7, 8, 9}));
//		System.out.println(ex.increAndSort(new int[] {1, 2, 3}));
//		System.out.println(ex.selectMax(new int[]{10, 1, 100}));
//		System.out.println(ex.sort(new int[] {4,3,5}));
//		double[] quickResult = ex.quickSort(new double[] {4, 3, 5,}, 0, 2);
//		double[] quickResult = ex.quickSortSingle(new double[] {5, 3, 4});
/*		double[] quickResult = ex.quickSortSingleMutant(new double[] {4, 3, 5});
		for (int i = 0; i< quickResult.length; i++) {
			System.out.println(quickResult[i]);
		}*/
		
//		System.out.println(ex.arrayDiv(new int[] {8, 3, 2, 9}));
//		System.out.println(ex.setMinVal(new double[] {4, 9, 10, 7, 2, 13, 1, -1}));
//		System.out.println(ex.arrayDec(new int[] {8, 4, 9, 7}));
//		System.out.println(ex.occurenceZero(new double[] {2, 3, 9, 0, 1}));
//		System.out.println(ex.occurenceNonZero(new double[] {2, 3, 9, 0, 1}));
//		System.out.println(ex.arrayCopy(new double[] {5, 8, 1}));
//		PukTest test = new PukTest("foo");
//		junit.textui.TestRunner.run(PukTest.suite());
//		System.out.println(new SimpleExample().go("abc","def",barzzz));
/*		ArrayList<Double> properties = ConfigLoader.getInstance().getProperty("Additive");
		for (Double d: properties) {
			System.out.println("Check properties: " + d);
		}*/
		
		/*SimpleExample ori = new SimpleExample();
		System.out.println(ori.arrayDiv(new int[] {8, 3, 2, 9}));*/
		
		/*SimpleExampleAORB_30 m1 = new SimpleExampleAORB_30();
		System.out.println(m1.arrayDiv(new int[] {8, 3, 2, 9}));*/
		
		/*SimpleExampleAORB_31 m2 = new SimpleExampleAORB_31();
		System.out.println(m2.arrayDiv(new int[] {8, 3, 2, 9}));*/
		/*try {
			Class<?> c = Class.forName("edu.columbia.cs.psl.mountaindew.example.SimpleKMeansExample");
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(conf);
			SimpleKMeansExample ex = new SimpleKMeansExample(fs, conf, 2);
			List<Vector> points = ex.readCSV("testdata/input/points.csv");
			
			Class[] params = new Class[]{FileSystem.class, Configuration.class, int.class};
			Constructor<?> constructor = c.getConstructor(params);
			
			Class<?>[] retrieveParams = constructor.getParameterTypes();
			
			for (Class clazz: retrieveParams) {
				System.out.println("Check params: " + clazz.getName());
			}
			
			Class<?> c2 = Class.forName("edu.columbia.cs.psl.mountaindew.example.SimpleExample");
			
			Object obj = constructor.newInstance(fs, conf, 2);
			Method m = c.getMethod("driveKMeans", List.class);
			List<Vector> centroids = (ArrayList<Vector>)m.invoke(obj, points);
			
			for (Vector centroid: centroids) {
				System.out.println("Centroids by reflection: " + centroid);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}*/
		SimpleExample ex = new SimpleExample();
		/*List<Integer> input = new ArrayList<Integer>();
		input.add(3);
		input.add(4);
		input.add(5);
		System.out.println(ex.arrayDiv(input));*/
		
		/*int[] out = ex.sortInt(new int[]{3, 1, 2});
		for (int i = 0; i < out.length; i++) {
			System.out.println(out[i]);
		}*/
		
		double arrayLength = ex.getLength(new int[]{4,7,8});
		
		double sum = ex.foo(new int[]{0, 0, 0});
		System.out.println("Check sum: " + sum);
		
	}
}
