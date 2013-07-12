package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.mahout.math.AbstractVector;
import org.apache.mahout.math.Vector;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Negate;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Reverse;
import edu.columbia.cs.psl.mountaindew.absprop.PairwiseMetamorphicProperty;
import edu.columbia.cs.psl.mountaindew.util.VectorSorter;

public class Negatable extends PairwiseMetamorphicProperty{
	
	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		// TODO Auto-generated method stub
		
		if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {
			int rt1Length = Array.getLength(returnValue1);
			int rt2Length = Array.getLength(returnValue2);
			
			if (rt1Length != rt2Length)
				return false;
			
			//Class checking
			Class<?> arrayElementClass = returnValue1.getClass().getComponentType();
			
			if (Number.class.isAssignableFrom(arrayElementClass)) {
				double[] rt1 = new double[rt1Length];
				double[] rt2 = new double[rt2Length];
				
				for (int i = 0 ; i < rt1Length; i++) {
					rt1[i] = -1 * ((Number)Array.get(returnValue1, i)).doubleValue();
					rt2[i] = ((Number)Array.get(returnValue2, i)).doubleValue();
				}
				
				Arrays.sort(rt1);
				Arrays.sort(rt2);
				
				return Arrays.equals(rt1, rt2);
			} else if (Vector.class.isAssignableFrom(arrayElementClass)) {
				VectorSorter vs = new VectorSorter();
				
				Vector[] rt1 = new AbstractVector[rt1Length];
				Vector[] rt2 = new AbstractVector[rt2Length];
				
				for (int i = 0 ; i < rt1Length; i++) {
					rt1[i] = ((Vector)Array.get(returnValue1, i)).times(-1);
					rt2[i] = (Vector)Array.get(returnValue2, i);
				}
				
				Arrays.sort(rt1, vs);
				Arrays.sort(rt2, vs);
				
				return Arrays.equals(rt1, rt2);
			}			
		} else if (Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass())) {
			VectorSorter vs = new VectorSorter();
			
			List o1List = new ArrayList();
			List o2List = new ArrayList((Collection)returnValue2);
			
			Class<?> collectionElementClass = ((Collection)returnValue1).iterator().next().getClass();
			
			if (Number.class.isAssignableFrom(collectionElementClass)) {
				Iterator rt1IT = ((Collection)returnValue1).iterator();
				while(rt1IT.hasNext()) {
					o1List.add(((Number)rt1IT.next()).doubleValue() * -1);
				}
				
				if (o1List.size() != o2List.size())
					return false;
							
				Collections.sort(o1List);
				Collections.sort(o2List);
							
				/*for (int i = 0; i < o1List.size(); i++) {
					if (((Number)o1List.get(i)).doubleValue() != ((Number)o2List.get(i)).doubleValue())
						return false;
				}
				
				return true;*/
				
				return o1List.equals(o2List);
			} else if (Vector.class.isAssignableFrom(collectionElementClass)) {
				Iterator rt1IT = ((Collection)returnValue1).iterator();
				while(rt1IT.hasNext()) {
					o1List.add(((Vector)rt1IT.next()).times(-1));
				}
				
				if (o1List.size() != o2List.size())
					return false;
				
				Collections.sort(o1List, vs);
				Collections.sort(o2List, vs);
				
				return o1List.equals(o2List);
			}
			
			
		} else if (Number.class.isAssignableFrom(returnValue1.getClass()) && Number.class.isAssignableFrom(returnValue2.getClass())) {
			if (((Number)returnValue1).doubleValue() != ((Number)returnValue2).doubleValue() * -1)
				return false;
			else
				return true;
		} else if (Map.class.isAssignableFrom(returnValue1.getClass()) && Map.class.isAssignableFrom(returnValue2.getClass())) {
			Map rt1Map = (Map)returnValue1;
			Map rt2Map = (Map)returnValue2;
			
			List rt1List = new ArrayList(rt1Map.values());
			List rt2List = new ArrayList(rt2Map.values());
			
			if (rt1List.size() != rt2List.size())
				return false;
			
			Class<?> rt1ElementClazz = rt1List.get(0).getClass();
			Class<?> rt2ElementClazz = rt2List.get(0).getClass();
			
			if (!rt1ElementClazz.getName().equals(rt2ElementClazz.getName()))
				return false;
			
			if (!Collection.class.isAssignableFrom(rt1ElementClazz) && !Map.class.isAssignableFrom(rt1ElementClazz)) {
				return this.returnValuesApply(p1, rt1List, p2, rt2List);
			} else if (Collection.class.isAssignableFrom(rt1ElementClazz)) {
				int shouldCorrect = rt1List.size();
				
				int count = 0;
				//Can this be faster? O(n2)...
				for (int i = 0; i < rt1List.size(); i++) {
					for (int j = 0; j < rt2List.size(); j++) {
						if (this.returnValuesApply(p1, rt1List.get(i), p2, rt2List.get(j)))
							count++;
					}
				}
				
				/*System.out.println("Check shouldCorrect: " + shouldCorrect);
				System.out.println("Check count: " + count);*/
				
				if (shouldCorrect == count)
					return true;
			}
		}
		
		return false;
	}

	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2,
			int interestedVariable) {
		// TODO Auto-generated method stub
		Object o1 = i1.params[interestedVariable];
		Object o2 = i2.params[interestedVariable];
		for (int i = 0 ; i < i2.params.length; i++) {
			if (i != interestedVariable && i1.params[i] != i2.params[i]) {
				return false;
			}
		}
		
		//If i1 is not i2's parent, no need to compare
		if (i2.getParent() != i1) {
			return false;
		}
		
		if (!i2.getBackend().equals(this.getName()))
			return false;
		else
			return true;
		
		/*if (o1.getClass().isArray() && o2.getClass().isArray()) {
			int o1Length = Array.getLength(o1);
			int o2Length = Array.getLength(o2);
			
			//Input params of parent should be the same with child
			if (o1Length != o2Length)
				return false;
			
			double o1Val, o2Val;
			for (int i = 0; i < o1Length; i++) {
				o1Val = ((Number)Array.get(o1, i)).doubleValue();
				o2Val = ((Number)Array.get(o2, i)).doubleValue();
				
//				System.out.println("Check o1Val in Negatable: " + o1Val);
//				System.out.println("Check o2Val in Negatable: " + o2Val);
				
				if (o1Val != o2Val * -1)
					return false;
			}
			
			return true;
		} else if (Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass())) {
			int o1Length, o2Length;
			double o1Val, o2Val;
			Object[] o1Array = ((Collection)o1).toArray();
			Object[] o2Array = ((Collection)o2).toArray();
			
			o1Length = o1Array.length;
			o2Length = o2Array.length;
			
			if (o1Length != o2Length)
				return false;
			
			for (int i = 0; i < o1Length; i++) {
				o1Val = ((Number)o1Array[i]).doubleValue();
				o2Val = ((Number)o2Array[i]).doubleValue();
				
				if (o1Val != o2Val * -1)
					return false;
			}
			
			return true;
		} else if (Number.class.isAssignableFrom(o1.getClass()) && Number.class.isAssignableFrom(o2.getClass())) {
			double o1Val = ((Number)o1).doubleValue();
			double o2Val = ((Number)o2).doubleValue();
			
			if (o1Val != -1 * o2Val)
				return false;
			else
				return true;
		}
		
		return false;*/
	}

	@Override
	protected int[] getInterestedVariableIndices() {
		// TODO Auto-generated method stub
		ArrayList<Integer> rets = new ArrayList<Integer>();
		for(int i = 0;i<getMethod().getParameterTypes().length; i++)
		{
			if(getMethod().getParameterTypes()[i].isArray() || Collection.class.isAssignableFrom(getMethod().getParameterTypes()[i]))
				rets.add(i);
		}
		int[] ret = new int[rets.size()];
		for(int i = 0;i<rets.size();i++)
			ret[i]=rets.get(i);
		return ret;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "C:Negatable";
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return new Negate();
	}

}

