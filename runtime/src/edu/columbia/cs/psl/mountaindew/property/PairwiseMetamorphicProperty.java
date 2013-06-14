package edu.columbia.cs.psl.mountaindew.property;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.mountaindew.property.MetamorphicProperty.PropertyResult.Result;

public abstract class PairwiseMetamorphicProperty extends MetamorphicProperty{


	@Override
	public final PropertyResult propertyHolds() {
		PropertyResult result = new PropertyResult();
		result.result=Result.UNKNOWN;
		result.property=this.getClass();
		
		int[] interestedIndices = getInterestedVariableIndices();
		MethodInvocation parent;
		MethodInvocation child;
		for(MethodInvocation i : getInvocations())
		{
			for(int k : interestedIndices)
			{
				Object v = i.params[k];
					for(MethodInvocation j : getInvocations())
					{
						if(i!=j)
						{
//							Object o1 = v;
//							Object o2 = j.params[k];
							// In order to make o1 is the ori (parent) and o2 is the transformation (child)
							Object o1;
							Object o2;
							
							if (i == j.getParent()) {
//								System.out.println("i is parent of j");
								parent = i;
								child = j;
								o1 = v;
								o2 = j.params[k];
							} else if (j == i.getParent()) {
//								System.out.println("j is parent of i");
								parent = j;
								child = i;
								o1 = j.params[k];
								o2 = v;
							} else {
								System.err.println("Two input has no relation to compare");
								return null;
							}
							
							if(propertyApplies(parent,child,k))
							{
								if(returnValuesApply(parent,parent.returnValue,child,child.returnValue))
								{
									//Property may hold
									result.result=Result.HOLDS;
									result.holds=true;
									result.supportingSize++;
									result.supportingInvocations.add(new MethodInvocation[] {i, j});
								}
								else
								{
									//Property definitely doesn't hold
									result.result=Result.DOES_NOT_HOLD;
									result.holds=false;
									result.antiSupportingSize++;
									result.antiSupportingInvocations.add(new MethodInvocation[] {i, j});
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
