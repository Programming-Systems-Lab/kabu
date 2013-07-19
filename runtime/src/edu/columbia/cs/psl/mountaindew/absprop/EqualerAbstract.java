package edu.columbia.cs.psl.mountaindew.absprop;

import java.util.ArrayList;
import java.util.Collection;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;

public abstract class EqualerAbstract extends PairwiseMetamorphicProperty{
	
	@Override
	protected int[] getInterestedVariableIndices() {
		ArrayList<Integer> rets = new ArrayList<Integer>();
		for(int i = 0;i<getMethod().getParameterTypes().length; i++)
		{
			if(getMethod().getParameterTypes()[i].isArray() || 
					Collection.class.isAssignableFrom(getMethod().getParameterTypes()[i]) ||
					String.class.isAssignableFrom(getMethod().getParameterTypes()[i]))
				rets.add(i);
		}
		int[] ret = new int[rets.size()];
		for(int i = 0;i<rets.size();i++)
			ret[i]=rets.get(i);
		return ret;
	}
	
	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected abstract boolean returnValuesApply(Object p1, Object returnValue1, Object p2, Object returnValue2);
	protected abstract boolean propertyApplies(MethodInvocation i1, MethodInvocation i2, int interestedVariable);
	//protected abstract boolean checkEquivalence(Collection c1, Collection c2);
	protected abstract boolean checkEquivalence(Object c1, Object c2);
	//protected abstract boolean checkEquivalence(Object[] objArray1, Object[] objArray2);

}
