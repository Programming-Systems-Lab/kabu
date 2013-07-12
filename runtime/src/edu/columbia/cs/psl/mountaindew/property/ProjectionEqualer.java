package edu.columbia.cs.psl.mountaindew.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.mahout.math.Vector;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.mountaindew.absprop.EqualerAbstract;
import edu.columbia.cs.psl.mountaindew.absprop.PairwiseMetamorphicProperty;
import edu.columbia.cs.psl.mountaindew.util.VectorSorter;

public class ProjectionEqualer extends EqualerAbstract{

	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		// TODO Auto-generated method stub
		
		//Integer is cluster number and set contains vector for this cluster
		if (Map.class.isAssignableFrom(returnValue1.getClass()) && Map.class.isAssignableFrom(returnValue2.getClass())) {
			
			Set<Entry> tmpSet = ((Map)returnValue1).entrySet();
			
			if (tmpSet.isEmpty())
				return false;
			
			//At least one entry in the map
			Entry tmpEntry = tmpSet.iterator().next();
			
			if (Collection.class.isAssignableFrom(tmpEntry.getValue().getClass())) {
				
				Map<Object, Collection> tmpRt1Map = (Map<Object, Collection>)returnValue1;
				Map<Object, Collection> tmpRt2Map = (Map<Object, Collection>)returnValue2;
				
				if (tmpRt1Map.size() != tmpRt2Map.size())
					return false;
				
				if (!tmpRt1Map.keySet().containsAll(tmpRt2Map.keySet()))
					return false;
				
				if (!tmpRt2Map.keySet().containsAll(tmpRt1Map.keySet()))
					return false;
				
				List tmp1ValList = new ArrayList(tmpRt1Map.values());
				List tmp2ValList = new ArrayList(tmpRt2Map.values());
				
				int shouldCorrect = tmpRt1Map.size();
				int count = 0;
				for (int i = 0; i < tmp1ValList.size(); i++) {
					for (int j = 0; j < tmp2ValList.size(); j++) {
						if (this.checkEquivalence(tmp1ValList.get(i), tmp2ValList.get(j)))
							count++;
					}
				}
				
				if (count == shouldCorrect)
					return true;
			}
		} else if (Collection.class.isAssignableFrom(returnValue1.getClass()) && Collection.class.isAssignableFrom(returnValue2.getClass())) {
			List v1List = new ArrayList((Collection)returnValue1);
			List v2List = new ArrayList((Collection)returnValue2);
			return this.checkEquivalence(v1List, v2List);
		}
		
		return false;
	}
	
	protected boolean checkEquivalence(Object obj1, Object obj2) {
		Class c1 = obj1.getClass();
		Class c2 = obj2.getClass();
		
		if (!c1.getName().equals(c2.getName()))
			return false;
		
		if (Collection.class.isAssignableFrom(c1) && Collection.class.isAssignableFrom(c2)) {
			return checkEquivalence((Collection)obj1, (Collection)obj2);
		} else if (c1.isArray() && c2.isArray()) {
			Collection col1 = Arrays.asList((Object[]) obj1);
			Collection col2 = Arrays.asList((Object[]) obj2);
			
			return checkEquivalence(col1, col2);
		} else {
			return (obj1.equals(obj2));
		}
		
	}
	
	protected boolean checkEquivalence(Collection v1Collection, Collection v2Collection) {
		List v1List = new ArrayList(v1Collection);
		List v2List = new ArrayList(v2Collection);
		
		if (v1List.size() != v2List.size())
			return false;
		
		if (Vector.class.isAssignableFrom(v1List.get(0).getClass()) && Vector.class.isAssignableFrom(v2List.get(0).getClass())) {
			List<Vector> vectors1 = (List<Vector>)v1List;
			List<Vector> vectors2 = (List<Vector>)v2List;
			
			VectorSorter vs = new VectorSorter();
			Collections.sort(vectors1, vs);
			Collections.sort(vectors2, vs);
			
			//Grab one element from v2List to build s 
			//Traverse v2List to check the second halves of each element are equal
			Vector v1, v2, vStandard, vBuf;
			int v1Size = vectors1.get(0).size();
			int v2Size = vectors2.get(0).size();
			int vDiff = v2Size -v1Size;
			vStandard = getPartialVector(vectors2.get(0), v1Size, vDiff);
			
			for (int i = 0; i < vectors2.size(); i++) {
				v2 = vectors2.get(i);
				vBuf = getPartialVector(v2, v1Size, vDiff);
				//System.out.println("Check second half: " + vBuf.toString());
				
				if (vs.compare(vBuf, vStandard) != 0)
					return false;
				
				v1 = vectors1.get(i);
				vBuf = getPartialVector(v2, 0, v1Size);
				//System.out.println("Check first half: " + vBuf.toString());
				
				if (vs.compare(v1, vBuf) != 0)
					return false;
			}
			
			return true;
		}
		//Any other datastructure?
		
		return false;
	}
		
	private Vector getPartialVector(Vector vector, int start, int length) {
		return vector.viewPart(start, length);
	}

	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2,
			int interestedVariable) {		
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
		return "C:ProjectionEqualer";
	}
}
