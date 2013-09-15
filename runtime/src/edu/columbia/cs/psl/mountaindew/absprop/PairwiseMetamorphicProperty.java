package edu.columbia.cs.psl.mountaindew.absprop;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.mountaindew.absprop.MetamorphicProperty.PropertyResult.Result;
import edu.columbia.cs.psl.mountaindew.runtime.MethodProfiler;

public abstract class PairwiseMetamorphicProperty extends MetamorphicProperty{
	
	private MethodProfiler mProfiler = new MethodProfiler();
	
	
	@Override
	public final List<PropertyResult> propertiesHolds() {
		List<PropertyResult> propertyList = new ArrayList<PropertyResult>();
		
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
							
							if(propertyApplies(i, j, k))
							{
								PropertyResult result = new PropertyResult();
								result.result = Result.UNKNOWN;
								
								this.mProfiler.addMethodProfile(i, j, result);
								
								//By default, we use original data as testing data.
								//It's adapter developer's responsibility to provide correct data in adaptOut of adapter.
								this.targetAdapter.setData(o1, i.returnValue, o2, j.returnValue);
								this.targetAdapter.setDefaultTestingData(o1);
								
								HashMap<String, Object> recorder1 = new HashMap<String, Object>();
								HashMap<String, Object> recorder2 = new HashMap<String, Object>();
								
								Object adaptRt1 = this.targetAdapter.adaptOutput(recorder1, i.returnValue, o1);
								Object adaptRt2 = this.targetAdapter.adaptOutput(recorder2, j.returnValue, o2);
								
								Object tmpObj1 = null;
								Object tmpObj2 = null;
								for (String tmpKey: recorder1.keySet()) {
									tmpObj1 = recorder1.get(tmpKey);
									tmpObj2 = recorder2.get(tmpKey);
									
									result.stateItem = tmpKey;
									
									if (returnValuesApply(o1, tmpObj1, o2, tmpObj2)) {
										//Property may hold
										result.result = Result.HOLDS;
										result.holds = true;
										result.supportingSize++;
										result.supportingInvocations.add(new MethodInvocation[]{i, j});
										result.combinedProperty = j.getFrontend() + "=>" + j.getBackend();
										this.mProfiler.addHoldMethodProfile(i, j, result);
									} else {
										//Property definitely does not hold
										result.result = Result.DOES_NOT_HOLD;
										result.holds = false;
										result.antiSupportingSize++;
										result.antiSupportingInvocations.add(new MethodInvocation[]{i, j});
										result.combinedProperty = j.getFrontend() + "=>" + j.getBackend();
									}
									propertyList.add(result);
								}
								
								/*if (returnValuesApply(o1, adaptRt1, o2, adaptRt2))
								{
									//Property may hold
									result.result=Result.HOLDS;
									result.holds=true;
									result.supportingSize++;
									result.supportingInvocations.add(new MethodInvocation[] {i, j});
									result.combinedProperty = j.getFrontend() + "=>" + j.getBackend();
									this.mProfiler.addHoldMethodProfile(i, j, result);
								}
								else
								{
									//Property definitely doesn't hold
									result.result=Result.DOES_NOT_HOLD;
									result.holds=false;
									result.antiSupportingSize++;
									result.antiSupportingInvocations.add(new MethodInvocation[] {i, j});
									result.combinedProperty = j.getFrontend() + "=>" + j.getBackend();
								}
								propertyList.add(result);*/
							}
						}
					}
			}
		}
		
		return propertyList;
		
	}
	

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
								mProfiler.addMethodProfile(i, j, result);
								if(returnValuesApply(o1, i.returnValue, o2, j.returnValue))
								//if (returnValuesApply(o2, j.returnValue, o1, i.returnValue))
								{
									//Property may hold
									result.result=Result.HOLDS;
									result.holds=true;
									result.supportingSize++;
									result.supportingInvocations.add(new MethodInvocation[] {i, j});
									result.combinedProperty = j.getFrontend() + "=>" + j.getBackend();
								}
								else
								{
									//Property definitely doesn't hold
									result.result=Result.DOES_NOT_HOLD;
									result.holds=false;
									result.antiSupportingSize++;
									result.antiSupportingInvocations.add(new MethodInvocation[] {i, j});
									result.combinedProperty = j.getFrontend() + "=>" + j.getBackend();
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
		else if (Number.class.isAssignableFrom(val.getClass())) {
			retList = new ArrayList();
			retList.add(val);
		}
		
		return retList;
		
	}
	
	public MethodProfiler getMethodProfiler() {
		return this.mProfiler;
	}

	protected abstract boolean returnValuesApply(Object p1, Object returnValue1, Object p2, Object returnValue2);
	protected abstract boolean propertyApplies(MethodInvocation i1, MethodInvocation i2, int interestedVariable);

	protected abstract int[] getInterestedVariableIndices();

}
