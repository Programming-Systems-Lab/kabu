package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Shuffle;

public class Shufflable extends PairwiseMetamorphicProperty {

	@Override
	public String getName() {
		return "Shuffleable";
	}


	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {

		//For array
		if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {
			int rt1Length = Array.getLength(returnValue1);
			int rt2Length = Array.getLength(returnValue2);
			
			if (rt1Length != rt2Length)
				return false;
			
			double tmp1, tmp2;
			for (int i = 0; i < rt1Length; i++) {
				tmp1 = ((Number)Array.get(returnValue1, i)).doubleValue();
				tmp2 = ((Number)Array.get(returnValue2, i)).doubleValue();
				
				if (tmp1 != tmp2)
					return false;
			}
			return true;
		}
		
		//For other type, includind Collection
		return returnValue1.equals(returnValue2);
	}

	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2, int interestedVariable) {
		Object o1 = i1.params[interestedVariable];
		Object o2 = i2.params[interestedVariable];
		for(int i = 0;i<i1.params.length;i++)
			if(i!=interestedVariable && !i1.params[i].equals(i2.params[i]))
				return false;
		
		//If i1 is not i2's parent, no need to compare
		if (i2.getParent() != i1) {
			return false;
		}
		
		if(o1.getClass().isArray() && o2.getClass().isArray())
		{
			if(Array.getLength(o1) != Array.getLength(o2))
				return false;
			HashSet<Double> o1h = new HashSet<Double>();
			HashSet<Double> o2h = new HashSet<Double>();
			for(int i = 0;i<Array.getLength(o1);i++)
			{
//				System.out.println("Test oriInput: " + (Number)Array.get(o1, i));
//				System.out.println("Test transInput: " + (Number)Array.get(o2, i));
				
				o1h.add(((Number)Array.get(o1, i)).doubleValue());
				o2h.add(((Number)Array.get(o2, i)).doubleValue());
			}
			return o1h.equals(o2h);
		}
		else if(Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass()))
		{
			if (((Collection)o1).size() != ((Collection)o2).size())
				return false;
			HashSet<Object> o1h = new HashSet<Object>();
			HashSet<Object> o2h = new HashSet<Object>();
			o1h.addAll((Collection) o1);
			o2h.addAll((Collection) o2);
			return o1h.equals(o2h);
		}
		else if(Number.class.isAssignableFrom(o1.getClass()) && Number.class.isAssignableFrom(o2.getClass())) {
//			System.out.println("Check o1 value: " + ((Number)o1).doubleValue());
//			System.out.println("Check o2 value: " + ((Number)o2).doubleValue());
			return (((Number)o1).doubleValue() == ((Number)o2).doubleValue());
		}
		return false;
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
	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		return new Shuffle();
	}

}
