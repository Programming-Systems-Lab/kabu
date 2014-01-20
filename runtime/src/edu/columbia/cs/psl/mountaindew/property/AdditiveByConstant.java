package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.AddNumericConstant;
import edu.columbia.cs.psl.mountaindew.absprop.PairwiseMetamorphicProperty;

public class AdditiveByConstant extends PairwiseMetamorphicProperty {

	@Override
	public String getName() {
		return "C:AdditiveByConstant";
	}

	boolean returnDoesntChange;
	
	private static int roundDigit = 5;
	
	private ContentEqualer ce = new ContentEqualer();
	
	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		
		System.out.println("Start comparison in additive: " + returnValue1 + " " + returnValue2);
		
		List rt1List = null;
		List rt2List = null;
		double fDiff = 0;
		try
		{
			if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {
				
				if (Array.getLength(returnValue1) != Array.getLength(returnValue2))
					return false;
				
				rt1List = this.returnList(returnValue1);
				rt2List = this.returnList(returnValue2);
				
				System.out.println("Additive check array1: " + rt1List);
				System.out.println("Additive check array2: " + rt2List);
				
				fDiff = this.getFirstDiff(rt1List, rt2List);
				if (fDiff == Double.MAX_VALUE || fDiff == 0)
					return false;
				
				List mrt1List = (List)this.addObject(rt1List, 0.0);
				List mrt2List = (List)this.addObject(rt2List, fDiff);
				
				System.out.println("Additive check mrt1Array: " + rt1List);
				System.out.println("Additive check mrt2Array: " + rt2List);
				
				return this.ce.checkEquivalence(mrt1List, mrt2List);
			} else if (Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass())) {
				rt1List = this.returnList(returnValue1);
				rt2List = this.returnList(returnValue2);
				
				System.out.println("Additive check list1: " + rt1List);
				System.out.println("Additive check list2: " + rt2List);
				
				fDiff = this.getFirstDiff(rt1List, rt2List);
				if (fDiff == Double.MAX_VALUE || fDiff == 0)
					return false;
				
				List mrt1List = (List)this.addObject(rt1List, 0.0);
				List mrt2List = (List)this.addObject(rt2List, fDiff);
				
				System.out.println("Additive check mrt1List: " + rt1List);
				System.out.println("Additive check mrt2List: " + rt2List);
				
				return this.ce.checkEquivalence(mrt1List, mrt2List);
			} else if (Map.class.isAssignableFrom(returnValue1.getClass()) && Map.class.isAssignableFrom(returnValue2.getClass())) {
				Map map1 = (Map)returnValue1;
				Map map2 = (Map)returnValue2;
				
				if (map1.size() != map2.size())
					return false;
				
				System.out.println("Additive check map1: " + map1);
				System.out.println("Additive check map2: " + map2);
				
				fDiff = this.getFirstDiff(map1, map2);
				if (fDiff == Double.MAX_VALUE || fDiff == 0)
					return false;
				
				System.out.println("Check first diff: " + fDiff);
				
				for (Object tmp: map1.keySet()) {
					Object tmpObj1 = map1.get(tmp);
					Object tmpObj2 = map2.get(tmp);
					
					if (tmpObj1 == null || tmpObj2 == null)
						return false;
					
					System.out.println("Check tmpObj1: " + tmpObj1);
					
					if (Number.class.isAssignableFrom(tmpObj1.getClass())) {
						Number n1 = (Number)tmpObj1;
						Number n2 = (Number)tmpObj2;
						double tmpDiff = (n1.doubleValue() - n2.doubleValue());
						
						if (this.roundDouble(tmpDiff, roundDigit) != this.roundDouble(fDiff, roundDigit))
							return false;
					} else if (tmpObj1.getClass().isArray() || Collection.class.isAssignableFrom(tmpObj1.getClass())) {
						List tmpList1 = this.returnList(tmpObj1);
						List tmpList2 = this.returnList(tmpObj2);
						
						System.out.println("Tmp list1: " + tmpList1);
						System.out.println("Tmp list2: " + tmpList2);
						
						List mList1 = (List)this.addObject(tmpList1, 0.0);
						List mList2 = (List)this.addObject(tmpList2, fDiff);
						
						if (this.ce.checkEquivalence(mList1, mList2) == false)
							return false;
					} else {
						return false;
					}	
				}
				return true;
			} else if (Number.class.isAssignableFrom(p1.getClass()) && Number.class.isAssignableFrom(p2.getClass()) 
					&& Number.class.isAssignableFrom(returnValue1.getClass()) && Number.class.isAssignableFrom(returnValue2.getClass())) {
				double diff1 = this.roundDouble(getDifference(p1, p2), roundDigit);
				double diff2 = this.roundDouble(getDifference(returnValue1, returnValue2), roundDigit);
				
				System.out.println("Additive diff1: " + diff1);
				System.out.println("Additive diff2: " + diff2);
				
				if (diff1 == 0)
					return false;
				else
					return diff1 == diff2;
			} else if (String.class.isAssignableFrom(p1.getClass()) && String.class.isAssignableFrom(p2.getClass()) 
					&&String.class.isAssignableFrom(returnValue1.getClass()) && String.class.isAssignableFrom(returnValue2.getClass())) {
				int diff1 = (int)getDifference(p1, p2);
				int diff2 = (int)getDifference(returnValue1, returnValue2);
				
				System.out.println("Additive string diff1: " + diff1);
				System.out.println("Additive string diff2: " + diff2);
				
				if (diff1 == 0)
					return false;
				else
					return diff1 == diff2;
			}  else {
				return false;
			}
			//return getDifference(p1, p2) == getDifference(returnValue1, returnValue2);
		} catch(Exception ex) {
			System.err.println(this.getName() + ": Unable to compare." + ex);
			return false;
		}
	}
	
	private double getFirstDiff(Object o1, Object o2) {
		try {
			if (Number.class.isAssignableFrom(o1.getClass()) && Number.class.isAssignableFrom(o2.getClass())) {
				return getDifference(o1, o2);
			} else if (String.class.isAssignableFrom(o1.getClass()) && String.class.isAssignableFrom(o2.getClass())) {
				return getDifference(o1, o2);
			} else if (o1.getClass().isArray() && o2.getClass().isArray()) {
				
				for (int i = 0; i < Array.getLength(o1); i++) {
					double ret = getFirstDiff(Array.get(o1, i), Array.get(o2, i));
					
					if (ret == Double.MAX_VALUE)
						continue;
					
					return ret;
				}
			} else if (Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass())) {
				List o1List = this.returnList(o1);
				List o2List = this.returnList(o2);
				
				for (int i = 0; i < o1List.size(); i++) {
					double ret = getFirstDiff(o1List.get(i), o2List.get(i));
					
					if (ret == Double.MAX_VALUE)
						continue;
					
					return ret;
				}
			} else if (Map.class.isAssignableFrom(o1.getClass()) && Map.class.isAssignableFrom(o2.getClass())) {
				Map map1 = (Map)o1;
				Map map2 = (Map)o2;
				
				for (Object key: map1.keySet()) {
					Object obj1 = map1.get(key);
					Object obj2 = map2.get(key);
					
					double ret = getFirstDiff(obj1, obj2);
					if (ret == Double.MAX_VALUE)
						continue;
					
					return ret;
				}
			} else {
				double ret = getDifference(o1, o2);
				return ret;
			}
		} catch (Exception ex) {
			throw ex;
		}
		return Double.MAX_VALUE;
	}
	
	private Object addObject(Object obj, double diff) {
		if (Number.class.isAssignableFrom(obj.getClass())) {
			return ((Number)obj).doubleValue() + diff;
		} else if (String.class.isAssignableFrom(obj.getClass())) {
			if (diff == 0)
				return obj;
			else {
				String objString = (String)obj;
				char c = (char)(-1 * ((int)diff));
				int last = objString.lastIndexOf(c);
				
				if (last == -1)
					return null;
				
				char[] oriChars = objString.toCharArray();
				char[] newChars = new char[oriChars.length - 1];
				int count = 0;
				for (int i = 0; i < oriChars.length; i++) {
					if (i == last)
						continue;
					else {
						newChars[count++] = oriChars[i];
					}
				}
				
				return new String(newChars);
			}
		} else if (obj.getClass().isArray()) {
			int objLength = Array.getLength(obj);
			
			for (int i = 0; i < objLength; i++) {
				Array.set(obj, i, this.addObject(Array.get(obj, i), diff));
			}
			return obj;
		} else if (Collection.class.isAssignableFrom(obj.getClass())) {
			List objList = this.returnList(obj);
			List retList = new ArrayList();
			for (int i = 0; i < objList.size(); i++) {
				retList.add(this.addObject(objList.get(i), diff));
			}
			return retList;
		} else if (Map.class.isAssignableFrom(obj.getClass())) {
			Map map = (Map)obj;
			Map retMap = new HashMap();
			
			for (Object key: map.keySet()) {
				retMap.put(key, this.addObject(map.get(key), diff));
			}
			return retMap;
		}
		
		return null;
	}

	private double getDifference(Object o1, Object o2) throws IllegalArgumentException
	{
		if(!o1.getClass().equals(o2.getClass()))
			throw new IllegalArgumentException("Both parameters must be of the same type");
		if(o1.getClass().equals(Integer.class) || o1.getClass().equals(Integer.TYPE))
			return ((Integer) o1) - ((Integer) o2);
		else if(o1.getClass().equals(Short.class) || o1.getClass().equals(Short.TYPE))
			return ((Short) o1) - ((Short) o2);
		else if(o1.getClass().equals(Long.class) || o1.getClass().equals(Long.TYPE))
			return ((Long) o1) - ((Long) o2);
		else if(o1.getClass().equals(Double.class) || o1.getClass().equals(Double.TYPE))
			return ((Double) o1) - ((Double) o2);
		else if (String.class.isAssignableFrom(o1.getClass()) || o1.getClass().equals(String.class)) {
			int ret = this.sumUpChars((String)o1) - this.sumUpChars((String)o2);
			return ret;
		}
		throw new IllegalArgumentException("Non numeric types");
	}
	
	private int sumUpChars(String str) {
		char[] chars = str.toCharArray();
		int ret = 0;
		for (int i = 0; i < chars.length; i++) {
			ret += ((int)chars[i]);
		}
		
		return ret;
	}
	
	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2,
			int interestedVariable) {
		/*for(int i = 0;i<i1.params.length;i++)
			if(i!=interestedVariable && !i1.params[i].equals(i2.params[i]))
				return false;*/
		
		//If i1 is not i2's parent, no need to compare
		if (i2.getParent() != i1) {			
			return false;
		}

		//If i2's checker is not this one, return false
		if (!i2.getBackend().equals(this.getName())) {
			return false;
		} else {
			return true;
		}
		
		/*System.out.println("Add i1: " + i1.getFrontend() + " " + i1.getBackend());
		System.out.println("Add i2: " + i2.getFrontend() + " " + i2.getBackend());*/
				
		/*double o1Val, o2Val;
		if (o1.getClass().isArray() && o2.getClass().isArray()) {
			
			double o1Checker = ((Number)Array.get(o1, 0)).doubleValue();
			double o2Checker = ((Number)Array.get(o2, 0)).doubleValue();
			double checkDiff = getDifference(o1Checker, o2Checker);
						
			for (int i = 0; i < Array.getLength(o1); i++) {
				o1Val = ((Number)Array.get(o1, i)).doubleValue();
				o2Val = ((Number)Array.get(o2, i)).doubleValue();
				
				//System.out.println("Add ori trans input: " + o1Val + " " + o2Val);
				
				if (getDifference(o1Val, o2Val) != checkDiff)
					return false;
			}
			return true;
		} else if (Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass())) {
			List o1List = this.returnList(o1);
			List o2List = this.returnList(o2);
			
			double o1Checker = ((Number)o1List.get(0)).doubleValue();
			double o2Checker = ((Number)o2List.get(0)).doubleValue();
			double checkDiff = getDifference(o1Checker, o2Checker);
			
			for (int i = 0; i < o1List.size(); i++) {
				o1Val = ((Number)o1List.get(i)).doubleValue();
				o2Val = ((Number)o2List.get(i)).doubleValue();
				
				//System.out.println("Add ori trans input: " + o1Val + " " + o2Val);
				
				if (getDifference(o1Val, o2Val) != checkDiff)
					return false;
			}
			return true;
		}
		
		return true;*/
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		return new AddNumericConstant(); //TODO how do we make this parameterized
	}

}
