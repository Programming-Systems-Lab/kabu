package edu.columbia.cs.psl.mountaindew.property;

import java.util.Collection;

import edu.columbia.cs.psl.metamorphic.struct.MethodInvocation;
import edu.columbia.cs.psl.mountaindew.property.MetamorphicProperty.PropertyResult;
import edu.columbia.cs.psl.mountaindew.property.MetamorphicProperty.PropertyResult.Result;

public abstract class PairwiseMetamorphicProperty extends MetamorphicProperty{


	@Override
	public final PropertyResult propertyHolds() {
		PropertyResult result = new PropertyResult();
		result.result=Result.UNKNOWN;
		result.property=this.getClass();
		
		int[] interestedIndices = getInterestedVariableIndices();
		for(MethodInvocation i : getInvocations())
		{
			for(int k : interestedIndices)
			{
				Object v = i.params[k];
					for(MethodInvocation j : getInvocations())
					{
						if(i!=j)
						{
							Object o1 = v;
							Object o2 = j.params[k];
							if(propertyApplies(i,j,k))
							{
								if(returnValuesApply(o1,i.returnValue,o2,j.returnValue))
								{
									//Property may hold
									result.result=Result.HOLDS;
									result.holds=true;
									result.supportingSize++;
								}
								else
								{
									//Property definitely doesn't hold
									result.result=Result.DOES_NOT_HOLD;
									result.holds=false;
									result.supportingSize++;
									return result;
								}
							}
						}
					}
			}
		}
		return result;
	}

	protected abstract boolean returnValuesApply(Object p1, Object returnValue1, Object p2, Object returnValue2);
	protected abstract boolean propertyApplies(MethodInvocation i1, MethodInvocation i2, int interestedVariable);

	protected abstract int[] getInterestedVariableIndices();

}
