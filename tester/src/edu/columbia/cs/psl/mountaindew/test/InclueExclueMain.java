package edu.columbia.cs.psl.mountaindew.test;

import java.util.ArrayList;
import java.util.List;

import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.ExclusiveMax;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.ExclusiveMid;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.ExclusiveMin;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.InclusiveMax;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.InclusiveMid;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.InclusiveMin;

public class InclueExclueMain {
	
	public static void main(String args[]) {		
		int[] a = {1, 2, 6, 8, 10};
		/*int[] b = inc.apply(a);
		
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
		}*/
		
		/*InclusiveMax inc = new InclusiveMax();
		int[] maxTest = inc.apply(a);
		
		for (int i = 0 ; i < maxTest.length; i++) {
			System.out.println("DEBUG InclusiveMax: " + maxTest[i]);
		}*/
		
		/*InclusiveMid incMid = new InclusiveMid();
		int[] midTest = incMid.apply(a);
		
		for (int i = 0 ; i < midTest.length; i++) {
			System.out.println("DEBUG InclusiveMid: " + midTest[i]);
		}*/
		
		/*InclusiveMin inMin = new InclusiveMin();
		int[] minTest = inMin.apply(a);
		
		for (int i = 0; i < minTest.length; i++) {
			System.out.println("DEBUG InclusiveMin: " + minTest[i]);
		}*/
		
		/*ExclusiveMax exMax = new ExclusiveMax();
		int[] exMaxTest = exMax.apply(a);
		
		for (int i = 0; i < exMaxTest.length; i++) {
			System.out.println("DEBUG ExclusiveMax: " + exMaxTest[i]);
		}*/
		
		/*ExclusiveMid exMid = new ExclusiveMid();
		int[] exMidTest = exMid.apply(a);
		
		for (int i = 0; i < exMidTest.length; i++) {
			System.out.println("DEBUG ExclusiveMid: " + exMidTest[i]);
		}*/
		
		ExclusiveMin exMin = new ExclusiveMin();
		int[] exMinTest = exMin.apply(a);
		
		for (int i = 0; i < exMinTest.length; i++) {
			System.out.println("DEBUG ExclusiveMin: " + exMinTest[i]);
		}
		
	}

}
