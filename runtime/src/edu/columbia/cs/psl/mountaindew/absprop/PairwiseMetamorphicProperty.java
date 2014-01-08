package edu.columbia.cs.psl.mountaindew.absprop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.LogState;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.mountaindew.absprop.MetamorphicProperty.PropertyResult.Result;
import edu.columbia.cs.psl.mountaindew.runtime.MethodProfiler;
import edu.columbia.cs.psl.mountaindew.util.ClassChecker;
import edu.columbia.cs.psl.mountaindew.util.MetaSerializer;

public abstract class PairwiseMetamorphicProperty extends MetamorphicProperty{
	
	private static String outputKey = "__metamorphicOutput";
	
	private static String uninitialized = "unintitialized";
	
	private static String localMap = "stateStorage";
	
	private MethodProfiler mProfiler = new MethodProfiler();
	
	private Map<String, Set<String>> classFieldMap = new HashMap<String, Set<String>>();
	
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
								//By default, we use original data as testing data.
								//Its adapter developer's responsibility to provide correct data in adaptOut of adapter.
								this.targetAdapter.setData(o1, i.returnValue, o2, j.returnValue);
								this.targetAdapter.setDefaultTestingData(o1);
								
								//Recorder for recording metamorphic testing result
								HashMap<String, Object> recorder1 = new HashMap<String, Object>();
								HashMap<String, Object> recorder2 = new HashMap<String, Object>();
								
								HashMap<String, Set<String>> fieldRecorder1 = new HashMap<String, Set<String>>();
								HashMap<String, Set<String>> fieldRecorder2 = new HashMap<String, Set<String>>();
								
								//Get the classmap from child
								this.targetAdapter.setStateDefinition(j.getClassMap());
								
								//Record state of Method object
								Object calleei = i.getCallee();
								Object calleej = j.getCallee();
								
								this.recursiveRecordState(recorder1, calleei, true);
								this.recursiveRecordState(recorder2, calleej, false);
								
								System.out.println("Check callee: " + calleei.getClass().getName());
						
								Object adaptRt1 = this.targetAdapter.adaptOutput(i.returnValue, o1);
								Object adaptRt2 = this.targetAdapter.adaptOutput(j.returnValue, o2);
								
								//Include output into comparison
								String outputFullName = i.returnValue.getClass().getName() + ":" + outputKey;
								if (adaptRt1 != null) {
									recorder1.put(outputFullName, adaptRt1);
								} else {
									recorder1.put(outputFullName, "void");
								}
								
								if (adaptRt2 != null) {
									recorder2.put(outputFullName, adaptRt2);
								} else {
									recorder2.put(outputFullName, "void");
								}
								
								//Recursively check if field in output object is metamorphic
								this.recursiveRecordState(recorder1, i.returnValue, true);
								this.recursiveRecordState(recorder2, j.returnValue, false);
								
								Object tmpObj1 = null;
								Object tmpObj2 = null;
								
								System.out.println("Check recorder1: " + recorder1);
								System.out.println("Check recorder2: " + recorder2);
								
								String version = MetaSerializer.extractVersion(i.returnValue);
								
								if (version != null)
									MetaSerializer.serializeClassFieldMap(version, this.classFieldMap);
								
