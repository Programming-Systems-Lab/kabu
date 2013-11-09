package edu.columbia.cs.psl.mountaindew.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ClassChecker {
	
	public static Class<?> checkClass(Object o) {
		Class<?> objClass = o.getClass();
		if (Number.class.isAssignableFrom(objClass)) {
			return Number.class;
		} else if (String.class.isAssignableFrom(objClass)) {
			return String.class; 
		} else if (Collection.class.isAssignableFrom(objClass)) {
			return Collection.class;
		} else {
			return objClass;
		}
	}
	
	public static Object comparableClasses(Object oriMap) {
		if (!Map.class.isAssignableFrom(oriMap.getClass()))
			return null;
		
		HashMap filterResult = new HashMap();
		HashMap realMap = (HashMap)oriMap;
		
		System.out.println("Check map: " + realMap);
		
		Object val;
		Class valClass;
		for (Object key: realMap.keySet()) {
			val = realMap.get(key);
			
			//Avoid val is null
			if (val != null) {
				valClass = val.getClass();
			} else {
				continue;
			}

			if (valClass.isPrimitive() || 
					valClass.isArray() || 
					Number.class.isAssignableFrom(valClass) ||
					Collection.class.isAssignableFrom(valClass) ||
					Map.class.isAssignableFrom(valClass)) {
				filterResult.put(key, val);
			} else {
				try {
					if (comparableClass(val, "equals", Object.class)) 
						filterResult.put(key, val);
					else if (comparableClass(val, "toString"))
						filterResult.put(key, val.toString());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		return filterResult;
	}
	
	public static boolean comparableClass(Object oriObj, String methodName, Class...params) {
		if (oriObj == null)
			return false;
		
		Class objClass = oriObj.getClass();
		try {
			Class eMethodClass = null;
			
			if (params.length != 0)
				eMethodClass = objClass.getMethod(methodName, params[0]).getDeclaringClass();
			else
				eMethodClass = objClass.getMethod(methodName).getDeclaringClass();
			
			if (!eMethodClass.getName().equals("java.lang.Object"))
				return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

}
