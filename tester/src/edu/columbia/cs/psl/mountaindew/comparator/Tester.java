package edu.columbia.cs.psl.mountaindew.comparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.columbia.cs.psl.mountaindew.versionsorter.WekaVersionSorter;

public class Tester {
	
	public static void main(String[] args) {
		String v1 = "3-4-2";
		String v2 = "3-4-1";
		String v3 = "3-4-19";
		String v4 = "3-5-1";
		
		List<String> sList = new ArrayList<String>();
		sList.add(v1);
		sList.add(v2);
		sList.add(v3);
		sList.add(v4);
		
		Collections.sort(sList, new WekaVersionSorter());
		System.out.println(sList);
				
	}

}
