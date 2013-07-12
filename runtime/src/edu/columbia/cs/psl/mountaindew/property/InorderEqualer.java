package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mahout.math.Vector;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Shuffle;
import edu.columbia.cs.psl.mountaindew.absprop.EqualerAbstract;
import edu.columbia.cs.psl.mountaindew.absprop.PairwiseMetamorphicProperty;

public class InorderEqualer extends EqualerAbstract {

	@Override
	public String getName() {
		return "C:InorderEqualer";
	}


	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		
		return checkEquivalence(returnValue1, returnValue2);

		//For array
		/*if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {
			int rt1Length = Array.getLength(returnValue1);
			int rt2Length = Array.getLength(returnValue2);
			
			if (rt1Length != rt2Length)
				return false;
			
			double tmp1, tmp2;
			for (int i = 0; i < rt1Length; i++) {
				tmp1 = ((Number)Array.get(returnValue1, i)).doubleValue();
				tmp2 = ((Number)Array.get(returnValue2, i)).doubleValue();
				
				if (tmp1 != tmp2)
					return false;
			}
			return true;
		}*/
		
		//For other type, includind Collection
		//return returnValue1.equals(returnValue2);
	}

	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2, int interestedVariable) {
		//Object o1 = i1.params[interestedVariable];
		//Object o2 = i2.params[interestedVariable];
		for(int i = 0;i<i1.params.length;i++)
			if(i!=interestedVariable && !i1.params[i].equals(i2.params[i]))
				return false;
		
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
	protected boolean checkEquivalence(Object o1, Object o2) {
		// TODO Auto-generated method stub
		
		if (o1.equals(o2))
			return true;
		
		Class c1 = o1.getClass();
		Class c2 = o2.getClass();
		
		if (!c1.getName().equals(c2))
			return false;
		
		if (c1.isArray() && c2.isArray()) {
			int rt1Length = Array.getLength(o1);
			int rt2Length = Array.getLength(o2);
			
			if (rt1Length != rt2Length)
				return false;
			
			/*double tmp1, tmp2;
			for (int i = 0; i < rt1Length; i++) {
				tmp1 = ((Number)Array.get(o1, i)).doubleValue();
				tmp2 = ((Number)Array.get(o2, i)).doubleValue();
				
				if (tmp1 != tmp2)
					return false;
			}
			return true;*/
			List c1List = Arrays.asList((Object[])o1);
			List c2List = Arrays.asList((Object[])o2);
			
			return c1List.equals(c2List);
		} else if (Collection.class.isAssignableFrom(c1) && Collection.class.isAssignableFrom(c2)) {
			List c1List = new ArrayList((Collection)o1);
			List c2List = new ArrayList((Collection)o2);
			//System.out.println("Check c1List: " + c1List);
			//System.out.println("Check c2List: " + c2List);
			
			return c1List.equals(c2List);
		} else if (Map.class.isAssignableFrom(c1) && Map.class.isAssignableFrom(c2)) {
			Map c1Map = (Map)o1;
			Map c2Map = (Map)o2;
			
			if (c1Map.size() != c2Map.size())
				return false;
			
			List c1List = new ArrayList(c1Map.values());
			List c2List = new ArrayList(c2Map.values());
			
			int shouldCorrect = c1Map.size();
			int count = 0;
			
			for (int i = 0; i < c1List.size(); i++) {
				for (int j = 0; j < c2List.size(); j++) {
					if (this.checkEquivalence(c1List.get(i), c2List.get(j)))
						count++;
				}
			}
			
			if (count == shouldCorrect)
				return true;
		}
		
		return false;
	}
}

