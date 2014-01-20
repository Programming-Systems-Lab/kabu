package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Instances;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.MultiplyByNumericConstant;
import edu.columbia.cs.psl.mountaindew.absprop.PairwiseMetamorphicProperty;

public class MultiplicativeByConstant extends PairwiseMetamorphicProperty {
	
	protected static int roundDigit = 5;
	
	protected ContentEqualer ce = new ContentEqualer();
	@Override
	public String getName() {
		return "C:MultiplicativeByConstant";
	}

	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		List rt1List = null;
		List rt2List = null;
		double fDivisor = 0;
		try {
			if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {
				//Because propertiesApply now not check length, check them here
				if (Array.getLength(returnValue1) != Array.getLength(returnValue2))
					return false;
				
				rt1List = this.returnList(returnValue1);
				rt2List = this.returnList(returnValue2);
				
				System.out.println("Multiplicity check array1: " + rt1List);
				System.out.println("Multiplicity check array2: " + rt2List);
				
				fDivisor = this.getFirstDivisor(rt1List, rt2List);
				if (fDivisor == Double.MAX_VALUE || fDivisor == 1 || fDivisor == -1)
					return false;
				
				List mrt1List = (List)this.multiplyObject(rt1List, 1.0);
				List mrt2List = (List)this.multiplyObject(rt2List, fDivisor);
				
				System.out.println("mrt1Array: " + mrt1List);
				System.out.println("mrt2Array: " + mrt2List);
				
				return this.ce.checkEquivalence(rt1List, mrt2List);
			} else if (Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass())) {
				rt1List = this.returnList(returnValue1);
				rt2List = this.returnList(returnValue2);
				
				System.out.println("Multiplicity check list1: " + rt1List);
				System.out.println("Multiplicity check list2: " + rt2List);
				
				fDivisor = this.getFirstDivisor(rt1List, rt2List);
				if (fDivisor == Double.MAX_VALUE || fDivisor == 1 || fDivisor == -1)
					return false;
				
				List mrt1List = (List)this.multiplyObject(rt1List, 1.0);
				List mrt2List = (List)this.multiplyObject(rt2List, fDivisor);
				
				System.out.println("fDivisor: " + fDivisor);
				System.out.println("mrt1List: " + mrt1List);
				System.out.println("mrt2List: " + mrt2List);
				
				return this.ce.checkEquivalence(rt1List, mrt2List);
			} else if (Map.class.isAssignableFrom(returnValue1.getClass()) && Map.class.isAssignableFrom(returnValue2.getClass())) {
				Map map1 = (Map)returnValue1;
				Map map2 = (Map)returnValue2;
				
				if (map1.size() != map2.size())
					return false;
				
				System.out.println("Multiplicity check map1: " + map1);
				System.out.println("Multiplicity check map2: " + map2);
				
				fDivisor = this.getFirstDivisor(map1, map2);
				if (fDivisor == Double.MAX_VALUE || fDivisor == 1 || fDivisor == -1)
					return false;
				
				for (Object tmp: map1.keySet()) {
					Object tmpObj1 = map1.get(tmp);
					Object tmpObj2 = map2.get(tmp);
					
					if (tmpObj1 == null || tmpObj2 == null)
						return false;
					
					if (Number.class.isAssignableFrom(tmpObj1.getClass())) {
						double n1 = ((Number)tmpObj1).doubleValue();
						double n2 = ((Number)tmpObj2).doubleValue();
						double tmpDiv = this.getDivisor(n1, n2);
						
						if (this.roundDouble(tmpDiv, roundDigit) != this.roundDouble(fDivisor, roundDigit))
							return false;
					} else if (tmpObj1.getClass().isArray() || Collections.class.isAssignableFrom(tmpObj1.getClass())) {
						List tmpList1 = this.returnList(tmpObj1);
						List tmpList2 = this.returnList(tmpObj2);
						
						List mList1 = (List)this.multiplyObject(tmpList1, 1.0);
						List mList2 = (List)this.multiplyObject(tmpList2, fDivisor);
						
						if (this.ce.checkEquivalence(mList1, mList2) == false)
							return false;
					} else {
						return false;
					}	
				}
				return true;
			} else if (Number.class.isAssignableFrom(p1.getClass()) && Number.class.isAssignableFrom(p2.getClass()) 
					&& Number.class.isAssignableFrom(returnValue1.getClass()) && Number.class.isAssignableFrom(returnValue2.getClass())) {
				double div1 = this.roundDouble(getDivisor(p1, p2), roundDigit);
				double div2 = this.roundDouble(getDivisor(returnValue1, returnValue2), roundDigit);
				
				System.out.println("Check div1: " + div1);
				System.out.println("Check div2: " + div2);
				
				//Leave 1 for equaler and -1 or negater to check
				if (div1 == 1 || div1 == -1)
					return false;
				else
					return div1 == div2;
			} else if (String.class.isAssignableFrom(returnValue1.getClass()) && String.class.isAssignableFrom(returnValue2.getClass())) {
				double div = getDivisor(returnValue1, returnValue2);
				
				System.out.println("Check p1: " + p1);
				System.out.println("Check p2: " + p2);
				if (div == Double.MAX_VALUE || div == 1)
					return false;
				
				System.out.println("Check return value1: " + returnValue1);
				System.out.println("Check return value2: " + returnValue2);
				String rv1 = (String)this.multiplyObject(returnValue1, 1.0);
				String rv2 = (String)this.multiplyObject(returnValue2, div);
				
				return rv1.equals(rv2);
			} else {
				return false;
			}
			//return getDivisor(p1, p2) == getDivisor(returnValue1, returnValue2);
		}
		catch(Exception ex)
		{
			System.err.println(this.getName() + ": Unable to compare. " + ex);
			return false;
		}
	}

	private double getFirstDivisor(Object o1, Object o2) {
		try {
			if (Number.class.isAssignableFrom(o1.getClass()) && Number.class.isAssignableFrom(o2.getClass())) {
				return getDivisor(o1, o2);
			} else if (String.class.isAssignableFrom(o1.getClass()) && String.class.isAssignableFrom(o2.getClass())) {
				return getDivisor(o1, o2);
			} else if (o1.getClass().isArray() && o2.getClass().isArray()) {
				
				for (int i = 0; i < Array.getLength(o1); i++) {
					double ret = getFirstDivisor(Array.get(o1, i), Array.get(o2, i));
					
					if (ret == Double.MAX_VALUE)
						continue;
					
					return ret;
				}
			} else if (Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass())) {
				List o1List = this.returnList(o1);
				List o2List = this.returnList(o2);
				
				for (int i = 0; i < o1List.size(); i++) {
					double ret = getFirstDivisor(o1List.get(i), o2List.get(i));
					
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
					
					double ret = getFirstDivisor(obj1, obj2);
					if (ret == Double.MAX_VALUE)
						continue;
					
					return ret;
				}
			} else {
				double ret = getDivisor(o1, o2);
				return ret;
			}
		} catch (Exception ex) {
			throw ex;
		}
		return Double.MAX_VALUE;
	}
	
	protected Object multiplyObject(Object obj, double divisor) {
		if (Number.class.isAssignableFrom(obj.getClass())) {
			return ((Number)obj).doubleValue() * divisor;
		} else if (String.class.isAssignableFrom(obj.getClass())) {
			String oString = (String)obj;
			Double newLength = oString.length() * divisor;
			System.out.println("Check oString: " + oString);
			
			if (newLength > oString.length() || newLength < 0)
				return null;
			else
				return oString.substring(0, newLength.intValue());
		} else if (obj.getClass().isArray()) {
			int objLength = Array.getLength(obj);
			
			for (int i = 0; i < objLength; i++) {
				Array.set(obj, i, this.multiplyObject(Array.get(obj, i), divisor));
			}
			return obj;
		} else if (Collection.class.isAssignableFrom(obj.getClass())) {
			List objList = this.returnList(obj);
			List retList = new ArrayList();
			for (int i = 0; i < objList.size(); i++) {
				retList.add(this.multiplyObject(objList.get(i), divisor));
			}
			return retList;
		} else if (Map.class.isAssignableFrom(obj.getClass())) {
			Map map = (Map)obj;
			Map retMap = new HashMap();
			
			for (Object key: map.keySet()) {
				retMap.put(key, this.multiplyObject(map.get(key), divisor));
			}
			return retMap;
		}
		
		return null;
	}
	
	private boolean checkListDivisor(List rt1List, List rt2List) {
		if (rt1List.size() != rt2List.size())
			return false;
		
		for (int i = 0; i < rt1List.size() - 1; i++) {
			Object rt1Tmp1 = rt1List.get(i);
			Object rt1Tmp2 = rt1List.get(i + 1);
			Object rt2Tmp1 = rt2List.get(i);
			Object rt2Tmp2 = rt2List.get(i + 1);
			
			if (checkDivisor(rt1Tmp1, rt1Tmp2, rt2Tmp1, rt2Tmp2) == false)
				return false;
		}
		
		return true;
	}
	
	private boolean checkDivisor(Object obj11, Object obj12, Object obj21, Object obj22) {
		if (Number.class.isAssignableFrom(obj11.getClass())) {
			//Round it
			double div1 = this.roundDouble(getDivisor(obj11, obj12), roundDigit);
			double div2 = this.roundDouble(getDivisor(obj21, obj22), roundDigit);
			return div1 == div2;
		} else if (Collection.class.isAssignableFrom(obj11.getClass())) {
			List tmpList11 = (List)obj11;
			List tmpList12 = (List)obj12;
			List tmpList21 = (List)obj21;
			List tmpList22 = (List)obj22;
			
			if (tmpList11.size() != tmpList21.size())
				return false;
			
			if (tmpList12.size() != tmpList22.size())
				return false;
			
			for (int i = 0; i < tmpList11.size(); i++) {
				if (checkDivisor(tmpList11.get(i), tmpList12.get(i), 
						tmpList21.get(i), tmpList22.get(i)) == false)
					return false;
			}
			
			return true;
		}
		
		return false;
	}

	private double getDivisor(Object o1, Object o2) throws IllegalArgumentException
	{
		if(!o1.getClass().equals(o2.getClass()))
			throw new IllegalArgumentException("Both parameters must be of the same type");
		if(o1.getClass().equals(Integer.class) || o1.getClass().equals(Integer.TYPE))
		{
			if((Integer) o2 != 0) {
				double rawResult = ((Integer) o1).doubleValue() / ((Integer) o2).doubleValue();
				return this.roundDouble(rawResult, 5);
			} else {
				return Double.MAX_VALUE;
			}
		}
		else if(o1.getClass().equals(Short.class) || o1.getClass().equals(Short.TYPE))
		{
			if((Short) o2 != 0) {
				double rawResult = ((Short) o1).doubleValue() / ((Short) o2).doubleValue();
				return this.roundDouble(rawResult, 5);
			} else {
				return Double.MAX_VALUE;
			}
		}
		else if(o1.getClass().equals(Long.class) || o1.getClass().equals(Long.TYPE))
		{
			if((Long) o2 != 0) {
				double rawResult = ((Long) o1).doubleValue() / ((Long) o2).doubleValue();
				return this.roundDouble(rawResult, 5);
			} else {
				return Double.MAX_VALUE;
			}
		}
		else if(o1.getClass().equals(Double.class) || o1.getClass().equals(Double.TYPE))
		{
			if((Double) o2 != 0) {
				double rawResult = ((Double)o1) / ((Double) o2);
				return this.roundDouble(rawResult, 5);
			} else {
				return Double.MAX_VALUE;
			}
		}
		else if(o1.getClass().equals(String.class) || String.class.isAssignableFrom(o1.getClass())) {
			double o1Length = (double)((String)o1).length();
			int o2Length = ((String)o2).length();
			
			System.out.println("Check o1 o2 length: " + o1Length + " " + o2Length);
			double ret = o1Length/o2Length;
			return ret;
		}
		throw new IllegalArgumentException("Non numeric types");
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
		
		//System.out.println("Mul i1: " + i1.getFrontend() + " " + i1.getBackend());
		//System.out.println("Mul i2: " + i2.getFrontend() + " " + i2.getBackend());
				
		/*double o1Val, o2Val;
		if (o1.getClass().isArray() && o2.getClass().isArray()) {
			double o1Checker = ((Number)Array.get(o1, 0)).doubleValue();
			double o2Checker = ((Number)Array.get(o2, 0)).doubleValue();
			double checkDiv = getDivisor(o1Checker, o2Checker);

			for (int i = 0; i < Array.getLength(o1); i++) {
				o1Val = ((Number)Array.get(o1, i)).doubleValue();
				o2Val = ((Number)Array.get(o2, i)).doubleValue();
				
				//System.out.println("Mul ori trans input: " + o1Val + " " + o2Val);
				
				if (getDivisor(o1Val, o2Val) != checkDiv)
					return false;
			}
			return true;
		} else if (Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass())) {
			List o1List = this.returnList(o1);
			List o2List = this.returnList(o2);
			
			double o1Checker = ((Number)o1List.get(0)).doubleValue();
			double o2Checker = ((Number)o2List.get(0)).doubleValue();
			double checkDiv = getDivisor(o1Checker, o2Checker);
			
			for (int i = 0; i < o1List.size(); i++) {
				o1Val = ((Number)o1List.get(i)).doubleValue();
				o2Val = ((Number)o2List.get(i)).doubleValue();
				
				//System.out.println("Mul ori trans input: " + o1Val + " " + o2Val);
				
				if (getDivisor(o1Val, o2Val) != checkDiv)
					return false;
			}
			return true;
		}
		
		return true;*/
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		return new MultiplyByNumericConstant(); //TODO
	}
}
