package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Negate;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Reverse;

public class Invertable extends PairwiseMetamorphicProperty{
	
	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		// TODO Auto-generated method stub
		
		if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {
			int rt1Length = Array.getLength(returnValue1);
			int rt2Length = Array.getLength(returnValue2);
			
			if (rt1Length != rt2Length)
				return false;
			
			double tmp1, tmp2;
			for (int i = 0; i < rt1Length; i++) {
				tmp1 = ((Number)Array.get(returnValue1, i)).doubleValue();
				tmp2 = ((Number)Array.get(returnValue2, rt1Length - i - 1)).doubleValue();
				
				if (tmp1 != tmp2)
					return false;
			}
			return true;
			
		} else if (Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass())) {
			List o1List = new ArrayList((Collection)returnValue1);
			List o2List = new ArrayList((Collection)returnValue2);
						
			Collections.sort(o1List);
			Collections.sort(o2List);
			
			/*for (Object obj: o1List) {
				System.out.println("Check return value o1 of invertable: " + ((Number)obj).doubleValue());
			}
			
			for (Object obj: o2List) {
				System.out.println("Check return value o2 of invertable: " + ((Number)obj).doubleValue());
			}*/
			
			for (int i = 0; i < o1List.size(); i++) {
				if (((Number)o1List.get(i)).doubleValue() != ((Number)o2List.get(i)).doubleValue())
					return false;
			}
			
			return true;
		} else if (Number.class.isAssignableFrom(returnValue1.getClass()) && Number.class.isAssignableFrom(returnValue2.getClass())) {
			if (((Number)returnValue1).doubleValue() != ((Number)returnValue2).doubleValue())
				return false;
			else
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
		
		//If i1 is not i2's parent, no need to compare
		if (i2.getParent() != i1) {
			return false;
		}
		
		if (!i2.getBackend().equals(this.getName()))
			return false;
		
		if (o1.getClass().isArray() && o2.getClass().isArray()) {
			int o1Length = Array.getLength(o1);
			int o2Length = Array.getLength(o2);
			
			//Input params of parent should be the same with child
			if (o1Length != o2Length)
				return false;
			
			double o1Val, o2Val;
			for (int i = 0; i < o1Length; i++) {
				o1Val = ((Number)Array.get(o1, i)).doubleValue();
				o2Val = ((Number)Array.get(o2, o1Length - i - 1)).doubleValue();
				
//				System.out.println("Check o1Val in Invertable: " + o1Val);
//				System.out.println("Check o2Val in Invertable: " + o2Val);
				
				if (o1Val != o2Val)
					return false;
			}
			
			return true;
		} else if (Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass())) {
			int o1Length, o2Length;
			double o1Val, o2Val;
			Object[] o1Array = ((Collection)o1).toArray();
			Object[] o2Array = ((Collection)o2).toArray();
			
			o1Length = o1Array.length;
			o2Length = o2Array.length;
			
			if (o1Length != o2Length)
				return false;
			
			for (int i = 0; i < o1Length; i++) {
				o1Val = ((Number)o1Array[i]).doubleValue();
				o2Val = ((Number)o2Array[o1Length - i - 1]).doubleValue();
				
				if (o1Val != o2Val)
					return false;
			}
			
			return true;
		} 
		
		return false;
	}

	@Override
	protected int[] getInterestedVariableIndices() {
		// TODO Auto-generated method stub
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

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "C:Invertable";
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return new Reverse();
	}

}
