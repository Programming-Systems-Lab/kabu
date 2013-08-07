package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.AddNumericConstant;
import edu.columbia.cs.psl.mountaindew.absprop.PairwiseMetamorphicProperty;

public class AdditiveByConstant extends PairwiseMetamorphicProperty {

	@Override
	public String getName() {
		return "C:AdditiveByConstant";
	}

	boolean returnDoesntChange;
	
	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		try
		{
			/*if(returnValue1.equals(returnValue2))
				return true;*/
			if (Number.class.isAssignableFrom(p1.getClass()) && Number.class.isAssignableFrom(p2.getClass()) 
					&& Number.class.isAssignableFrom(returnValue1.getClass()) && Number.class.isAssignableFrom(returnValue2.getClass()))
				return getDifference(p1, p2) == getDifference(returnValue1, returnValue2);
			else if (p1.getClass().isArray() && p2.getClass().isArray()) {
				
				//Because propertiesApply now not check length, check them here
				if (Array.getLength(p1) != Array.getLength(p2))
					return false;
				
				double p1Element, p2Element, rt1Element, rt2Element;
				
				List rt1List = this.returnList(returnValue1);
				List rt2List = this.returnList(returnValue2);
				
				if (rt1List.size() != rt2List.size())
					return false;
				
				// If the length of return value is not the same with input, how to compare them?
				if (Array.getLength(p1) != rt1List.size())
					return false;
				
				for (int i = 0; i < Array.getLength(p1); i++) {
					p1Element = ((Number)Array.get(p1, i)).doubleValue();
					p2Element = ((Number)Array.get(p2, i)).doubleValue();
					rt1Element = ((Number)rt1List.get(i)).doubleValue();
					rt2Element = ((Number)rt2List.get(i)).doubleValue();
					
					/*System.out.println("DEBUG Additive p1 element: " + p1Element);
					System.out.println("DEBUG Additive p2 element: " + p2Element);
					System.out.println("DEBUG Additive rt1 element: " + rt1Element);
					System.out.println("DEBUG Additive rt2 element: " + rt2Element);*/
					
					//System.out.println("Add ori_input trans_input ori_output trans_output: " + p1Element + " " + p2Element + " " + rt1Element + " " + rt2Element);
					
					if (getDifference(p1Element, p2Element) != getDifference(rt1Element, rt2Element))
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
					
					//System.out.println("Add ori_input trans_input ori_output trans_output: " + p1Element + " " + p2Element + " " + rt1Element + " " + rt2Element);
					
					if (getDifference(p1Element, p2Element) != getDifference(rt1Element, rt2Element))
						return false;
				}
				return true;
			}
			return getDifference(p1, p2) == getDifference(returnValue1, returnValue2);
		}
		catch(IllegalArgumentException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	private double getDifference(Object o1, Object o2) throws IllegalArgumentException
	{
		if(!o1.getClass().equals(o2.getClass()))
			throw new IllegalArgumentException("Both parameters must be of the same type");
		if(o1.getClass().equals(Integer.class) || o1.getClass().equals(Integer.TYPE))
			return ((Integer) o1) - ((Integer) o2);
		else if(o1.getClass().equals(Short.class) || o1.getClass().equals(Short.TYPE))
			return ((Short) o1) - ((Short) o2);
		else if(o1.getClass().equals(Long.class) || o1.getClass().equals(Long.TYPE))
			return ((Long) o1) - ((Long) o2);
		else if(o1.getClass().equals(Double.class) || o1.getClass().equals(Double.TYPE))
			return ((Double) o1) - ((Double) o2);
		throw new IllegalArgumentException("Non numeric types");
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
		
		/*System.out.println("Add i1: " + i1.getFrontend() + " " + i1.getBackend());
		System.out.println("Add i2: " + i2.getFrontend() + " " + i2.getBackend());*/
				
		/*double o1Val, o2Val;
		if (o1.getClass().isArray() && o2.getClass().isArray()) {
			
			double o1Checker = ((Number)Array.get(o1, 0)).doubleValue();
			double o2Checker = ((Number)Array.get(o2, 0)).doubleValue();
			double checkDiff = getDifference(o1Checker, o2Checker);
						
			for (int i = 0; i < Array.getLength(o1); i++) {
				o1Val = ((Number)Array.get(o1, i)).doubleValue();
				o2Val = ((Number)Array.get(o2, i)).doubleValue();
				
				//System.out.println("Add ori trans input: " + o1Val + " " + o2Val);
				
				if (getDifference(o1Val, o2Val) != checkDiff)
					return false;
			}
			return true;
		} else if (Collection.class.isAssignableFrom(o1.getClass()) && Collection.class.isAssignableFrom(o2.getClass())) {
			List o1List = this.returnList(o1);
			List o2List = this.returnList(o2);
			
			double o1Checker = ((Number)o1List.get(0)).doubleValue();
			double o2Checker = ((Number)o2List.get(0)).doubleValue();
			double checkDiff = getDifference(o1Checker, o2Checker);
			
			for (int i = 0; i < o1List.size(); i++) {
				o1Val = ((Number)o1List.get(i)).doubleValue();
				o2Val = ((Number)o2List.get(i)).doubleValue();
				
				//System.out.println("Add ori trans input: " + o1Val + " " + o2Val);
				
				if (getDifference(o1Val, o2Val) != checkDiff)
					return false;
			}
			return true;
		}
		
		return true;*/
	}

	@Override
	protected int[] getInterestedVariableIndices() {
		ArrayList<Integer> rets = new ArrayList<Integer>();
		/*System.out.println("Check method parameter types: " + getMethod().getParameterTypes().length);
		System.out.println("Check method parameter: " + getMethod().getParameterTypes()[0].getComponentType().getName());*/
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
					Collection.class.isAssignableFrom(getMethod().getParameterTypes()[i]))
				rets.add(i);
		}
		int[] ret = new int[rets.size()];
		for(int i = 0;i<rets.size();i++)
			ret[i]=rets.get(i);
		return ret;
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		return new AddNumericConstant(); //TODO how do we make this parameterized
	}

}
