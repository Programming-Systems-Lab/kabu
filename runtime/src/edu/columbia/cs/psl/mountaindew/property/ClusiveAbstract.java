package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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
