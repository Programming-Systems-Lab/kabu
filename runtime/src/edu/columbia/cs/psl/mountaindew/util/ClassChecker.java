package edu.columbia.cs.psl.mountaindew.util;

import java.util.Collection;

public class ClassChecker {
	
	public static Class<?> checkClass(Object o) {
		Class<?> objClass = o.getClass();
		if (Number.class.isAssignableFrom(objClass)) {
			return Number.class;
		} else if (String.class.isAssignableFrom(objClass)) {
			return String.class; 
		} else if (Collection.class.isAssignableFrom(objClass)) {
			return Collection.class;
		}else {
			return objClass;
		}
	}

}
