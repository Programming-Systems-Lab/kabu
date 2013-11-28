package edu.columbia.cs.psl.mountaindew.struct;

import java.util.List;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;

public class TransClassTuple {
	
	private Class<? extends MetamorphicInputProcessor> transClass;
		
	private List<Number> times;
		
	public TransClassTuple(Class<? extends MetamorphicInputProcessor> transClass, List<Number> times) {
		this.transClass = transClass;
		this.times = times;
	}
		
	public Class<? extends MetamorphicInputProcessor> getTransClass() {
		return this.transClass;
	}
		
	public List<Number> getTimes() {
		return this.times;
	}
		
	public void setTransClass(Class<? extends MetamorphicInputProcessor> transClass) {
		this.transClass = transClass;
	}
		
	public void setTimes(List<Number> times) {
		this.times = times;
	}
		
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TransClassTuple))
			return false;
			
		TransClassTuple tmpTuple = (TransClassTuple)obj;
			
		if (!tmpTuple.getTransClass().equals(this.getTransClass()))
			return false;
			
		if (!tmpTuple.getTimes().equals(this.getTimes()))
			return false;
			
		return true;
	}
		
	@Override
	public int hashCode() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getTransClass().getName());
		for (Number num: this.times) {
			sb.append(num);
		}
			
		return sb.toString().hashCode();
	}
}
