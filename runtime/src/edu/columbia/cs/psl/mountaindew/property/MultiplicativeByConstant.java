package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import weka.core.Instances;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.MultiplyByNumericConstant;
import edu.columbia.cs.psl.mountaindew.absprop.PairwiseMetamorphicProperty;

public class MultiplicativeByConstant extends PairwiseMetamorphicProperty {
	
	private int roundDigit = 5;
	
	private ContentEqualer ce = new ContentEqualer();
	@Override
	public String getName() {
		return "C:MultiplicativeByConstant";
	}

	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		try {
			if (Number.class.isAssignableFrom(p1.getClass()) && Number.class.isAssignableFrom(p2.getClass()) 
					&& Number.class.isAssignableFrom(returnValue1.getClass()) && Number.class.isAssignableFrom(returnValue2.getClass()))
				return getDivisor(p1, p2) == getDivisor(returnValue1, returnValue2);
			else if (p1.getClass().isArray() && p2.getClass().isArray()) {
				//Because propertiesApply now not check length, check them here
				if (Array.getLength(p1) != Array.getLength(p2))
					return false;
				
				if (String.class.isAssignableFrom(Array.get(p1, 0).getClass()) && 
						String.class.isAssignableFrom(Array.get(p2, 0).getClass())) {
					return this.checkReturnValOnly(returnValue1, returnValue2);
				}
				
				double p1Element, p2Element, rt1Element, rt2Element;
				
				List rt1List = this.returnList(returnValue1);
				List rt2List = this.returnList(returnValue2);
				
				if (rt1List.size() != rt2List.size())
					return false;
				
				if (Array.getLength(p1) != rt1List.size())
					return false;

				for (int i = 0; i < Array.getLength(p1); i++) {
					p1Element = ((Number)Array.get(p1, i)).doubleValue();
					p2Element = ((Number)Array.get(p2, i)).doubleValue();
					rt1Element = ((Number)rt1List.get(i)).doubleValue();
					rt2Element = ((Number)rt2List.get(i)).doubleValue();
					
					if (getDivisor(p1Element, p2Element) != getDivisor(rt1Element, rt2Element))
						return false;
				}
				return true;
			} else if (Collection.class.isAssignableFrom(p1.getClass()) && Collection.class.isAssignableFrom(p1.getClass())) {
				double p1Element, p2Element, rt1Element, rt2Element;
				
				List p1List = this.returnList(p1);
				List p2List = this.returnList(p2);
				List rt1List = this.returnList(returnValue1);
				List rt2List = this.returnList(returnValue2);
				
				if (p1List.size() != p2List.size())
					return false;
				
				if (rt1List.size() != rt2List.size())
					return false;
				
				if (p1List.size() != rt1List.size())
					return false;
				
				for (int i = 0; i < p1List.size(); i++) {
					p1Element = ((Number)p1List.get(i)).doubleValue();
					p2Element = ((Number)p2List.get(i)).doubleValue();
					rt1Element = ((Number)rt1List.get(i)).doubleValue();
					rt2Element = ((Number)rt2List.get(i)).doubleValue();
					
					if (getDivisor(p1Element, p2Element) != getDivisor(rt1Element, rt2Element))
						return false;
				}
				return true;
			} 
			System.out.println("Warning: Shouldn't go here");
			return getDivisor(p1, p2) == getDivisor(returnValue1, returnValue2);
		}
		catch(IllegalArgumentException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	private boolean checkReturnValOnly(Object returnValue1, Object returnValue2) {
		if (returnValue1.getClass().isArray() && returnValue2.getClass().isArray()) {
			int rt1Length = Array.getLength(returnValue1);
			int rt2Length = Array.getLength(returnValue2);
			
			if (rt1Length != rt2Length)
				return false;
			
			double[][] roundOriArray = (double[][])returnValue1;
			
			for (int i = 0; i < roundOriArray.length; i++) {
				System.out.print("Rt1 array: " + i + " ");
				for (int j = 0; j < roundOriArray[0].length; j++) {
					roundOriArray[i][j] = this.roundDouble(roundOriArray[i][j], roundDigit);
					System.out.print(roundOriArray[i][j]);
					System.out.print(" ");
				}
				System.out.println("");
			}
			
			double[][] roundTransArray = (double[][])returnValue2;
			for (int i = 0; i < roundTransArray.length; i++) {
				System.out.print("Rt2 array: " + i + " ");
				for (int j = 0; j < roundTransArray[0].length; j++) {
					roundTransArray[i][j] = this.roundDouble(roundTransArray[i][j], roundDigit);
					System.out.print(roundTransArray[i][j]);
					System.out.print(" ");
				}
				System.out.println("");
			}
			
			double divisor = this.getFirstDivisor(returnValue1, returnValue2);
			System.out.println("Confirm divisor: " + divisor);
			
			Object divRt2 = this.multiplyObject(returnValue2, divisor);
			double[][] finalTransArray = (double[][])divRt2;
			for (int i = 0; i < finalTransArray.length; i++) {
				System.out.print("Transform array: " + i + " ");
				for (int j = 0; j < finalTransArray[0].length; j++) {
					//roundTransArray[i][j] = this.roundDouble(roundTransArray[i][j], roundDigit);
					finalTransArray[i][j] = this.roundDouble(finalTransArray[i][j], roundDigit);
					System.out.print(finalTransArray[i][j]);
					System.out.print(" ");
				}
				System.out.println("");
			}
			//return ce.checkEquivalence(roundOriArray, finalTransArray);
			
			for (int i = 0; i < finalTransArray.length; i++) {
				for (int j = 0; j < finalTransArray[0].length; j++) {
					if (!this.checkEquivalenceWithThreshold(roundOriArray[i][j], finalTransArray[i][j])) {
						return false;
					}
				}
			}
			
			return true;
		} else if (Collection.class.isAssignableFrom(returnValue1.getClass()) && 
				Collection.class.isAssignableFrom(returnValue2.getClass())) {
			List rt1List = (ArrayList)returnValue1;
			List rt2List = (ArrayList)returnValue2;
			
			if (rt1List.size() != rt2List.size())
				return false;
			
			double divisor = this.getFirstDivisor(returnValue1, returnValue2);	
			Object divRt2 = this.multiplyObject(returnValue2, divisor);
				
			return ce.checkEquivalence(returnValue1, divRt2);
		}
		return false;
	}
	
	private boolean checkEquivalenceWithThreshold(double d1, double d2) {
		//Need a way to define a more scientific threshold
		if (Math.abs(d1 - d2) <= 5 * Math.pow(10, this.roundDigit * -1)) {
			return true;
		} else {
			return false;
		}
	}
	
	private double getFirstDivisor(Object o1, Object o2) {
		if (Number.class.isAssignableFrom(o1.getClass()) && Number.class.isAssignableFrom(o2.getClass())) {
			return getDivisor(o1, o2);
		} else if (o1.getClass().isArray() && o2.getClass().isArray()) {
			int i = 0;
			while (true) {
				double ret = getFirstDivisor(Array.get(o1, i), Array.get(o2, i));
				if (ret == Double.MAX_VALUE)
					i++;
				else
					return ret;
			}
		} else if (Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass())) {
			List o1List = (ArrayList)o1;
			List o2List = (ArrayList)o2;
			int i = 0;
			while (true) {
				double ret = getFirstDivisor(o1List.get(i), o2List.get(i));
				if (ret == Double.MAX_VALUE)
					i++;
				else return ret;
			}
		} else {
			return getDivisor(o1, o2);
		}
	}
	
