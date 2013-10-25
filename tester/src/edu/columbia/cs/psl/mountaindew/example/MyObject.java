package edu.columbia.cs.psl.mountaindew.example;

import java.util.HashMap;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

@Metamorphic
public class MyObject {
	
	private String myString = "Test123";
	
	private Integer[] myArray;
	
	private int[] myStupidArray = new int[] {7, 8, 9};
	
	private HashMap dummyMap;
	
	private HashMap dummyMap2 = new HashMap();
	
	public MyObject(int i) {
		myArray = new Integer[]{1, 2, 3, 4, 5};
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
		mo.sumUp(5);
	}
}
