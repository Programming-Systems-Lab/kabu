package edu.columbia.cs.psl.mountaindew.example;

import java.util.HashMap;
import java.util.Map;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

@Metamorphic
public class MyObject {
	
	private static int __metamorphicCount = 0;
	
	private static HashMap __meta_static_map;
	
	private HashMap __meta_obj_map = new HashMap();
	
	private String myString = "Test123";
	
	private Integer[] myArray;
	
	private int[] myStupidArray = new int[] {7, 8, 9};
	
	private HashMap dummyMap;
	
	private HashMap dummyMap2 = new HashMap();
	
	public MyObject(int i) {
		myArray = new Integer[]{1, 2, 3, 4, 5};
		__meta_static_map = new HashMap();
	}
	
	public void pushObjMap() {
		HashMap __meta_local_map = new HashMap();
		int i = 0;
		__meta_local_map.put(0, i);
		int j = 1;
		__meta_local_map.put(1, j);
		String k = "Friday";
		__meta_local_map.put(2, k);
		this.__meta_obj_map.put("pushObjMap", __meta_local_map);
	}
	
	public void pushOriObjMap() {
		int i = 0;
		dummyMap2.put(0, i);
		int j = 1;
		dummyMap2.put(1, j);
		String k = "Friday";
		dummyMap2.put(2, k);
	}
	
	public static void pushStaticMap() {		
		int i = 0;
		__meta_static_map.put(0, i);
		int j = 1;
		__meta_static_map.put(1, j);
		String k = "Friday";
		__meta_static_map.put(2, k);
		
		System.out.println("My static map: " + __meta_static_map);
	}
	
	public void pushOriMap() {
		int i = 0;
		this.__meta_obj_map.put(0, i);
		int j = 1;
		this.__meta_obj_map.put(1, j);
		String k = "Friday";
		this.__meta_obj_map.put(2, k);
	}
	
	public String getMyString() {
		return myString;
	}
	
	public Integer[] getMyArray() {
		return myArray;
	}
	
	public void dummyPrint(Object obj) {
		System.out.println(obj.toString());
	}
	
	@Metamorphic
	public FakeInnerObj sumUp(int test) {
		String dummy1 = "Please get me once";
		String dummy2 = "Pleae get me twice";
		
		//this.dummyMap.put(1, dummy1);
		
		int sum = 0;
		for (int i = 0; i < myArray.length; i++) {
			sum += myArray[i];
		}
		
		this.myString = "Test 456";
		this.myArray[3] = 8;
		
		FakeInnerObj ret = new FakeInnerObj();
		ret.buildClassifier(2);
		
		return ret;
	}
	
	public int dummyReturn() {
		int i = 1;
		int j = 2;
		int k = 3;
		int l = 4;
		
		return this.myStupidArray[0];
	}
	
	public void stupidConversion() {
		int i = 3;
		//Integer iObj = new Integer(i);
		int j = 5;
		this.dummyMap.put(i, j);
	}
	
	public HashMap getDummyMap() {
		return this.dummyMap;
	}
	
	public int returnDummyInt() {
		return this.dummyInt();
	}
	
	public int dummyInt() {
		return 1;
	}
	
	public static void main (String args[]) {
		MyObject mo = new MyObject(5);
		//mo.sumUp(5);
		mo.pushStaticMap();
	}
}
