package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mahout.math.Vector;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Shuffle;
import edu.columbia.cs.psl.mountaindew.absprop.PairwiseMetamorphicProperty;

public class Shufflable extends PairwiseMetamorphicProperty {

	@Override
	public String getName() {
		return "C:Shufflable";
	}
	
	private ContentEqualer ce = new ContentEqualer();


	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {

		//For array
		if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {
			int rt1Length = Array.getLength(returnValue1);
			int rt2Length = Array.getLength(returnValue2);
			
			if (rt1Length != rt2Length)
				return false;
			
			List rt1List = this.returnList(returnValue1);
			List rt2List = this.returnList(returnValue2);
			
			System.out.println("Shufflable array1: " + rt1List);
			System.out.println("Shufflable array2: " + rt2List);
			
			return this.sortAndCheck(rt1List, rt2List);
		} else if (Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass())) {
			List rt1List = this.returnList(returnValue1);
			List rt2List = this.returnList(returnValue2);
			
			if (rt1List.size() != rt2List.size())
				return false;
			
			System.out.println("Shufflable list1: " + rt1List);
			System.out.println("Shufflable list2: " + rt2List);
			
			return this.sortAndCheck(rt1List, rt2List);
		} else if (Map.class.isAssignableFrom(returnValue1.getClass()) && Map.class.isAssignableFrom(returnValue2.getClass())) {
			Map map1 = (Map)returnValue1;
			Map map2 = (Map)returnValue2;
			
			for (Object key: map1.keySet()) {
				Object tmpObj1 = map1.get(key);
				Object tmpObj2 = map2.get(key);
				
				if (tmpObj1.getClass().isArray() && tmpObj2.getClass().isArray()) {
					List tmpList1 = this.returnList(tmpObj1);
					List tmpList2 = this.returnList(tmpObj2);
					
					if (this.sortAndCheck(tmpList1, tmpList2) == false)
						return false;
				} else if (Collection.class.isAssignableFrom(tmpObj1.getClass()) && Collection.class.isAssignableFrom(tmpObj2.getClass())) {
					List tmpList1 = this.returnList(tmpObj1);
					List tmpList2 = this.returnList(tmpObj2);
					
					if (this.sortAndCheck(tmpList1, tmpList2) == false)
						return false;
				} else {
					return false;
				}
			}
			return true;
		} else if (String.class.isAssignableFrom(returnValue1.getClass()) && String.class.isAssignableFrom(returnValue2.getClass())) {
			String s1 = (String)returnValue1;
			String s2 = (String)returnValue2;
			
			List tmpList1 = this.returnList(s1.toCharArray());
			List tmpList2 = this.returnList(s2.toCharArray());
			
			System.out.println("Shufflable string: " + tmpList1);
			System.out.println("Shufflabel string: " + tmpList2);
			
			return this.sortAndCheck(tmpList1, tmpList2);
		}
		
		return false;
	}

	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2, int interestedVariable) {
		/*for(int i = 0;i<i1.params.length;i++)
			if(i!=interestedVariable && !i1.params[i].equals(i2.params[i]))
				return false;*/
		
		//If i1 is not i2's parent, no need to compare
		if (i2.getParent() != i1) {
			return false;
		}
		
		if (!i2.getBackend().equals(this.getName()))
			return false;
		else
			return true;
		
		/*if(o1.getClass().isArray() && o2.getClass().isArray())
		{
			if(Array.getLength(o1) != Array.getLength(o2))
				return false;
			HashSet<Double> o1h = new HashSet<Double>();
			HashSet<Double> o2h = new HashSet<Double>();
			for(int i = 0;i<Array.getLength(o1);i++)
			{
//				System.out.println("Test oriInput: " + (Number)Array.get(o1, i));
//				System.out.println("Test transInput: " + (Number)Array.get(o2, i));
				
				o1h.add(((Number)Array.get(o1, i)).doubleValue());
				o2h.add(((Number)Array.get(o2, i)).doubleValue());
			}
			return o1h.equals(o2h);
		}
		else if(Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass()))
		{
			if (((Collection)o1).size() != ((Collection)o2).size())
				return false;
			HashSet<Object> o1h = new HashSet<Object>();
			HashSet<Object> o2h = new HashSet<Object>();
			o1h.addAll((Collection) o1);
			o2h.addAll((Collection) o2);
			return o1h.equals(o2h);
		}
		else if(Number.class.isAssignableFrom(o1.getClass()) && Number.class.isAssignableFrom(o2.getClass())) {
//			System.out.println("Check o1 value: " + ((Number)o1).doubleValue());
//			System.out.println("Check o2 value: " + ((Number)o2).doubleValue());
			return (((Number)o1).doubleValue() == ((Number)o2).doubleValue());
		}
		return false;*/
	}
	
	private boolean sortAndCheck(List l1, List l2) {
		
		try {
			Collections.sort(l1);
			Collections.sort(l2);
		} catch (Exception ex) {
			System.err.println("Your list is not sortable. Remain original list");
		}
		
		System.out.println("Sorted l1: " + l1);
		System.out.println("Sorted l2: " + l2);
		
		return this.ce.checkEquivalence(l1, l2);
	}

	@Override
	protected int[] getInterestedVariableIndices() {
		ArrayList<Integer> rets = new ArrayList<Integer>();
		for(int i = 0;i<getMethod().getParameterTypes().length; i++)
		{
			if(getMethod().getParameterTypes()[i].isArray() ||
					Collection.class.isAssignableFrom(getMethod().getParameterTypes()[i]) ||
					String.class.isAssignableFrom(getMethod().getParameterTypes()[i]))
				rets.add(i);
		}
		
		if (rets.size() == 0)
			rets.add(0);
		
		int[] ret = new int[rets.size()];
		for(int i = 0;i<rets.size();i++)
			ret[i]=rets.get(i);
		return ret;
	}
	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		return new Shuffle();
	}

}
