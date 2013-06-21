package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;

public class SizeChecker extends PairwiseMetamorphicProperty{

	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		int inputSize = this.returnObjectLength(p2);
		int outputSize = this.returnObjectLength(returnValue2);
		
		if (inputSize == 0 || outputSize == 0)
			return false;
		
		//Only check transformed input output
		return inputSize != outputSize;
	}

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

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "SizeChecker";
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private int returnObjectLength(Object target) {
		int ret = 0;
		if (Number.class.isAssignableFrom(target.getClass()))
			ret = 1;
		else if (target.getClass().isArray())
			ret = Array.getLength(target);
		else if (Collection.class.isAssignableFrom(target.getClass()))
			ret = ((Collection)target).size();
		
		return ret;
	}
	
	

}
