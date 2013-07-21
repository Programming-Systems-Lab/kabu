package edu.columbia.cs.psl.mountaindew.property;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.struct.Word;
import edu.columbia.cs.psl.mountaindew.absprop.EqualerAbstract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class ContentEqualer extends EqualerAbstract{

	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		System.out.println("In the return value apply");
		return this.checkEquivalence(returnValue1, returnValue2);
	}

	@Override
	protected boolean propertyApplies(MethodInvocation i1, MethodInvocation i2,
			int interestedVariable) {
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
		if (c1.equals(c2))
			return true;
		
		Set c1Set, c2Set;
		if (Collection.class.isAssignableFrom(c1.getClass()) && Collection.class.isAssignableFrom(c2.getClass())) {
			Object sentinel = ((Collection)c1).iterator().next();
			
			List c1List = new ArrayList((Collection)c1);
			List c2List = new ArrayList((Collection)c2);
			if (Word.class.isAssignableFrom(sentinel.getClass())) {
				c1List = this.selectTopWords((List<Word>)c1List, 10);
				c2List = this.selectTopWords((List<Word>)c2List, 10);
			}
			
			System.out.println("Check c1List: " + c1List);
			System.out.println("Check c2List: " + c2List);
			
			c1Set = new HashSet((Collection)c1List);
			c2Set = new HashSet((Collection)c2List);
			
			return c1Set.equals(c2Set);
		} else if (c1.getClass().isArray() && c2.getClass().isArray()) {
			c1Set = new HashSet(Arrays.asList((Object[])c1));
			c2Set = new HashSet(Arrays.asList((Object[])c2));
			
			return c1Set.equals(c2Set);
		} else if (Map.class.isAssignableFrom(c1.getClass()) && Map.class.isAssignableFrom(c2.getClass())) {
			System.out.println("c1 c2 are maps");
			Map c1Map = (Map)c1;
			Map c2Map = (Map)c2;
			
			if (c1Map.size() != c2Map.size())
				return false;
			
			int shouldCorrect  = c1Map.size();
			int count = 0;
			
			
			List c1List = new ArrayList(c1Map.values());
			List c2List = new ArrayList(c2Map.values());

			for (int i = 0; i < c1List.size(); i++) {
				for (int j =0; j < c2List.size(); j++) {
					if (this.checkEquivalence(c1List.get(i), c2List.get(j)))
						count++;
				}
			}
			
			System.out.println("Check shouldCorrect: " + shouldCorrect);
			System.out.println("Check count: " + count);
			
			if (count == shouldCorrect)
				return true;
			else
				return false;
		}
		return false;
	}
	
	private List<String> selectTopWords(List<Word>targetList, int k) {
		int finalK = 0;
		
		if (k > targetList.size())
			finalK = targetList.size();
		else 
			finalK = k;
		
		Collections.sort(targetList);
		
		List<String> topList = new ArrayList<String>();
		Word tmpWord;
		for (int i = 0; i < finalK; i++) {
			tmpWord = targetList.get(i);
			topList.add(tmpWord.getContent());
		}
		return topList;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "C:ContentEqualer";
	}

}
