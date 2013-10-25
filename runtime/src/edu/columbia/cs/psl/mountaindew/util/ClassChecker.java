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
		
		Object val;
		Class valClass;
		for (Object key: realMap.keySet()) {
			val = realMap.get(key);
			valClass = val.getClass();
			if (valClass.isPrimitive() || 
					valClass.isArray() || 
					Number.class.isAssignableFrom(valClass) ||
					Collection.class.isAssignableFrom(valClass) ||
					Map.class.isAssignableFrom(valClass)) {
				filterResult.put(key, val);
			} else {
				try {
					if (comparableClass(val)) 
						filterResult.put(key, val);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		return filterResult;
	}
	
	public static boolean comparableClass(Object oriObj) {
		Class objClass = oriObj.getClass();
		try {
			Class eMethodClass = objClass.getMethod("equals", Object.class).getDeclaringClass();
			
			if (!eMethodClass.getName().equals("java.lang.Object"))
				return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

}
