package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.mountaindew.property.MetamorphicProperty.PropertyResult.Result;
import edu.columbia.cs.psl.mountaindew.runtime.MethodProfiler;

public abstract class PairwiseMetamorphicProperty extends MetamorphicProperty{
	
	private MethodProfiler mProfiler = new MethodProfiler();


	@Override
	public final PropertyResult propertyHolds() {		
		PropertyResult result = new PropertyResult();
		result.result=Result.UNKNOWN;
		result.property=this.getClass();
		
		int[] interestedIndices = getInterestedVariableIndices();
		//Check interestedIndeices
		/*System.out.println("Indice length:  " + interestedIndices.length);
		for (int i = 0; i < interestedIndices.length; i++) {
			System.out.println("Check index: " + interestedIndices[i]);
		}*/
		
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
							// In order to make o1 is the ori (parent) and o2 is the transformation (child)
							/*Object o1;
							Object o2;
							
							System.out.println("Test i: " + i + " " + i.getParent() + " " + i.getReturnValue());
							System.out.println("Test j: " + j + " " + j.getParent() + " " + j.getReturnValue());
							
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
							}*/
							
							if(propertyApplies(i, j, k))
							{
								//mProfiler.addMethodProfile(i, j, result);
								if(returnValuesApply(o1, i.returnValue, o2, j.returnValue))
								//if (returnValuesApply(o2, j.returnValue, o1, i.returnValue))
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
	
	protected List returnList(Object val) {
		
		List retList = null;
		
		if (val.getClass().isArray()) {
			retList = new ArrayList();
			
			for (int i = 0; i < Array.getLength(val); i++) {
				retList.add(Array.get(val, i));
			}
		}
		else if (Collection.class.isAssignableFrom(val.getClass()))
			retList = new ArrayList((Collection)val);
		
		return retList;
		
	}
	
	public MethodProfiler getMethodProfiler() {
		return this.mProfiler;
	}

	protected abstract boolean returnValuesApply(Object p1, Object returnValue1, Object p2, Object returnValue2);
	protected abstract boolean propertyApplies(MethodInvocation i1, MethodInvocation i2, int interestedVariable);

	protected abstract int[] getInterestedVariableIndices();

}
