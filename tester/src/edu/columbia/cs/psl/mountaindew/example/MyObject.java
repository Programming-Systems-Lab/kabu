package edu.columbia.cs.psl.mountaindew.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

@Metamorphic
public class MyObject {
	
	private static int __metamorphicCount = 0;
	
	private static HashMap __meta_static_map;
	
	private static HashMap __meta_static_record = new HashMap(); 
	
	private HashMap __meta_obj_map = new HashMap();
	
	private String myString = "Test123";
	
	private Integer[] myArray;
	
	private int[] myStupidArray = new int[] {7, 8, 9};
	
	private HashMap dummyMap;
	
	private HashMap dummyMap2 = new HashMap();
	
	static {
		System.out.println("Static block 1");
	}
	
	static {
		System.out.println("Static block 2");
	}
	
	public MyObject(int i) {
		myArray = new Integer[]{1, 2, 3, 4, 5};
		__meta_static_map = new HashMap();
	}
	
	public void pushObjMap() {
		HashMap __meta_local_map = new HashMap();
		this.__meta_obj_map.put("pushObjMap", __meta_local_map);
		int i = 0;
		__meta_local_map.put(0, i);
		int j = 1;
		__meta_local_map.put(1, j);
		String k = "Friday";
		__meta_local_map.put(2, k);
	}
	
	public void pushOriObjMap() {
		int i = 0;
		dummyMap2.put(0, i);
		int j = 1;
		dummyMap2.put(1, j);
		String k = "Friday";
		dummyMap2.put(2, k);
	}
	
	public static String getName() {
		return "Test123";
	}
	
	public static void pushStaticMap(int test) {
		HashMap localMap = new HashMap();
		long threadId = Thread.currentThread().getId();
		__meta_static_record.put(getName() + "_" + threadId, localMap);
		int i = 0;
		localMap.put(0, i);
		int j = 1;
		localMap.put(1, j);
		String k = "Friday";
		localMap.put(2, k);
		localMap.put(3, test);
		
		System.out.println("My meta static record: " + __meta_static_record);
		System.out.println("My current thread id: " + Thread.currentThread().getId());
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
	
	public static void main (String args[]) throws InterruptedException {
		MyObject mo = new MyObject(5);
		//mo.sumUp(5);
		System.out.println("In main check thread id: " + Thread.currentThread().getId());
		
		Thread t = new Thread() {
			public void run() {
				System.out.println("Check thread id in thread: " + Thread.currentThread().getId());
				MyObject.pushStaticMap(10);
			}
		};
		t.start();
		t.join();
		
		/*Thread t2 = new Thread() {
			public void run() {
				System.out.println("Check thread id in thread: " + Thread.currentThread().getId());
				MyObject.pushStaticMap();
			}
		};
		t2.start();*/
		mo.pushStaticMap(-1);
	}
}
