package edu.columbia.cs.psl.mountaindew.versionsorter;

import java.util.Comparator;
import java.util.Stack;

public class WekaVersionSorter implements Comparator<String>{
	public int compare(String v1, String v2) {
		StringBuilder v1Builder = new StringBuilder();
		StringBuilder v2Builder = new StringBuilder();
		
		int i = v1.length() - 1;
		while(i >= 0) {
			if (!Character.isDigit(v1.charAt(i)))
				break;
			
			v1Builder.append(v1.charAt(i--));
		}
		
		int j = v2.length() - 1;
		while(j >= 0) {
			if (!Character.isDigit(v2.charAt(j)))
				break;
			
			v2Builder.append(v2.charAt(j--));
		}
		
		if (v1Builder.length() < v2Builder.length())
			v1Builder.append('0');
		else if (v2Builder.length() < v1Builder.length())
			v2Builder.append('0');
		
		String v1New = v1.substring(0, i+1) + v1Builder.reverse().toString();
		String v2New = v2.substring(0, j+1) + v2Builder.reverse().toString();
		
		return v1New.compareTo(v2New);
		
	}
}
