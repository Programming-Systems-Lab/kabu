package edu.columbia.cs.psl.mountaindew.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

public class FieldCollector {
	
	public static Field[] collectFields(Class clazz) {
		HashSet<Field> myFields = new HashSet<Field>(Arrays.asList(clazz.getFields()));
		HashSet<Field> parentFields = new HashSet<Field>(Arrays.asList(clazz.getFields()));
		
		Comparator<Field> fieldSorter = new Comparator<Field>() {
			public int compare(Field a, Field b) {
				String aString = a.toString() + a.getName();
				String bString = b.toString() + b.getName();
				
				return aString.compareTo(bString);
			}
		};
		
		TreeSet<Field> totalFields = new TreeSet<Field>(fieldSorter);
		totalFields.addAll(myFields);
		totalFields.addAll(parentFields);
		
		return (Field[])totalFields.toArray();
	}
	
	public static void collectFields(Class clazz, List<Field> collector) {
		if (clazz == null)
			return;
		
		System.out.println("Collecting fields for: " + clazz.getCanonicalName());
		Field[] myFields = clazz.getDeclaredFields();
		
		for (Field f: myFields) {
			if (shouldFilterField(f.getName())) {
				continue;
			}
			
			collector.add(f);
		}
		
		Class superClass = clazz.getSuperclass();
		collectFields(superClass, collector);
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
