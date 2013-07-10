package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.mahout.math.Vector;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.InclusiveMid;
import edu.columbia.cs.psl.mountaindew.absprop.ClusiveAbstract;
import edu.columbia.cs.psl.mountaindew.util.VectorSorter;

public class InclusiveByMid extends ClusiveAbstract{

	/**
	 * Inclusive property needs to catch the "predictive" behavior of a method.
	 * Need to think about this. Current version is not strong enough.
	 */
	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		// TODO Auto-generated method stub
		double rt1Max, rt1Min, rt1Avg, rt2Max, rt2Min, rt2Avg;
		if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {
			if (Array.getLength(returnValue1) + 1 != Array.getLength(returnValue2))
				return false;
			
			rt1Max = this.findMax(returnValue1);
			rt1Min = this.findMin(returnValue1);
			rt1Avg = this.findAvg(returnValue1);
			
			rt2Max = this.findMax(returnValue2);
			rt2Min = this.findMin(returnValue2);
			rt2Avg = this.findAvg(returnValue2);
			
			/*System.out.println("Check ori output max: " + rt1Max);
			System.out.println("Check ori output min: " + rt1Min);
			System.out.println("Check ori output avg: " + rt1Avg);
			
			System.out.println("Check transformed output max: " + rt2Max);
			System.out.println("Check transformed output min: " + rt2Min);
			System.out.println("Check transformed output avg: " + rt2Avg);*/
			
			if (rt1Max == rt2Max && rt1Min == rt2Min && rt2Avg > rt1Avg)
				return true;
		}
		
		/*System.out.println("Returvalue1 class: " + returnValue1.getClass().getName() + " " + Collection.class.isAssignableFrom(returnValue1.getClass()));
		System.out.println("Returvalue1 class: " + returnValue1.getClass().getName() + " " + Collection.class.isAssignableFrom(returnValue2.getClass()));*/
		
		if (Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass())) {
			//if (((Collection)returnValue1).size() + 1 != ((Collection)returnValue2).size())
				//return false;
			
			//Object tmp1 = ((Collection)returnValue1).iterator().next();
			//Object tmp2 = ((Collection)returnValue2).iterator().next();
			
			rt1Max = this.findMax(returnValue1);
			rt1Min = this.findMin(returnValue2);
			rt1Avg = this.findAvg(returnValue1);
			
			rt2Max = this.findMax(returnValue2);
			rt2Min = this.findMin(returnValue2);
			rt2Avg = this.findAvg(returnValue2);
			
			if (rt1Max  == rt2Max && rt1Min == rt2Min && rt2Avg > rt1Avg)
				return true;

		}
		
		if (Map.class.isAssignableFrom(returnValue1.getClass()) && Map.class.isAssignableFrom(returnValue2.getClass())) {
			Set<Entry> tmpSet = ((Map)returnValue1).entrySet();
			
			if (tmpSet.size() == 0)
				return false;
			
			Entry tmpEntry = tmpSet.iterator().next();
			
			if (Collection.class.isAssignableFrom(tmpEntry.getValue().getClass())) {
				Map<Object, Collection> rt1Map = (Map<Object, Collection>)returnValue1;
				Map<Object, Collection> rt2Map = (Map<Object, Collection>)returnValue2;
				
				if (rt1Map.size() != rt2Map.size())
					return false;
				
				Set rt1Set, rt2Set;
				for (Object key: rt1Map.keySet()) {
					if (this.checkCollectionEquivalence((Collection)rt1Map.get(key), (Collection)rt2Map.get(key)) == false)
						return false;
				}
				
				return true;
			}
		}
		
		if (Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass())) {
			return this.checkCollectionEquivalence((Collection)returnValue1, (Collection)returnValue2);
		}
		
		if (Number.class.isAssignableFrom(returnValue1.getClass()) && Number.class.isAssignableFrom(returnValue2.getClass())) {
			//Check if this method try to find out max, min
			double rt1 = ((Number)returnValue1).doubleValue();
			double rt2 = ((Number)returnValue2).doubleValue();
			/*if (findMax(p1) == rt1 && findMax(p2) == rt2) {
				if (rt2 - rt1 == 1)
					return true;
			}
			
			if (findMin(p1) == rt1 && findMin(p2) == rt2) {
				if (rt2 == rt1)
					return true;
			}*/
			
			//Find a way to simulate the method here
			
			//If the return value is only a number, hard to tell what it is doing. Let rt2 > rt1 first. This handles selectMin, selectMax, calAvg or so..
			if (rt2 >= rt1)
				return true;
		}

		return false;
	}
	
	private boolean checkCollectionEquivalence(Collection l1, Collection l2) {
		Set s1 = new HashSet(l1);
		Set s2 = new HashSet(l2);
		
		if (!s1.containsAll(s2))
			return false;
		if (!s2.containsAll(s1))
			return false;
		
		return true;
	}

	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2,
			int interestedVariable) {
		// TODO Auto-generated method stub
		//Get specific parameter from parameters by interestedVariable
		Object o1 = i1.params[interestedVariable];
		Object o2 = i2.params[interestedVariable];
		for (int i = 0 ; i < i2.params.length; i++) {
			if (i != interestedVariable && i1.params[i] != i2.params[i]) {
				return false;
			}
		}
		
		//If i1 is not i2's parents, no need to compare;
		if (i2.getParent() != i1) {
			return false;
		}
		
		if (!i2.getBackend().equals(this.getName()))
			return false;
		else
			return true;
		
		//If parameter is array or collection, check length if i2 = i1 +1, check sum
		/*if (o1.getClass().isArray() && o2.getClass().isArray()) {
			int o1Length = Array.getLength(o1);
			int o2Length = Array.getLength(o2);
			if (o1Length + 1 != o2Length)
				return false;
			
			double o1Max = this.findMax(o1);
			double o1Min = this.findMin(o1);
			double o1Sum = this.calSum(o1);
			
//			System.out.println("Check ori input max in propertyApplies: " + o1Max);
//			System.out.println("Check ori input min in propertyApplies: " + o1Min);
			
			double o2Max = this.findMax(o2);
			double o2Min = this.findMin(o2);
			double o2Sum = this.calSum(o2);
			
//			System.out.println("Check transformed input max in propertyApplies: " + o2Max);
//			System.out.println("Check transformed input min in propertyApplies: " + o2Min);
						
			//Because frontend add one more max in the transformed input
			if (o1Max == o2Max && o1Min == o2Min && o2Sum - o1Sum == o2Max) {
				return true;
			}
		} else if (Collection.class.isAssignableFrom(o1.getClass()) && (Collection.class.isAssignableFrom(o2.getClass()))) {
			int o1Size = ((Collection)o1).size();
			int o2Size = ((Collection)o2).size();
			
			if (o1Size + 1 != o2Size)
				return false;
			
			double o1Max = this.findMax(o1);
			double o1Min = this.findMin(o1);
			double o1Sum = this.calSum(o1);
			
			double o2Max = this.findMax(o2);
			double o2Min = this.findMin(o2);
			double o2Sum = this.calSum(o2);
			
			//Because frontend add one more max in the transformed input
			if (o1Max == o2Max && o1Min == o2Min && o2Sum - o1Sum == o2Max)
				return true;
		}
		
		return false;*/
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "C:InclusiveByMid";
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return new InclusiveMid();
	}

}