	private Object multiplyObject(Object obj, double divisor) {
		if (Number.class.isAssignableFrom(obj.getClass())) {
			return (double)obj * divisor;
		} else if (obj.getClass().isArray()) {
			int objLength = Array.getLength(obj);
			
			for (int i = 0; i < objLength; i++) {
				Array.set(obj, i, this.multiplyObject(Array.get(obj, i), divisor));
			}
			return obj;
		} else if (Collection.class.isAssignableFrom(obj.getClass())) {
			List objList = (ArrayList)obj;
			List retList = new ArrayList();
			for (int i = 0; i < objList.size(); i++) {
				retList.add(this.multiplyObject(objList.get(i), divisor));
			}
			return retList;
		}
		
		return null;
	}

	private double getDivisor(Object o1, Object o2) throws IllegalArgumentException
	{
		if(!o1.getClass().equals(o2.getClass()))
			throw new IllegalArgumentException("Both parameters must be of the same type");
		if(o1.getClass().equals(Integer.class) || o1.getClass().equals(Integer.TYPE))
		{
			if((Integer) o2 != 0) {
				double rawResult = ((Integer) o1) / ((Integer) o2);
				return this.roundDouble(rawResult, 1);
			} else {
				return Double.MAX_VALUE;
			}
		}
		else if(o1.getClass().equals(Short.class) || o1.getClass().equals(Short.TYPE))
		{
			if((Short) o2 != 0) {
				double rawResult = ((Short) o1) / ((Short) o2);
				return this.roundDouble(rawResult, 1);
			} else {
				return Double.MAX_VALUE;
			}
		}
		else if(o1.getClass().equals(Long.class) || o1.getClass().equals(Long.TYPE))
		{
			if((Long) o2 != 0) {
				double rawResult = ((Long) o1) / ((Long) o2);
				return this.roundDouble(rawResult, 1);
			} else {
				return Double.MAX_VALUE;
			}
		}
		else if(o1.getClass().equals(Double.class) || o1.getClass().equals(Double.TYPE))
		{
			if((Double) o2 != 0) {
				double rawResult = ((Double)o1) / ((Double) o2);
				return this.roundDouble(rawResult, 1);
			} else {
				return Double.MAX_VALUE;
			}
		}
		throw new IllegalArgumentException("Non numeric types");
	}
	
