package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.rits.cloning.Cloner;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.mountaindew.struct.Parameter;
import edu.columbia.cs.psl.mountaindew.struct.PossiblyMetamorphicMethodInvocation;

public abstract class MetamorphicProperty {
	public abstract String getName();

	public abstract MetamorphicInputProcessor getInputProcessor();

	public String getDescription() {
		return "Metamorphic property: " + getName();
	}

	private ArrayList<MethodInvocation> invocations = new ArrayList<MethodInvocation>();

	protected ArrayList<MethodInvocation> getInvocations() {
		return invocations;
	}

	public abstract PropertyResult propertyHolds();

	public void logExecution(MethodInvocation data) {
		invocations.add(data);
	}

	public static class PropertyResult {
		public boolean holds;
		public int supportingSize;
		public double probability;
		public int antiSupportingSize;
		public HashSet<MethodInvocation[]> supportingInvocations = new HashSet<MethodInvocation[]>();
		public HashSet<MethodInvocation[]> antiSupportingInvocations = new HashSet<MethodInvocation[]>();

		public enum Result {
			HOLDS, DOES_NOT_HOLD, UNKNOWN
		};

		public Result result;
		public Object data;
		public Class<? extends MetamorphicProperty> property;

		@Override
		public String toString() {
			String supportingInvocationsString = "[";
			String antiSupportingInvocationsString = "[";
			for (MethodInvocation[] ar : supportingInvocations) {
				supportingInvocationsString += "{" + ar[0] + "," + ar[1] + "}, ";
			}
			supportingInvocationsString = supportingInvocationsString.substring(0,
					(supportingInvocationsString.length() > 2 ? supportingInvocationsString.length() - 2 : 1))
					+ "]";
			for (MethodInvocation[] ar : antiSupportingInvocations) {
				antiSupportingInvocationsString += "{" + ar[0] + "," + ar[1] + "}, ";
			}
			antiSupportingInvocationsString = antiSupportingInvocationsString.substring(0,
					(antiSupportingInvocationsString.length() > 2 ? antiSupportingInvocationsString.length() - 2 : 1))
					+ "]";

			return "PropertyResult [holds=" + holds + ", supportingSize=" + supportingSize + ", probability=" + probability + ", antiSupportingSize="
					+ antiSupportingSize + ", supportingInvocations=" + supportingInvocationsString + ", antiSupportingInvocations="
					+ antiSupportingInvocationsString + ", result=" + result + ", property=" + property + "]";
		}

	}

	private Method method;

	protected Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	private Cloner cloner = new Cloner();

	public HashSet<PossiblyMetamorphicMethodInvocation> createChildren(MethodInvocation inv) {
		HashSet<PossiblyMetamorphicMethodInvocation> ret = new HashSet<PossiblyMetamorphicMethodInvocation>();
		boolean[] paramFlipping = new boolean[inv.params.length];
		
		ArrayList<boolean[]> combis = computeCombinations(paramFlipping);
		
		for (Object[] propertyParams : getInputProcessor().getBoundaryDefaultParameters()) {
			CombiLoop: for (boolean[] pset : combis) {
				PossiblyMetamorphicMethodInvocation child = new PossiblyMetamorphicMethodInvocation();
				child.parent = inv;
				child.params = new Object[inv.params.length];
				child.inputFlippedParams = new boolean[pset.length];
				child.propertyParams = new Object[pset.length][];
				boolean atLeastOneTrue = false;
				for (int i = 0; i < pset.length; i++) {
					atLeastOneTrue = atLeastOneTrue || pset[i];
					if (pset[i]) {
						child.inputFlippedParams[i] = true;
						try {
							child.propertyParams[i] = propertyParams;
							child.params[i] = getInputProcessor().apply((Object) cloner.deepClone(inv.params[i]), propertyParams);
						} catch (Exception ex) {
//							ex.printStackTrace();
							continue CombiLoop;
						}

					} else
						child.params[i] = cloner.deepClone(inv.params[i]);
				}
				if(atLeastOneTrue)
				{
					ret.add(child);
				}
			}
		}
		return ret;
	}
	private ArrayList<boolean[]> computeCombinations(boolean[] restOfVals) {
		return computeCombinations(new boolean[0], restOfVals);
	}
	
	private ArrayList<boolean[]> computeCombinations(boolean[] prefix, boolean[] restOfVals) {
		if(restOfVals.length == 0 )
		{
			ArrayList<boolean[]> ret = new ArrayList<boolean[]>();
			ret.add(prefix);
			return ret;
		}
		else {
			ArrayList<boolean[]> newList = new ArrayList<boolean[]>();
			newList.addAll(prependToEach(appendElement(prefix,false), computeCombinations(removeFirstElement(restOfVals))));
			newList.addAll(prependToEach(appendElement(prefix,true), computeCombinations(removeFirstElement(restOfVals))));
			return newList;
		}

	}
	private boolean[] appendElement(boolean[] ar, boolean v)
	{
		boolean[] ret = new boolean[ar.length + 1];
		for(int i =0;i<ar.length;i++)
		{
			ret[i]=ar[i];
		}
		ret[ar.length] = v;
		return ret;
	}
	private boolean[] removeFirstElement(boolean[] ar)
	{
		boolean[] ret = new boolean[ar.length -1];
		for(int i =1;i<ar.length;i++)
		{
			ret[i-1]=ar[i];
		}
		return ret;
	}
	private ArrayList<boolean[]> prependToEach(boolean[] prefix, ArrayList<boolean[]> vals) {
		ArrayList<boolean[]> ret = new ArrayList<boolean[]>();
		for (boolean[] o : vals) {
			boolean[] r = new boolean[o.length + prefix.length];
			for(int i = 0; i<prefix.length;i++)
			{
				r[i] = prefix[i];
			}
			for(int i = 0; i<o.length;i++)
			{
				r[i+prefix.length]=o[i];
			}
			ret.add(r);
		}
		return ret;
	}

}
