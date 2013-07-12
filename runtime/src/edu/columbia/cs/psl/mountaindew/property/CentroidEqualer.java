package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.mahout.math.Vector;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.mountaindew.absprop.EqualerAbstract;

public class CentroidEqualer extends EqualerAbstract{
	
	private double convergence = 0.05;

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
			
			List rt1Values = new ArrayList(rt1Map.values());
			List rt2Values = new ArrayList(rt2Map.values());
			
			if (Collection.class.isAssignableFrom(rt1Values.get(0).getClass()) && Collection.class.isAssignableFrom(rt2Values.get(0).getClass())) {
				Vector rt1Centroid;
				Vector rt2Centroid;
				for (int i = 0; i < rt1Values.size(); i++) {
					for (int j = 0; j < rt2Values.size(); j++) {
						rt1Centroid = this.calculateCentroid((Collection)rt1Values.get(i));
						rt2Centroid = this.calculateCentroid((Collection)rt2Values.get(j));
						
						if (this.checkEquivalence(rt1Centroid, rt2Centroid))
							count++;
					}
				}
				
				System.out.println("Should equal: " + shouldEqual);
				System.out.println("count: " + count);
			} else {
				for (int i = 0; i < rt1Values.size(); i++) {
					for (int j = 0; j < rt2Values.size(); j++) {
						if (this.checkEquivalence(rt1Values.get(i), rt2Values.get(j)))
							count++;
					}
				}
			}
			
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
		
		if (Vector.class.isAssignableFrom(c1.getClass()) && Vector.class.isAssignableFrom(c2.getClass())) {
			Vector v1 = (Vector)c1;
			Vector v2 = (Vector)c2;
			
			Vector lowerBound = v1.times(1 - this.convergence);
			Vector upperBound = v1.times(1 + this.convergence);
			
			/*System.out.println("v1: " + v1 + " " + lowerBound + " " + upperBound);
			
			System.out.println("Check lowerbound: " + this.checkLowerbound(v2, lowerBound));
			System.out.println("Check upperbound: " + this.checkUpperbound(v2, upperBound));*/
			
			if (this.checkLowerbound(v2, lowerBound) && this.checkUpperbound(v2, upperBound))
				return true;
		} else if (Number.class.isAssignableFrom(c1.getClass()) && Number.class.isAssignableFrom(c2.getClass())) {
			double d1 = ((Number)c1).doubleValue();
			double d2 = ((Number)c2).doubleValue();
			
			double upperBound = d1 * (1 + this.convergence);
			double lowerBound = d1 * (1 - this.convergence);
			
			if (d2 < upperBound && d2 > lowerBound)
				return true;
		} else if (c1.getClass().isArray() && c2.getClass().isArray()) {
			int arrayLength = Array.getLength(c1);
			
			for (int i = 0; i < arrayLength; i++) {
				if (!this.checkEquivalence(Array.get(c1, i), Array.get(c2, i)))
					return false;
			}
			
			return true;
		} else if (Collection.class.isAssignableFrom(c1.getClass()) && Collection.class.isAssignableFrom(c2.getClass())) {
			List c1List = new ArrayList((Collection)c1);
			List c2List = new ArrayList((Collection)c2);
			
			Collections.sort(c1List);
			Collections.sort(c2List);
			
			for (int i = 0 ; i < c1List.size(); i++) {
				if (!this.checkEquivalence(c1List.get(i), c2List.get(i))) {
					return false;
				}
			}
			
			return true;
		}
		return false;
	}
	
	private boolean checkLowerbound(Vector vec, Vector lowerBound) {
		if (vec.size() != lowerBound.size())
			return false;
		
		for (int i = 0; i < vec.size(); i++) {
			//System.out.println("Vec lower: " + vec.get(i) + " " + lowerBound.get(i) + " " + (vec.get(i) < lowerBound.get(i)));
			if (vec.get(i) < lowerBound.get(i)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean checkUpperbound(Vector vec, Vector upperBound) {
		if (vec.size() != upperBound.size())
			return false;
		
		for (int i = 0; i < vec.size(); i++) {
			//System.out.println("Vec lower: " + vec.get(i) + " " + upperBound.get(i) + " " + (vec.get(i) > upperBound.get(i)));
			if (vec.get(i) > upperBound.get(i))
				return false;
		}
		
		return true;
	}

	

}