								for (String tmpKey: recorder1.keySet()) {
									tmpObj1 = recorder1.get(tmpKey);
									tmpObj2 = recorder2.get(tmpKey);
									
									PropertyResult result = new PropertyResult();
									result.result = Result.UNKNOWN;
									
									this.mProfiler.addMethodProfile(i, j, result);
									
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
							}
						}
					}
			}
		}
		
		return propertyList;
		
	}
	
	private void recursiveRecordState(HashMap<String, Object> recorder, Object obj, boolean shouldSerialize) {
		if (obj == null)
			return ;
		
		if (obj.getClass().getAnnotation(LogState.class) == null)
			return ;
		
		try {
			String fieldName;
			Object fieldValue;
			
			//Get all Fields in this obj and its parent
			Set<Field> myFields = new HashSet(Arrays.asList(obj.getClass().getDeclaredFields()));
			Set<Field> parentFields = new HashSet(Arrays.asList(obj.getClass().getFields()));
			
			Set<Field> combinedFields = new HashSet();
			combinedFields.addAll(myFields);
			combinedFields.addAll(parentFields);
			
			String objClassName = obj.getClass().getName();
			Map<Integer, String> localVarMap = MetaSerializer.deserializeLocalVarMap(objClassName);
			System.out.println("Check localVarMap in pairwise: " + localVarMap);
			
			Set<String> allFields = new HashSet<String>();
			for(Field field: combinedFields) {
				fieldName = field.getName();
				
				if (fieldName.contains("__invivoCloned") || 
						fieldName.contains("___interceptor__by_mountaindew") ||
						fieldName.contains("__metamorphicChildCount"))
					continue;
				
				field.setAccessible(true);
				fieldValue = field.get(obj);
				
				boolean basic = ClassChecker.basicClass(fieldValue);
				boolean comparable = ClassChecker.comparableClass(fieldValue, "equals", Object.class);
				boolean stringable = ClassChecker.comparableClass(fieldValue, "toString");
				boolean annotable = (fieldValue == null)? false: 
					(fieldValue.getClass().getAnnotation(LogState.class) == null?false: true);
				
				if (!basic && !comparable && !stringable && !annotable)
					continue;
					
				if (fieldName.equals(localMap)) {
					//Flatten local variable map
					Map tmpMap = (HashMap)ClassChecker.comparableClasses(fieldValue);
					for (Object key: tmpMap.keySet()) {
						recorder.put(objClassName + ":" + localVarMap.get(key) + MetaSerializer.localSuffix, tmpMap.get(key));
						allFields.add(localVarMap.get(key) + MetaSerializer.localSuffix);
					}
				} else if (fieldValue != null) {
					if (comparable || basic)
						recorder.put(objClassName + ":" + fieldName, fieldValue);
					else
						recorder.put(objClassName + ":" + fieldName, fieldValue.toString());
					
					allFields.add(fieldName);
				} else {
					recorder.put(objClassName + ":" + fieldName, uninitialized);
					allFields.add(fieldName);
				}
				
				recursiveRecordState(recorder, fieldValue, shouldSerialize);
			}
			
			if (shouldSerialize) {
				this.classFieldMap.put(objClassName, allFields);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
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
		
		List retList = new ArrayList();
		
		if (val.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(val); i++) {
				Object tmp = Array.get(val, i);
				if (Number.class.isAssignableFrom(tmp.getClass())) {
					retList.add((Number)tmp);
				} else {
					retList.add(returnList(tmp));
				}
			}
		} else if (Collection.class.isAssignableFrom(val.getClass())) {
			Collection tmpCollection = (Collection)val;
			
			for (Object t: tmpCollection) {
				if (Number.class.isAssignableFrom(t.getClass())) {
					retList.add((Number)t);
				} else {
					retList.add(returnList(t));
				}
			}
		}
		return retList;
	}
	
	protected double roundDouble(double numberToRound, int digit) {
		int roundMultiplier = (int)Math.pow(10, digit);
		numberToRound = numberToRound * roundMultiplier;
		numberToRound = Math.round(numberToRound);
		numberToRound = numberToRound / roundMultiplier;
		return numberToRound;
	}
	
	public MethodProfiler getMethodProfiler() {
		return this.mProfiler;
	}

	protected abstract boolean returnValuesApply(Object p1, Object returnValue1, Object p2, Object returnValue2);
	protected abstract boolean propertyApplies(MethodInvocation i1, MethodInvocation i2, int interestedVariable);
	protected abstract int[] getInterestedVariableIndices();

}
