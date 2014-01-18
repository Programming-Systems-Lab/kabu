package edu.columbia.cs.psl.mountaindew.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;

public class FieldCollector {
	
	public static void collectFields(Class clazz, Set<Field> allFields) {
		try {
			Field[] fields = clazz.getDeclaredFields();
			
			for (Field f: fields) {
				allFields.add(f);
			}
			
			Class superClass = clazz.getSuperclass();
			if (superClass != null)
				collectFields(superClass, allFields);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void collectMethodMaps(Class clazz, 
			Map<Class, Map<String, Map<Integer, String>>> inheritenceMap) {
		try {
			Method[] methods = clazz.getDeclaredMethods();
			inheritenceMap.put(clazz, MetaSerializer.deserializedAllClassLocalVarMap(clazz.getName(), methods));
			
			Class superClass = clazz.getSuperclass();
			if (superClass != null)
				collectMethodMaps(superClass, inheritenceMap);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void collectAndFilterFields(Class clazz, Method m, List<Field> collector) {
		if (clazz == null)
			return;
		
		System.out.println("Collecting fields for: " + clazz.getCanonicalName());
		Field[] myFields = clazz.getDeclaredFields();
		Set<String> usedVar = MetaSerializer.deserializeUsedVarSet(clazz.getName(), m);
		
		if (usedVar != null) {
			for (Field f: myFields) {
				if (shouldFilterField(f.getName())) {
					continue;
				}
				
				if (usedVar.contains(f.getType().getName() + ":" + f.getName()))
					collector.add(f);
			}
		}
				
		Class superClass = clazz.getSuperclass();
		collectAndFilterFields(superClass, m, collector);
	}
	
	public static Class getCorrectMethodOwner(Class clazz, 
			Map<Class, Map<String, Map<Integer, String>>> inheritenceMap) {
		Map<String, Map<Integer, String>> methodMap = inheritenceMap.get(clazz);
		
		if (methodMap != null && methodMap.size() > 0) {
			return clazz;
		} else {
			return getCorrectMethodOwner(clazz.getSuperclass(), inheritenceMap);
		}
	}
	
	public static boolean shouldFilterField(String fieldName) {
		if (fieldName.contains("__meta_should_trans") ||
				fieldName.contains("__invivoCloned") ||
				fieldName.contains("__meta_gen") ||
				fieldName.contains("___interceptor__by_mountaindew") ||
				fieldName.contains("___interceptor__by_mountaindew_static") ||
				fieldName.contains("__metamorphicChildCount") ||
				fieldName.equals("__meta_obj_map") ||
				fieldName.equals("__meta_static_map") ||
				fieldName.equals("__meta_valid_case")) {
			return true;
		}
		
		return false;
	}

}
