package edu.columbia.cs.psl.mountaindew.absprop;

import java.util.ArrayList;
import java.util.Collection;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;

public abstract class RelationAbstract extends PairwiseMetamorphicProperty{

	protected abstract boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2);

	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2,
			int interestedVariable) {
		if (i2.getParent() != i1)
			return false;
		
		if (!i2.getBackend().equals(this.getName()))
			return false;
		
		return true;
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
	
	protected boolean checkParameterType(Object oriInput, Object oriOutput, Object transInput, Object transOutput) {
		return singleChecking(oriInput)&&singleChecking(oriOutput)&&singleChecking(transInput)&&singleChecking(transOutput);
	}
	
	protected boolean singleChecking(Object checked) {
		if (checked.getClass().isArray())
			return true;
		
		if (Collection.class.isAssignableFrom(checked.getClass()))
			return true;
		
		return false;
	}

	public abstract String getName();

	public abstract MetamorphicInputProcessor getInputProcessor();
	
}
