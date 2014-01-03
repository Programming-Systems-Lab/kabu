package edu.columbia.cs.psl.mountaindew.versionsorter;

import java.util.Comparator;

public class EJMLVersionSorter implements Comparator<String>{
	
	public int compare(String v1, String v2) {
		String[] v1Array = v1.split("\\.");
		String[] v2Array = v2.split("\\.");
		
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		
		for (int i = 1; i < v1Array.length; i++) {
			sb1.append(v1Array[i]);
			sb1.append(".");
		}
		
		for (int i = 1; i < v2Array.length; i++) {
			sb2.append(v2Array[i]);
			sb2.append(".");
		}
		
		String v1Real = sb1.substring(0, sb1.length());
		String v2Real = sb2.substring(0, sb2.length());
		
		return v1Real.compareTo(v2Real);
	}

}
