package edu.columbia.cs.psl.mountaindew.absprop;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public abstract class ClusiveAbstract extends PairwiseMetamorphicProperty{
	protected double calSum(Object arrayList) {
		double sum = 0;
				
		if (arrayList.getClass().isArray()) {
			int arrayLength = Array.getLength(arrayList);
			
			for (int i = 0; i < arrayLength; i++) {
				sum += ((Number)Array.get(arrayList, i)).doubleValue();
			}
			return sum;
		} else if (Collection.class.isAssignableFrom(arrayList.getClass())) {
			Iterator colIT = ((Collection)arrayList).iterator();
			while(colIT.hasNext()) {
				sum += ((Number)colIT.next()).doubleValue();
			}
			
			return sum;
		}
		throw new IllegalArgumentException("Only allow array and collection to calculate sum");
	}
	
	protected double findDiff(Object result1, Object result2) {
		List<Double> rList1;
		List<Double> rList2;
		if (result1.getClass().isArray() && result2.getClass().isArray()) {
			rList1 = new ArrayList<Double>();
			rList2 = new ArrayList<Double>();
			
			for (int i = 0; i < Array.getLength(result1); i++) {
				rList1.add(((Number)Array.get(result1, i)).doubleValue());
			}
			
			for (int i = 0; i < Array.getLength(result2); i++) {
				rList2.add(((Number)Array.get(result2, i)).doubleValue());
			}
		} else if (Collection.class.isAssignableFrom(result1.getClass()) && Collection.class.isAssignableFrom(result2.getClass())) {
			rList1 = new ArrayList<Double>((Collection)result1);
			rList2 = new ArrayList<Double>((Collection)result2);
		} else {
			throw new IllegalArgumentException("Only array and collection are allowed to use findDiff");
		}
		
		Collections.sort(rList1);
		Collections.sort(rList2);
		
		double diffElement;
		double r1Val, r2Val;
		//Because rList2 is 1 unit shorter than rList1
		for (int i = 0; i < rList2.size(); i++) {
			r1Val = ((Number)rList1.get(i)).doubleValue();
			r2Val = ((Number)rList2.get(i)).doubleValue();
			if (r1Val != r2Val) {
				diffElement = ((Number)rList1.get(i)).doubleValue();
				return diffElement;
			}
		}
		
		//If everything is the same, the diff elemnt is the last one of rList1
		diffElement = ((Number)rList1.get(rList1.size() - 1)).doubleValue();
		return diffElement;
	}
	
	protected double findMax(Object arrayList) {
		double max = Double.MIN_VALUE;
		double tmp;
		
		if (arrayList.getClass().isArray()) {
			int arrayLength = Array.getLength(arrayList);
			
			for (int i = 0; i < arrayLength; i++) {
				tmp = ((Number)Array.get(arrayList, i)).doubleValue();
				
				if (tmp > max) {
					max = tmp;
				}
			}
			
			return max;
		} else if (Collection.class.isAssignableFrom(arrayList.getClass())) {
			Iterator colIT = ((Collection)arrayList).iterator();
			while(colIT.hasNext()) {
				tmp = ((Number)colIT.next()).doubleValue();
				
				if (tmp > max) {
					max = tmp;
				} 
			}
			
			return max;
		}
		throw new IllegalArgumentException("Only allow array and collection to find max");
	}
	
	protected double findMin(Object arrayList) {
		double min = Double.MAX_VALUE;
		double tmp;
		
		if (arrayList.getClass().isArray()) {
			int arrayLength = Array.getLength(arrayList);
			
			for (int i = 0; i < arrayLength; i++) {
				tmp = ((Number)Array.get(arrayList, i)).doubleValue();
				
				if (tmp < min) {
					min = tmp;
				}
			}
			
			return min;
		} else if (Collection.class.isAssignableFrom(arrayList.getClass())) {
			Iterator colIT = ((Collection)arrayList).iterator();
			while(colIT.hasNext()) {
				tmp = ((Number)colIT.next()).doubleValue();
				
				if (tmp < min) {
					min = tmp;
				} 
			}
			
			return min;
		}
		throw new IllegalArgumentException("Only allow array and collection to find max");
	}
	
	protected double findAvg(Object arrayList) {
		double sum = this.calSum(arrayList);
		
		if (arrayList.getClass().isArray()) {
			int arrayLength = Array.getLength(arrayList);
			return sum/arrayLength;
		} else if (Collection.class.isAssignableFrom(arrayList.getClass())){
			int listLength = ((Collection)arrayList).size();
			return sum/listLength;
		}
		throw new IllegalArgumentException("Only allow array and collection to find avg");
	}
	
	@Override
	protected int[] getInterestedVariableIndices() {
		ArrayList<Integer> rets = new ArrayList<Integer>();
		for(int i = 0;i<getMethod().getParameterTypes().length; i++)
		{
			if(getMethod().getParameterTypes()[i].isArray() || Collection.class.isAssignableFrom(getMethod().getParameterTypes()[i]))
				rets.add(i);
		}
		int[] ret = new int[rets.size()];
		for(int i = 0;i<rets.size();i++)
			ret[i]=rets.get(i);
		return ret;
	}
}
