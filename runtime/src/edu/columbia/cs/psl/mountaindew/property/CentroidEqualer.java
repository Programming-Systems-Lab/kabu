package edu.columbia.cs.psl.mountaindew.property;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.mahout.math.Vector;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.mountaindew.absprop.EqualerAbstract;

public class CentroidEqualer extends EqualerAbstract{

	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		// TODO Auto-generated method stub
		
		if (Map.class.isAssignableFrom(returnValue1.getClass()) && Map.class.isAssignableFrom(returnValue2.getClass())) {
			Map rt1Map = (Map)returnValue1;
			Map rt2Map = (Map)returnValue2;
			
			if (rt1Map.size() != rt2Map.size())
				return false;
			
			//Start to calculate centroid
			int shouldEqual = rt1Map.size();
			int count = 0;
			for (Object key: rt1Map.keySet()) {
				if (Collection.class.isAssignableFrom(rt1Map.get(key).getClass()) && Collection.class.isAssignableFrom(rt2Map.get(key).getClass())) {
					Vector rt1Centroid = this.calculateCentroid((Collection)rt1Map.get(key));
					Vector rt2Centroid = this.calculateCentroid((Collection)rt2Map.get(key));
					
					System.out.println("rt1Centroid: " + rt1Centroid);
					System.out.println("rt2Centroid: " + rt2Centroid);
					
					if (rt1Centroid.equals(rt2Centroid))
						count++;
				}
			}
			
			System.out.println("Should equal: " + shouldEqual);
			System.out.println("count: " + count);
			
			if (shouldEqual == count)
				return true;
			
		}
		return false;
	}
	
	private <T> T calculateCentroid(Collection<T> vectors) {
		if (Vector.class.isAssignableFrom(vectors.iterator().next().getClass())) {
			Collection<Vector> vecCollection = (Collection<Vector>)vectors;
			Vector centroid = vecCollection.iterator().next().like();
			for (Vector v: vecCollection) {
				centroid = centroid.plus(v);
			}
			
			centroid = centroid.divide(vecCollection.size());
			
			return (T)centroid;
		}
		
		return null;
	}

	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2,
			int interestedVariable) {
		// TODO Auto-generated method stub
		Object o1 = i1.params[interestedVariable];
		Object o2 = i2.params[interestedVariable];
		for(int i = 0;i<i1.params.length;i++)
			if(i!=interestedVariable && !i1.params[i].equals(i2.params[i]))
				return false;
		
		//If i1 is not i2's parent, no need to compare
		if (i2.getParent() != i1) {
			return false;
		}

		if (!i2.getBackend().equals(this.getName()))
			return false;
		
		//Temporarily only for collection and vector. Other data type may not need to check the projection property
		if (Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass()))
			return true;
		else if (Vector.class.isAssignableFrom(o1.getClass()) && Vector.class.isAssignableFrom(o2.getClass()))
			return true;
		else if (Map.class.isAssignableFrom(o1.getClass()) && Map.class.isAssignableFrom(o2.getClass()))
			return true;
		else if (o1.getClass().isArray() && o2.getClass().isArray())
			return true;
		else
			return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "C:CentroidEqualer";
	}

	@Override
	protected boolean checkEquivalence(Object c1, Object c2) {
		// TODO Auto-generated method stub
		return false;
	}

	

}
