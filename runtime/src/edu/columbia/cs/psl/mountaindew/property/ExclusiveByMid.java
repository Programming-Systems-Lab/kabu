package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.Collection;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.ExclusiveMid;

public class ExclusiveByMid extends ClusiveAbstract{
	
	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		// TODO Auto-generated method stub
		double rt1Max, rt1Min, rt1Sum, rt2Max, rt2Min, rt2Sum;
		if ((returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) ||
				(Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass()))) {
			rt1Max = this.findMax(returnValue1);
			rt1Min = this.findMin(returnValue1);
			rt2Max = this.findMax(returnValue2);
			rt2Min = this.findMin(returnValue2);
			
			rt1Sum = this.calSum(returnValue1);
			rt2Sum = this.calSum(returnValue2);
			
//			System.out.println("ExclusiveByMid rt1Max, rt1Min, rt1Sum " + rt1Max + " " + rt1Min + " " + rt1Sum);
//			System.out.println("ExclusiveByMid rt2Max, rt2Min, rt2Sum " + rt2Max + " " + rt2Min + " " + rt2Sum);
			
			if (rt1Max == rt2Max && rt1Min == rt2Min && rt1Sum > rt1Min)
				return true;
		}
		
		if (Number.class.isAssignableFrom(returnValue1.getClass()) && Number.class.isAssignableFrom(returnValue2.getClass())) {
			double rt1 = ((Number)returnValue1).doubleValue();
			double rt2 = ((Number)returnValue2).doubleValue();

			if (rt1 >= rt2)
				return true;
		}
		return false;
	}
	
	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2,
			int interestedVariable) {
		// TODO Auto-generated method stub
		Object o1 = i1.params[interestedVariable];
		Object o2 = i2.params[interestedVariable];
		for (int i = 0 ; i < i2.params.length; i++) {
			if (i != interestedVariable && i1.params[i] != i2.params[i]) {
				return false;
			}
		}
		
		if (i2.getParent() != i1) {
			return false;
		}
		
		//If parameter is array or collection, check length if i2 = i1-1, find out dif/uf, check sum
		if (o1.getClass().isArray() && o2.getClass().isArray()) {
			int o1Length = Array.getLength(o1);
			int o2Length = Array.getLength(o2);
			if (o1Length - 1 != o2Length)
				return false;
			

		} else if (Collection.class.isAssignableFrom(o1.getClass()) && (Collection.class.isAssignableFrom(o2.getClass()))) {
			int o1Size = ((Collection)o1).size();
			int o2Size = ((Collection)o2).size();
			
			if (o1Size - 1 != o2Size)
				return false;
		}
		
		double o1Max, o1Min, o2Max, o2Min;
				
		o1Max = this.findMax(o1);
		o1Min = this.findMin(o1);
		o2Max = this.findMax(o2);
		o2Min = this.findMin(o2);
				
		if (o1Max == o2Max && o1Min == o2Min) {
			return true;
		}

		return false;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ExclusiveByMid";
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return new ExclusiveMid();
	}

}
