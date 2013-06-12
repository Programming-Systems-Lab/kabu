package edu.columbia.cs.psl.mountaindew.test;

import java.util.ArrayList;
import java.util.List;

import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Exclusive;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.InclusiveMax;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.InclusiveMin;

public class InclueExclueMain {
	
	public static void main(String args[]) {
		InclusiveMax inc = new InclusiveMax();
		
		int[] a = {1, 2, 6, 8, 10};
		int[] b = inc.apply(a);
		
		for (int i = 0 ; i < b.length; i++) {
			System.out.println("DEBUG InclusiveMax: " + b[i]);
		}
		
		List<Integer> aList = new ArrayList<Integer>();
		aList.add(6);
		aList.add(18);
		aList.add(0);
		
		List<Integer> bList = inc.apply(aList);
		
		for (Integer i: bList) {
			System.out.println("DEBUG InclusiveMax list: " + i);
		}
		
		InclusiveMin incMin = new InclusiveMin();
		
		int [] b2 = incMin.apply(a);
		
		for (int i = 0 ; i < b2.length; i++) {
			System.out.println("DEBUG InclusiveMin: " + b2[i]);
		}
		
		List<Integer> b2List = incMin.apply(aList);
		
		for (Integer i: b2List) {
			System.out.println("DEBUG InclusiveMin list: " + i);
		}
		
		Exclusive exc = new Exclusive();
		int[] c = exc.apply(a);
		
		for (int i = 0 ; i < c.length; i++) {
			System.out.println("DEBUG Exclusive: " + c[i]);
		}
		
		List<Integer> cList = exc.apply(aList);
		
		for (Integer i: cList) {
			System.out.println("DEBUG Exclusive list: " + i);
		}
	}

}
