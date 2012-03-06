package edu.columbia.cs.psl.mountaindew.property;

import java.lang.reflect.Method;
import java.util.ArrayList;

import edu.columbia.cs.psl.metamorphic.processor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.struct.MethodInvocation;

public abstract class MetamorphicProperty {
	public abstract String getName();
	public abstract MetamorphicInputProcessor getInputProcessor();
	
	public String getDescription()
	{
		return "Metamorphic property: " + getName();
	}
	private ArrayList<MethodInvocation> invocations = new ArrayList<MethodInvocation>();
	
	protected ArrayList<MethodInvocation> getInvocations() {
		return invocations;
	}
	public abstract boolean propertyApplies();
	public abstract PropertyResult propertyHolds();
	public void logExecution(MethodInvocation data)
	{
		invocations.add(data);
	}
	public static class PropertyResult
	{
		public boolean holds;
		public int supportingSize;
		public double probability;
		public enum Result {HOLDS,DOES_NOT_HOLD,UNKNOWN};
		public Result result;
		public Object data;
		public Class<? extends MetamorphicProperty> property;
		@Override
		public String toString() {
			return "PropertyResult [holds=" + holds + ", supportingSize="
					+ supportingSize + ", probability=" + probability
					+ ", result=" + result + ", data=" + data + ", property="
					+ property + "]";
		}
		

	}
	
	private Method method;
	protected Method getMethod()
	{
		return method;
	}
	public void setMethod(Method method) {
		this.method=method;
	}
	

	
}
