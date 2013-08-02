package edu.columbia.cs.psl.mountaindew.property;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.struct.Word;
import edu.columbia.cs.psl.mountaindew.absprop.EqualerAbstract;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContentEqualer extends EqualerAbstract{
	
	private int roundDigit = 5;

	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		System.out.println("In the return value apply");
		System.out.println("RT1: " + returnValue1.getClass().getName());
		System.out.println("RT2: " + returnValue2.getClass().getName());
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
		if (Number.class.isAssignableFrom(c1.getClass()) && Number.class.isAssignableFrom(c2.getClass())) {
			double roundC1 = this.roundDouble((double)c1, roundDigit);
			double roundC2 = this.roundDouble((double)c2, roundDigit);
			//System.out.println("rt1 rt2: " + roundC1 + " " + roundC2);
			return (roundC1 == roundC2);
		} else if (Collection.class.isAssignableFrom(c1.getClass()) && Collection.class.isAssignableFrom(c2.getClass())) {
			Object sentinel = ((Collection)c1).iterator().next();
						
			List c1List = new ArrayList((Collection)c1);
			List c2List = new ArrayList((Collection)c2);
			
			int topK = (int)(0.2 * c1List.size());
			
			//System.out.println("Select top " + topK + " words");
			
			if (Word.class.isAssignableFrom(sentinel.getClass())) {
				c1List = this.selectTopWords((List<Word>)c1List, topK);
				c2List = this.selectTopWords((List<Word>)c2List, topK);
			}
			
			System.out.println("Check c1List: " + c1List);
			System.out.println("Check c2List: " + c2List);
			
			if (c1List.size() != c2List.size())
				return false;
			
			int shouldCorrect = c1List.size();
			int count = 0;
			
			for (int i = 0; i < c1List.size(); i++) {
				if (this.checkEquivalence(c1List.get(i), c2List.get(i)))
					count++;
			}
			
			if (count == shouldCorrect)
				return true;
			else
				return false;
			
			/*c1Set = new HashSet((Collection)c1List);
			c2Set = new HashSet((Collection)c2List);
			
			return c1Set.equals(c2Set);*/
		} else if (c1.getClass().isArray() && c2.getClass().isArray()) {
			//c1Set = new HashSet(Arrays.asList((Object[])c1));
			//c2Set = new HashSet(Arrays.asList((Object[])c2));			
			int c1Length = Array.getLength(c1);
			int c2Length = Array.getLength(c2);
			
			if (c1Length != c2Length)
				return false;
			
			int shouldCorrect = c1Length;
			int count = 0;
			
			for (int i = 0; i < c1Length; i++) {
				if (this.checkEquivalence(Array.get(c1, i), Array.get(c2, i)))
					count++;
			}
			
			System.out.println("Check should correct: " + shouldCorrect);
			System.out.println("Check count: " + count);
			
			if (count == shouldCorrect)
				return true;
			else
				return false;
			
			/*for (int i = 0; i < c1Len; i++) {
				System.out.println("Check c1 element: " + Array.get(c1, i));
				System.out.println("Check c2 element: " + Array.get(c2, i));
				if (!this.checkEquivalence(Array.get(c1, i), Array.get(c2, i)))
					return false;
			}
			return true;*/
		} else if (Map.class.isAssignableFrom(c1.getClass()) && Map.class.isAssignableFrom(c2.getClass())) {
			//System.out.println("c1 c2 are maps");
			Map c1Map = (Map)c1;
			Map c2Map = (Map)c2;
			
			if (c1Map.size() != c2Map.size())
				return false;
			
			int shouldCorrect  = c1Map.size();
			int count = 0;
			
			
			List c1List = new ArrayList(c1Map.values());
			List c2List = new ArrayList(c2Map.values());

			for (int i = 0; i < c1List.size(); i++) {
				for (int j = 0; j < c2List.size(); j++) {
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
	
	protected List<String> selectTopWords(List<Word>targetList, int k) {
		int finalK = 0;
		
		//this.selectNonZeroWords(targetList);
		
		if (k > targetList.size())
			finalK = targetList.size();
		else 
			finalK = k;
		
		Collections.sort(targetList);
		
		List<String> topList = new ArrayList<String>();
		Word tmpWord;
		for (int i = 0; i < targetList.size(); i++) {
			tmpWord = targetList.get(i);
			
			if (i >= finalK) {
				if (targetList.get(i-1).getRoundVal() * 0.95 < tmpWord.getRoundVal()) {
					topList.add(tmpWord.getContent());
				} else {
					break;
				}
			} else {
				topList.add(tmpWord.getContent());
			}
		}
		return topList;
	}
	

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "C:ContentEqualer";
	}

}
