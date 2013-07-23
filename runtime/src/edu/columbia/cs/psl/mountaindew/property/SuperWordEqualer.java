package edu.columbia.cs.psl.mountaindew.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;

import edu.columbia.cs.psl.metamorphic.struct.Word;

public class SuperWordEqualer extends ContentEqualer{
	
	private String superWord;
	
	@Override
	protected boolean checkEquivalence(Object o1, Object o2) {
		
		Set c1Set, c2Set;
		if (Map.class.isAssignableFrom(o1.getClass()) && Map.class.isAssignableFrom(o2.getClass())) {
			Map c1Map = (Map)o1;
			Map c2Map = (Map)o2;
			
			if (c1Map.size() != c2Map.size())
				return false;
			
			int shouldCorrect = c1Map.size();
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
			
			if (shouldCorrect == count)
				return true;
		} else if (Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass())) {
			Object sentinel = ((Collection)o1).iterator().next();
			int topK = 10;
			
			if (Word.class.isAssignableFrom(sentinel.getClass())) {
				List c1List = new ArrayList((Collection)o1);
				List c2List = new ArrayList((Collection)o2);
				
				if (c1List.size()+1 != c2List.size())
					return false;
				
				//Filter out top word
				if (Word.class.isAssignableFrom(sentinel.getClass())) {
					c1List = this.selectTopWords((List<Word>)c1List, topK);
					c2List = this.selectTopWords((List<Word>)c2List, topK+1);
				}
				
				System.out.println("Check c1List: " + c1List);
				System.out.println("Check c2List: " + c2List);
				
				String tmpSuper = (String)c2List.remove(0);
				if (this.superWord == null) {
					this.superWord = tmpSuper;
				} else {
					if (!this.superWord.equals(tmpSuper))
						return false;
				}
				
				System.out.println("Superword: " + this.superWord);
				
				System.out.println("After c1List: " + c1List);
				System.out.println("After c2List: " + c2List);
				
				c1Set = new HashSet((Collection)c1List);
				c2Set = new HashSet((Collection)c2List);
				
				return c1Set.equals(c2Set);
			}
		} else if (o1.getClass().isArray() && o2.getClass().isArray()) {
			List c1List = Arrays.asList((Object[])o1);
			List c2List = Arrays.asList((Object[])o2);
			
			return this.checkEquivalence(c1List, c2List);
		}
		return false;
	}

	@Override
	public String getName() {
		return "C:SuperWordEqualer";
	}
	

}
