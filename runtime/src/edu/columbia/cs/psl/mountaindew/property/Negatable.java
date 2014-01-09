package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.mahout.math.AbstractVector;
import org.apache.mahout.math.Vector;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Negate;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Reverse;
import edu.columbia.cs.psl.mountaindew.absprop.PairwiseMetamorphicProperty;
import edu.columbia.cs.psl.mountaindew.util.VectorSorter;

public class Negatable extends MultiplicativeByConstant{
	
	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		// TODO Auto-generated method stub
		
		List rt1List = null;
		List rt2List = null;
		double neg = -1;
		try {
			if (Number.class.isAssignableFrom(returnValue1.getClass()) && Number.class.isAssignableFrom(returnValue2.getClass())) {
				double rv1 = ((Number)returnValue1).doubleValue();
				double rv2 = ((Number)returnValue2).doubleValue();
				
				System.out.println("Negatable check rv1: " + rv1);
				System.out.println("Negatable check rv2: " + rv2);
				
				//Leave this case to equality checker
				if (rv1 == rv2)
					return false;
				else
					return rv1 == rv2 * neg;
			} else if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {
				//Because propertiesApply now not check length, check them here
				if (Array.getLength(returnValue1) != Array.getLength(returnValue2))
					return false;
				
				rt1List = this.returnList(returnValue1);
				rt2List = this.returnList(returnValue2);
				
				System.out.println("Negatable check array1: " + rt1List);
				System.out.println("Negatable check array2: " + rt2List);
				
				if (rt1List.equals(rt2List))
					return false;
				
				List mrt2List = (List)this.multiplyObject(rt2List, neg);
				
				System.out.println("Negate mrt2List: " + mrt2List);
				
				return this.ce.checkEquivalence(rt1List, mrt2List);
			} else if (Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass())) {
				rt1List = this.returnList(returnValue1);
				rt2List = this.returnList(returnValue2);
				
				System.out.println("Negatable check list1: " + rt1List);
				System.out.println("Negatable check list2: " + rt2List);
				
				if (rt1List.equals(rt2List))
					return false;
				
				List mrt2List = (List)this.multiplyObject(rt2List, neg);
				
				System.out.println("Negate mrt2List: " + mrt2List);
				
				return this.ce.checkEquivalence(rt1List, mrt2List);
			} else if (Map.class.isAssignableFrom(returnValue1.getClass()) && Map.class.isAssignableFrom(returnValue2.getClass())) {
				Map map1 = (Map)returnValue1;
				Map map2 = (Map)returnValue2;
				
				if (map1.size() != map2.size())
					return false;
				
				System.out.println("Negatable check map1: " + map1);
				System.out.println("Negatable check map2: " + map2);
				
				for (Object tmp: map1.keySet()) {
					Object tmpObj1 = map1.get(tmp);
					Object tmpObj2 = map2.get(tmp);
					
					if (tmpObj1 == null || tmpObj2 == null)
						return false;
					
					if (Number.class.isAssignableFrom(tmpObj1.getClass())) {
						double n1 = ((Number)tmpObj1).doubleValue();
						double n2 = ((Number)tmpObj2).doubleValue();
						
						//For filtering out 0 = 0 * -1
						if (this.roundDouble(n1, roundDigit) == this.roundDouble(n2, roundDigit))
							return false;
						else if (this.roundDouble(n1, roundDigit) != this.roundDouble(n2 * neg, roundDigit))
							return false;
					} else if (tmpObj1.getClass().isArray() || Collections.class.isAssignableFrom(tmpObj1.getClass())) {
						List tmpList1 = this.returnList(tmpObj1);
						List tmpList2 = this.returnList(tmpObj2);
						
						List mList2 = (List)this.multiplyObject(tmpList2, neg);
						
						if (tmpList1.equals(tmpList2))
							return false;
						else if (this.ce.checkEquivalence(tmpList1, mList2) == false)
							return false;
					} else {
						return false;
					}	
				}
				return true;
			} else {
				return false;
			}
		}
		catch(IllegalArgumentException ex)
		{
			ex.printStackTrace();
			return false;
		}
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
		
		//If i1 is not i2's parent, no need to compare
		if (i2.getParent() != i1) {
			return false;
		}
		
		if (!i2.getBackend().equals(this.getName()))
			return false;
		else
			return true;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "C:Negatable";
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return new Negate();
	}

}

