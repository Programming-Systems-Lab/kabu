package edu.columbia.cs.psl.mountaindew.example;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.collections.bag.HashBag;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.LogState;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

public class CmnCollectionTest {
	
	public Object[] testToArray(List items) {
		HashBag hb = new HashBag();
		hb.addAll(items);
		
		Object[] ret = hb.toArray();
		return ret;
	}
	
	public static void main(String args[]) {
		List input = new ArrayList();
		input.add(1);
		input.add(3);
		input.add(1);
		
		CmnCollectionTest cct = new CmnCollectionTest();
		Object[] result = cct.testToArray(input);
		
		for (Object obj: result) {
			System.out.println(obj);
		}
	}

}
