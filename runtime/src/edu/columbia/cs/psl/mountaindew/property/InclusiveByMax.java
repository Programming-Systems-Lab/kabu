package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Collections;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.InclusiveMax;
import edu.columbia.cs.psl.mountaindew.absprop.ClusiveAbstract;

public class InclusiveByMax extends ClusiveAbstract{

	/**
	 * Inclusive property needs to catch the "predictive" behavior of a method.
	 * Need to think about this. Current version is not strong enough.
	 */
	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		// TODO Auto-generated method stub
		double rt1Max, rt1Sum, rt1Min, rt2Max, rt2Sum, rt2Min;
		if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {
			if (Array.getLength(returnValue1) + 1 != Array.getLength(returnValue2))
				return false;
			
			rt1Max = this.findMax(returnValue1);
			rt1Sum = this.calSum(returnValue1);
			rt1Min = this.findMin(returnValue1);
			
			rt2Max = this.findMax(returnValue2);
			rt2Sum = this.calSum(returnValue2);
			rt2Min = this.findMin(returnValue1);
			
			/*System.out.println("rt1Max: " + rt1Max);
			System.out.println("rt2Max: " + rt2Max);
			System.out.println("rt1Sum: " + rt1Sum);
			System.out.println("rt2Sum: " + rt2Sum);*/
			
			/*if (rt2Max - rt1Max == 1 && rt2Sum - rt1Sum == rt2Max)
				return true;*/
			
			if (rt2Max > rt1Max && rt2Min == rt1Min && rt2Sum > rt1Sum)
				return true;
		}
		
		/*System.out.println("Returvalue1 class: " + returnValue1.getClass().getName() + " " + Collection.class.isAssignableFrom(returnValue1.getClass()));
		System.out.println("Returvalue1 class: " + returnValue1.getClass().getName() + " " + Collection.class.isAssignableFrom(returnValue2.getClass()));*/
		
		if (Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass())) {
			if (((Collection)returnValue1).size() + 1 != ((Collection)returnValue2).size())
				return false;
			
			rt1Max = this.findMax(returnValue1);
			rt1Sum = this.calSum(returnValue1);
			rt1Min = this.findMin(returnValue1);
			
			rt2Max = this.findMax(returnValue2);
			rt2Sum = this.calSum(returnValue2);
			rt2Min = this.findMin(returnValue2);
			
			/*System.out.println("rt1Max: " + rt1Max);
			System.out.println("rt2Max: " + rt2Max);
			System.out.println("rt1Sum: " + rt1Sum);
			System.out.println("rt2Sum: " + rt2Sum);*/
			
			/*if (rt2Max - rt1Max == 1 && rt2Sum - rt1Sum == rt2Max)
				return true;*/
			
			//Make it more general first
			if (rt2Max > rt1Max && rt2Min == rt1Min && rt2Sum > rt1Sum)
				return true;
		}
		
		//This is targeting for method that aims to select max/min currently. "Predictive" is a tricky word for defining this property 
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
			
			//Make it general first. Suitable for selectMax, selectMin, calAvg, etc
			if (rt2 >= rt1)
				return true;
		}

		return false;
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
		
		//If i1 is not i2's parent, no need to compare
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
			double o1Sum = this.calSum(o1);
			
			double o2Max = this.findMax(o2);
			double o2Sum = this.calSum(o2);

			if (o1Max + 1 == o2Max && o2Sum - o1Sum == o2Max) {
				return true;
			}
		} else if (Collection.class.isAssignableFrom(o1.getClass()) && (Collection.class.isAssignableFrom(o2.getClass()))) {
			int o1Size = ((Collection)o1).size();
			int o2Size = ((Collection)o2).size();
			
			if (o1Size + 1 != o2Size)
				return false;
			
			double o1Max = this.findMax(o1);
			double o1Sum = this.calSum(o1);
			
			double o2Max = this.findMax(o2);
			double o2Sum = this.calSum(o2);
			
			if (o1Max + 1 == o2Max && o2Sum - o1Sum == o2Max)
				return true;
		}
		
		return false;*/
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "C:InclusiveByMax";
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return new InclusiveMax();
	}

}
