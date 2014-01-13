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
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Negate;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Reverse;
import edu.columbia.cs.psl.mountaindew.absprop.PairwiseMetamorphicProperty;

public class Invertable extends PairwiseMetamorphicProperty{
	
	private ContentEqualer ce = new ContentEqualer();
	
	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		// TODO Auto-generated method stub
		
		if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {			
			int rt1Length = Array.getLength(returnValue1);
			int rt2Length = Array.getLength(returnValue2);
			
			if (rt1Length != rt2Length)
				return false;
			
			List rt1List = this.returnList(returnValue1);
			List rt2List = this.returnList(returnValue2);
			
			System.out.println("Invertable check array1: " + rt1List);
			System.out.println("Invertable check array2: " + rt2List);
			
			//Leave this case for identity checker
			if (rt1List.equals(rt2List))
				return false;
			
			List reverseList = (List)this.reverseObject(rt2List);
			
			return this.ce.checkEquivalence(rt1List, reverseList);
		} else if (Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass())) {
			List o1List = this.returnList(returnValue1);
			List o2List = this.returnList(returnValue2);
			
			if (o1List.size() != o2List.size())
				return false;
						
			System.out.println("Invertable check list1: " + o1List);
			System.out.println("Invertable check list2: " + o2List);
			
			if (o1List.equals(o2List))
				return false;
			
			List reverseList = (List)this.reverseObject(o2List);
			return this.ce.checkEquivalence(o1List, reverseList);
		} else if (Map.class.isAssignableFrom(returnValue1.getClass()) && Map.class.isAssignableFrom(returnValue2.getClass())) {
			Map o1Map = (Map)returnValue1;
			Map o2Map = (Map)returnValue2;
			
			if (o1Map.size() != o2Map.size())
				return false;
			
			if (o1Map.equals(o2Map))
				return false;
			
			Map r2Map = (Map)this.reverseObject(o2Map);
			
			return this.ce.checkEquivalence(o1Map, r2Map);
		} else if (String.class.isAssignableFrom(returnValue1.getClass()) && String.class.isAssignableFrom(returnValue2.getClass())) {
			String s1 = (String)returnValue1;
			String s2 = (String)returnValue2;
			
			if (s1.length() != s2.length())
				return false;
			
			if (s1.equals(s2))
				return false;
			
			String rs2 = (String)this.reverseObject(s2);
			
			return this.ce.checkEquivalence(s1, rs2);
		}
		
		return false;
	}

	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2,
			int interestedVariable) {
		// TODO Auto-generated method stub
		/*for (int i = 0 ; i < i2.params.length; i++) {
			if (i != interestedVariable && i1.params[i] != i2.params[i]) {
				return false;
			}
		}*/
		
		//If i1 is not i2's parent, no need to compare
		if (i2.getParent() != i1) {
			return false;
		}
		
		if (!i2.getBackend().equals(this.getName()))
			return false;
		else
			return true;
		
	}

	@Override
	protected int[] getInterestedVariableIndices() {
		// TODO Auto-generated method stub
		ArrayList<Integer> rets = new ArrayList<Integer>();
		for(int i = 0;i<getMethod().getParameterTypes().length; i++)
		{
			if(getMethod().getParameterTypes()[i].isArray() || 
					Collection.class.isAssignableFrom(getMethod().getParameterTypes()[i]) ||
					String.class.isAssignableFrom(getMethod().getParameterTypes()[i])) {
				rets.add(i);
			}
		}
		
		//If no input type matches, target on first param
		if (rets.size() == 0) {
			rets.add(0);
		}
		
		int[] ret = new int[rets.size()];
		for(int i = 0;i<rets.size();i++)
			ret[i]=rets.get(i);
		return ret;
	}
	
	private Object reverseObject(Object obj) {
		if (obj.getClass().isArray()) {
			List ret = new ArrayList();
			int objLength = Array.getLength(obj);
			
			for (int i = objLength - 1; i >= 0; i--) {
				ret.add(Array.get(obj, i));
			}
			return ret;
		} else if (Collection.class.isAssignableFrom(obj.getClass())) {
			List ret = new ArrayList();
			List objList = new ArrayList((Collection)obj);
			
			for (int i = objList.size() - 1; i >=0; i--) {
				ret.add(objList.get(i));
			}
			return ret;
		} else if (Map.class.isAssignableFrom(obj.getClass())) {
			Map objMap = (Map)obj;
			Map retMap = new HashMap();
			
			for (Object key: objMap.keySet()) {
				Object val = objMap.get(key);
				retMap.put(key, this.reverseObject(val));
			}
			return retMap;
		} else if (String.class.isAssignableFrom(obj.getClass())) {
			StringBuilder sb = new StringBuilder();
			sb.append((String)obj);
			return sb.reverse().toString();
		}
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "C:Invertable";
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return new Reverse();
	}

}
