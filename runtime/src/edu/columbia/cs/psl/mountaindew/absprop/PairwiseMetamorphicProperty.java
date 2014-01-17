package edu.columbia.cs.psl.mountaindew.absprop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
	
	private static String objMap = "__meta_obj_map";
	
	private static String staticMap = "__meta_static_map";
	
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
				Object v = null;
				if (i.params.length > k) {
					v = i.params[k];
				}
					for(MethodInvocation j : getInvocations())
					{
						if(i!=j)
						{
							Object o1 = v;
							Object o2 = null;
							if (j.params.length > k) {
								o2 = j.params[k];
							}
							
							if(propertyApplies(i, j, k))
							{								
								//By default, we use original data as testing data.
								//Its adapter developer's responsibility to provide correct data in adaptOut of adapter.
								this.targetAdapter.setData(o1, i.returnValue, o2, j.returnValue);
								this.targetAdapter.setDefaultTestingData(o1);
								
								//Recorder for recording metamorphic testing result
								HashMap<String, Object> recorder1 = new HashMap<String, Object>();
								HashMap<String, Object> recorder2 = new HashMap<String, Object>();
								
								//Get the classmap from child
								this.targetAdapter.setStateDefinition(j.getClassMap());
								
								//Record state of Method object
								Object calleei = i.getCallee();
								Object calleej = j.getCallee();
								
								long parentId = i.thread.getId();
								long childId = j.thread.getId();
								
								System.out.println("Check callee: " + calleei.getClass().getName());
								System.out.println("Parent thread id: " + parentId);
								System.out.println("Child thread id: " + childId);
								
								this.recursiveRecordState(recorder1, calleei, true, parentId);
								this.recursiveRecordState(recorder2, calleej, false, childId);
						
								Object adaptRt1 = this.targetAdapter.adaptOutput(i.returnValue, o1);
								Object adaptRt2 = this.targetAdapter.adaptOutput(j.returnValue, o2);
								
								//Include output into comparison
								//String outputFullName = i.returnValue.getClass().getName() + ":" + outputKey;
								//String outputFullName = calleei.getClass().getName() + ":" + i.getMethod().getName() + "_" +outputKey;
								String outputFullName = MetaSerializer.composeFullMethodName(calleei.getClass().getName(), i.getMethod()) + "_" + outputKey;
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
								this.recursiveRecordState(recorder1, i.returnValue, true, parentId);
								this.recursiveRecordState(recorder2, j.returnValue, false, childId);
								
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
									System.out.println("Check tmpKey: " + tmpKey);
									
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
	
	private void recursiveRecordState(HashMap<String, Object> recorder, Object obj, boolean shouldSerialize, long threadId) {
		if (obj == null)
			return ;
		
		System.out.println("In recursiveRecordeState: " + obj.getClass().getName());
		
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
			
			//Need a Map<methodName, localVarMap>
			Set<Method> myMethods = new HashSet(Arrays.asList(obj.getClass().getDeclaredMethods()));
			Set<Method> parentMethods = new HashSet(Arrays.asList(obj.getClass().getMethods()));
			
			Set<Method> combinedMethods = new HashSet();
			combinedMethods.addAll(myMethods);
			combinedMethods.addAll(parentMethods);
			
			String objClassName = obj.getClass().getName();
			String superClassName = obj.getClass().getSuperclass().getName();
			Map<String, Map<Integer, String>> methodVarMap = 
					MetaSerializer.deserializedAllClassLocalVarMap(objClassName, combinedMethods);
			Map<String, Map<Integer, String>> sMethodVarMap = 
					MetaSerializer.deserializedAllClassLocalVarMap(superClassName, parentMethods);
			
			System.out.println("Check all localVarMap in pairwise: " + methodVarMap);
			System.out.println("Check all parentVarMap in pairwise: " + sMethodVarMap);
			
			Set<String> allFields = new HashSet<String>();
			for(Field field: combinedFields) {
				fieldName = field.getName();
				
				if (fieldName.contains("__invivoCloned") || 
						fieldName.contains("___interceptor__by_mountaindew") ||
						fieldName.contains("__metamorphicChildCount") ||
						fieldName.contains("__meta_gen") ||
						fieldName.contains("__meta_should_trans"))
					continue;
				
				field.setAccessible(true);
				fieldValue = field.get(obj);
				System.out.println("Check field: " + fieldName + " " + fieldValue);
				
				boolean basic = ClassChecker.basicClass(fieldValue);
				boolean comparable = ClassChecker.comparableClass(fieldValue, "equals", Object.class);
				boolean stringable = ClassChecker.comparableClass(fieldValue, "toString");
				boolean annotable = (fieldValue == null)? false: 
					(fieldValue.getClass().getAnnotation(LogState.class) == null?false: true);
				
				if (!basic && !comparable && !stringable && !annotable)
					continue;
					
				if (fieldName.equals(objMap)) {
					//Flatten all local variable map
					System.out.println("Show obj map: " + fieldValue);
					Map allMaps = (Map)fieldValue;
					
					for (Object methodName: allMaps.keySet()) {
						Map tmpMap = (HashMap)ClassChecker.comparableClasses(allMaps.get(methodName));
						String localKey = objClassName + ":" + methodName;
						Map varMap = (HashMap)(methodVarMap.get(localKey));
						
						if (varMap == null) {
							System.out.println("No var map. Check parent: " + superClassName);
							localKey = superClassName + ":" + methodName;
							
							System.out.println("Check super method var map: " + sMethodVarMap);
							
							varMap = (HashMap)(sMethodVarMap.get(localKey));
						}
						
						for (Object innerKey: tmpMap.keySet()) {
							Object innerObj = tmpMap.get(innerKey);
							
							if (innerObj.getClass().getAnnotation(LogState.class) != null) {
								recursiveRecordState(recorder, innerObj, shouldSerialize, threadId);
							} else {
								System.out.println("Local key: " + localKey);
								System.out.println("Innerkey val: " + varMap.get(innerKey));
								String fullKey = localKey + "_" + varMap.get(innerKey) + MetaSerializer.localSuffix;
								recorder.put(fullKey, tmpMap.get(innerKey));
								allFields.add(fullKey);
							}
						}
					}
				} else if (fieldName.equals(staticMap)) {
					Map allMaps = (Map)fieldValue;
					Map myMaps = new HashMap();
					
					//Filter out correct maps based on thread id
					for (Object methodName: allMaps.keySet()) {
						String tmpName = methodName.toString();
						int start = tmpName.lastIndexOf("_");
						String tName = tmpName.substring(0, start);
						String tId = tmpName.substring(start + 1);
						
						if (tId.equals(String.valueOf(threadId))) {
							System.out.println("Get correct method: " + allMaps.get(methodName));
							Map tmpMap = (HashMap)ClassChecker.comparableClasses(allMaps.get(methodName));
							String localKey = objClassName + ":" + tName;
							
							Map varMap = (HashMap)(methodVarMap.get(localKey));
							
							for (Object innerKey: tmpMap.keySet()) {
								Object innerObj = tmpMap.get(innerKey);
								
								if (innerObj.getClass().getAnnotation(LogState.class) != null) {
									recursiveRecordState(recorder, innerObj, shouldSerialize, threadId);
								} else {
									String fullKey = localKey + "_" + varMap.get(innerKey) + MetaSerializer.localSuffix;
									recorder.put(fullKey, tmpMap.get(innerKey));
									allFields.add(fullKey);
								}
							}
						}
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
				
				recursiveRecordState(recorder, fieldValue, shouldSerialize, threadId);
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
		
		if (val == null)
			return null;
		
		List retList = new ArrayList();
		
		if (val.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(val); i++) {
				Object tmp = Array.get(val, i);
				if (Number.class.isAssignableFrom(tmp.getClass())) {
					retList.add((Number)tmp);
				} else if (tmp.getClass().isArray() || Collection.class.isAssignableFrom(tmp.getClass())){
					retList.add(returnList(tmp));
				} else {
					retList.add(tmp.toString());
				}
			}
		} else if (Collection.class.isAssignableFrom(val.getClass())) {
			Collection tmpCollection = (Collection)val;
			
			for (Object t: tmpCollection) {
				if (Number.class.isAssignableFrom(t.getClass())) {
					retList.add((Number)t);
				} else if (t.getClass().isArray() || Collection.class.isAssignableFrom(t.getClass())){
					retList.add(returnList(t));
				} else {
					retList.add(t.toString());
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
	
	protected int[] getInterestedVariableIndices() {
		ArrayList<Integer> rets = new ArrayList<Integer>();
		for(int i = 0;i<getMethod().getParameterTypes().length; i++)
		{
			Class paramClass = getMethod().getParameterTypes()[i];
			if(paramClass.isArray() || 
					Collection.class.isAssignableFrom(paramClass) ||
					Number.class.isAssignableFrom(paramClass) ||
					String.class.isAssignableFrom(paramClass) ||
					paramClass.equals(Integer.TYPE) ||
					paramClass.equals(Double.TYPE) ||
					paramClass.equals(Long.TYPE) ||
					paramClass.equals(Short.TYPE) ||
					paramClass.equals(Float.TYPE)) {
				rets.add(i);
			}
		}
		
		System.out.println("Check interested variables: " + rets);
		
		//If no input type matches, target on first param
		if (rets.size() == 0) {
			rets.add(0);
		}
		
		int[] ret = new int[rets.size()];
		for(int i = 0;i<rets.size();i++)
			ret[i]=rets.get(i);
		return ret;
	}

}
