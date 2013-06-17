package edu.columbia.cs.psl.mountaindew.stats;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class Correlationer {
	
	public static PearsonsCorrelation pCorrelation = new PearsonsCorrelation();
	
	public static double UNDEFINED = -2;
	
	//A better way to control calCorrelation?
	public static double[] calCorrelation(Object[] input, Object output) {
		double[] inputArray, outputArray;
		double[] retArray = new double[input.length];
		
		outputArray = returnDoubleArray(output);
		
		for (int i = 0; i < input.length; i++) {
			inputArray = returnDoubleArray(input[i]);
			
			if (inputArray.length != outputArray.length) {
				retArray[i] = UNDEFINED;
				continue;
			}
			
			if (inputArray != null && outputArray != null)
				retArray[i] = pCorrelation.correlation(inputArray, outputArray);
			else
				retArray[i] = UNDEFINED;
		}
		
		return retArray;
	}
	
	public static double[] calCorrelation(Object[] input, Object[] output) {
		
		double[] inputArray, outputArray;
		double[] retArray = new double[input.length];
		for (int i = 0; i < input.length; i++) {
			inputArray = returnDoubleArray(input[i]);
			outputArray = returnDoubleArray(output[i]);
			
			if (inputArray.length != outputArray.length) {
				retArray[i] = UNDEFINED;
				continue;
			}
			
			retArray[i] = pCorrelation.correlation(inputArray, outputArray);
		}
		
		return retArray;
	}
	
	public static double[] calCorrelation(Object input, Object output) {
		double[] inputArray = returnDoubleArray(input);
		double[] outputArray = returnDoubleArray(output);
		double[] retArray = new double[1];
		
		if (inputArray.length != outputArray.length) {
			retArray[0] = UNDEFINED;
			return retArray;
		}
		
		//Temporarily target on single input
		retArray[0] = pCorrelation.correlation(inputArray, outputArray);
		return retArray;
	}
	
	public static double[] returnDoubleArray(Object val) {
		double[] retArray = null;
//		System.out.println("Check val type: " + val.getClass().getName() + " " + val.getClass().isArray());
		if (val.getClass().isArray()) {
			retArray = new double[Array.getLength(val)];
			for (int i = 0; i < Array.getLength(retArray); i++) {
				retArray[i] = ((Number)Array.get(val, i)).doubleValue();
			}
		} else if (Collection.class.isAssignableFrom(val.getClass())) {
			//retArray = (double[]) Array.newInstance(val.getClass().getComponentType(), ((Collection)val).size());
			int valSize = ((Collection)val).size();
			retArray = new double[valSize];
			Iterator valIT = ((Collection)val).iterator();
			
			int count = 0;
			while(valIT.hasNext()) {
				retArray[count++] = ((Number)valIT.next()).doubleValue();
			}
		}
		
		return retArray;
	}
}
