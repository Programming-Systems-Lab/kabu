package edu.columbia.cs.psl.mountaindew.property;

import java.util.ArrayList;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.MultiplyByNumericConstant;
import edu.columbia.cs.psl.invivo.struct.MethodInvocation;

public class MultiplicativeByConstant extends PairwiseMetamorphicProperty {
	@Override
	public String getName() {
		return "Multiplicative";
	}

	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		try
		{
			if(returnValue1.equals(returnValue2))
				return true;
			return getDivisor(p1, p2) == getDivisor(returnValue1, returnValue2);
		}
		catch(IllegalArgumentException ex)
		{
			return false;
		}
	}

	private double getDivisor(Object o1, Object o2) throws IllegalArgumentException
	{
		if(!o1.getClass().equals(o2.getClass()))
			throw new IllegalArgumentException("Both parameters must be of the same type");
		if(o1.getClass().equals(Integer.class) || o1.getClass().equals(Integer.TYPE))
		{
			if((Integer) o2 != 0)
				return ((Integer) o1) / ((Integer) o2);
		}
		else if(o1.getClass().equals(Short.class) || o1.getClass().equals(Short.TYPE))
		{
			if((Short) o2 != 0)
				return ((Short) o1) / ((Short) o2);
		}
		else if(o1.getClass().equals(Long.class) || o1.getClass().equals(Long.TYPE))
		{
			if((Long) o2 != 0)
				return ((Long) o1) / ((Long) o2);
		}
		else if(o1.getClass().equals(Double.class) || o1.getClass().equals(Double.TYPE))
		{
			if((Double) o2 != 0)
				return ((Double) o1) / ((Double) o2);
		}
		throw new IllegalArgumentException("Non numeric types");
	}
	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2,
			int interestedVariable) {
		return true;
	}

	@Override
	protected int[] getInterestedVariableIndices() {
		ArrayList<Integer> rets = new ArrayList<Integer>();
		for(int i = 0;i<getMethod().getParameterTypes().length; i++)
		{
			if(getMethod().getParameterTypes()[i].equals(Integer.TYPE) || 
					getMethod().getParameterTypes()[i].equals(Short.TYPE) || 
					getMethod().getParameterTypes()[i].equals(Long.TYPE) || 
					getMethod().getParameterTypes()[i].equals(Double.TYPE) || Integer.class.isAssignableFrom(getMethod().getParameterTypes()[i]) || Float.class.isAssignableFrom(getMethod().getParameterTypes()[i])|| Double.class.isAssignableFrom(getMethod().getParameterTypes()[i]))
				rets.add(i);
		}
		int[] ret = new int[rets.size()];
		for(int i = 0;i<rets.size();i++)
			ret[i]=rets.get(i);
		return ret;
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		return new MultiplyByNumericConstant(); //TODO
	}
}