	private double roundDouble(double numberToRound, int digit) {
		int roundMultiplier = (int)Math.pow(10, digit);
		numberToRound = numberToRound * roundMultiplier;
		numberToRound = Math.round(numberToRound);
		numberToRound = numberToRound / roundMultiplier;
		return numberToRound;
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
		
		//If i2's checker is not this one, return false
		if (!i2.getBackend().equals(this.getName())) {
			return false;
		} else {
			return true;
		}
		
		//System.out.println("Mul i1: " + i1.getFrontend() + " " + i1.getBackend());
		//System.out.println("Mul i2: " + i2.getFrontend() + " " + i2.getBackend());
				
		/*double o1Val, o2Val;
		if (o1.getClass().isArray() && o2.getClass().isArray()) {
			double o1Checker = ((Number)Array.get(o1, 0)).doubleValue();
			double o2Checker = ((Number)Array.get(o2, 0)).doubleValue();
			double checkDiv = getDivisor(o1Checker, o2Checker);

			for (int i = 0; i < Array.getLength(o1); i++) {
				o1Val = ((Number)Array.get(o1, i)).doubleValue();
				o2Val = ((Number)Array.get(o2, i)).doubleValue();
				
				//System.out.println("Mul ori trans input: " + o1Val + " " + o2Val);
				
				if (getDivisor(o1Val, o2Val) != checkDiv)
					return false;
			}
			return true;
		} else if (Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass())) {
			List o1List = this.returnList(o1);
			List o2List = this.returnList(o2);
			
			double o1Checker = ((Number)o1List.get(0)).doubleValue();
			double o2Checker = ((Number)o2List.get(0)).doubleValue();
			double checkDiv = getDivisor(o1Checker, o2Checker);
			
			for (int i = 0; i < o1List.size(); i++) {
				o1Val = ((Number)o1List.get(i)).doubleValue();
				o2Val = ((Number)o2List.get(i)).doubleValue();
				
				//System.out.println("Mul ori trans input: " + o1Val + " " + o2Val);
				
				if (getDivisor(o1Val, o2Val) != checkDiv)
					return false;
			}
			return true;
		}
		
		return true;*/
	}

	@Override
	protected int[] getInterestedVariableIndices() {
		//Find a way to make this more flexible
		ArrayList<Integer> rets = new ArrayList<Integer>();
		for(int i = 0;i<getMethod().getParameterTypes().length; i++)
		{
			if(getMethod().getParameterTypes()[i].equals(Integer.TYPE) || 
					getMethod().getParameterTypes()[i].equals(Short.TYPE) || 
					getMethod().getParameterTypes()[i].equals(Long.TYPE) || 
					getMethod().getParameterTypes()[i].equals(Double.TYPE) || 
					Integer.class.isAssignableFrom(getMethod().getParameterTypes()[i]) || 
					Float.class.isAssignableFrom(getMethod().getParameterTypes()[i])|| 
					Double.class.isAssignableFrom(getMethod().getParameterTypes()[i])||
					getMethod().getParameterTypes()[i].isArray()||
					Collection.class.isAssignableFrom(getMethod().getParameterTypes()[i])||
					Instances.class.isAssignableFrom(getMethod().getParameterTypes()[i])||
					String.class.isAssignableFrom(getMethod().getParameterTypes()[i]))
				rets.add(i);
		}
		int[] ret = new int[rets.size()];
		for(int i = 0;i<rets.size();i++)
			ret[i]=rets.get(i);
		return ret;
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		return new MultiplyByNumericConstant(); //TODO
	}
}
