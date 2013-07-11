package edu.columbia.cs.psl.mountaindew.property;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.mountaindew.absprop.EqualerAbstract;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContentEqualer extends EqualerAbstract{

	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		// TODO Auto-generated method stub
		return this.checkEquivalence(returnValue1, returnValue2);
	}

	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2,
			int interestedVariable) {
		// TODO Auto-generated method stub
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
	protected boolean checkEquivalence(Object c1, Object c2) {
		// TODO Auto-generated method stub
		
		if (c1.equals(c2))
			return true;
		
		Set c1Set, c2Set;
		if (Collection.class.isAssignableFrom(c1.getClass()) && Collection.class.isAssignableFrom(c2.getClass())) {
			c1Set = new HashSet((Collection)c1);
			c2Set = new HashSet((Collection)c1);
			
			//System.out.println("Check c1Set: " + c1Set);
			//System.out.println("Check c2Set: " + c2Set);
			
			return c1Set.equals(c2Set);
		} else if (c1.getClass().isArray() && c2.getClass().isArray()) {
			c1Set = new HashSet(Arrays.asList((Object[])c1));
			c2Set = new HashSet(Arrays.asList((Object[])c2));
			
			return c1Set.equals(c2Set);
		} else if (Map.class.isAssignableFrom(c1.getClass()) && Map.class.isAssignableFrom(c2.getClass())) {
			Map c1Map = (Map)c1;
			Map c2Map = (Map)c2;
			
			if (c1Map.size() != c2Map.size())
				return false;
			
			for (Object key: c1Map.keySet()) {
				if (!checkEquivalence(c1Map.get(key), c2Map.get(key))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "C:ContentEqualer";
	}

}
