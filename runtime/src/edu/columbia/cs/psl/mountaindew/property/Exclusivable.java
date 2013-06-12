package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Exclusive;

public class Exclusivable extends ClusiveAbstract{

	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		// TODO Auto-generated method stub
		double diffElement, rt1Sum, rt2Sum;
		if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {
			diffElement = findDiff(returnValue1, returnValue2);
			rt1Sum = calSum(returnValue1);
			rt2Sum = calSum(returnValue2);
			
			if (diffElement == rt1Sum - rt2Sum)
				return true;
		}
		
		if (Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass())) {
			diffElement = findDiff(returnValue1, returnValue2);
			rt1Sum = calSum(returnValue1);
			rt2Sum = calSum(returnValue2);
			
			if (diffElement == rt1Sum - rt2Sum)
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
		
		//If parameter is array or collection, check length if i2 = i1-1, find out diff, check sum
		double diffElement;
		double o1Sum;
		double o2Sum;
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
		
		diffElement = findDiff(o1, o2);
		o1Sum = calSum(o1);
		o2Sum = calSum(o2);
		
		System.out.println("DEBUG Exclusive diffElement: " + diffElement);
		System.out.println("DEBUG o1Sum: " + o1Sum);
		System.out.println("DEBUG o2Sum: " + o2Sum);
		
		if (o1Sum - o2Sum == diffElement) {
			System.out.println("It's true in exclusive");
			return true;
		}

		return false;
	}
	
	private double findDiff(Object result1, Object result2) {
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
		//Because rList2 is 1 unit shorter than rList1
		for (int i = 0; i < rList2.size(); i++) {
			if (rList1.get(i) != rList2.get(i)) {
				diffElement = ((Number)rList1.get(i)).doubleValue();
				return diffElement;
			}
		}
		
		//If everything is the same, the diff elemnt is the last one of rList1
		diffElement = ((Number)rList1.get(rList1.size() - 1)).doubleValue();
		return diffElement;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Exclusivable";
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return new Exclusive();
	}

	

}
