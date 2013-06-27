package edu.columbia.cs.psl.mountaindew.struct;

import java.lang.reflect.Array;

import java.util.Collection;
import java.util.Iterator;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.mountaindew.property.MetamorphicProperty;
import edu.columbia.cs.psl.mountaindew.property.MetamorphicProperty.PropertyResult;

public class MethodProfile {
	
	private MethodInvocation ori;
	
	private MethodInvocation trans;
	
	/*private double oriInOriOut;
	
	private double transInTransOut;
	
	private double oriInTransIn;
	
	private double oriOutTransOut;*/
	
	private PropertyResult result;
	
	public static String interpretParams(Object params) {
		StringBuilder sBuilder = new StringBuilder();
		if (params.getClass().isArray()) {
			sBuilder.append("[");
			for (int i = 0; i < Array.getLength(params); i++) {
				//sBuilder.append(((Double)Array.get(params, i)).toString());
				sBuilder.append(String.valueOf((Number)Array.get(params, i)));
				sBuilder.append(" ");
			}
			sBuilder.append("]");
		} else if (Collection.class.isAssignableFrom(params.getClass())) {
			Iterator paramIT = ((Collection)params).iterator();
			sBuilder.append("[");
			while(paramIT.hasNext()) {
				//sBuilder.append(((Double)paramIT.next()).toString());
				sBuilder.append(String.valueOf((Number)paramIT.next()));
				sBuilder.append(" ");
			}
			sBuilder.append("]");
		} else if (Number.class.isAssignableFrom(params.getClass())) {
			sBuilder.append("[");
			sBuilder.append(String.valueOf((Number)params));
			sBuilder.append("]");
		};
		
		return sBuilder.toString();
	}
	
	public static String delim = ",";
	
	public static String newLine = "\n";
	
	public MethodProfile(MethodInvocation ori, MethodInvocation trans, PropertyResult result) {
		this.ori = ori;
		this.trans = trans;
		this.result = result;
	}
	
	/*public void setOriInOriOut(double correlation) {
		this.oriInOriOut = correlation;
	}
	
	public double getOriInOriOut() {
		return this.oriInOriOut;
	}
	
	public void setTransInTransOut(double correlation) {
		this.transInTransOut = correlation;
	}
	
	public double getTransInTransOut() {
		return this.transInTransOut;
	}
	
	public void setOriInTransIn(double correlation) {
		this.oriInTransIn = correlation;
	}
	
	public double getOriInTransIn() {
		return this.oriInTransIn;
	}
	
	public void setOriOutTransOut(double correlation) {
		this.oriOutTransOut = correlation;
	}
	
	public double getOriOutTransOut() {
		return this.oriOutTransOut;
	}*/
	
	public MethodInvocation getOri() {
		return this.ori;
	}
	
	public MethodInvocation getTrans() {
		return this.trans;
	}
	
	public PropertyResult getResult() {
		return this.result;
	}
	
	/**
	 * Only transformed invocation has frontend transformer
	 * @return
	 */
	public String getFrontend() {
		return trans.getFrontend();
	}
	
	/**
	 * Only transformed invocation has backend checker
	 * @return
	 */
	public String getBackend() {
		return trans.getBackend();
	}
	
	public String toString() {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(this.ori.getMethod().getName() + delim);
		//Target on single input first
		sBuilder.append(interpretParams(ori.getParams()[0]) + delim);
		sBuilder.append(interpretParams(ori.getReturnValue()) + delim);
		sBuilder.append(interpretParams(trans.getParams()[0]) + delim);
		sBuilder.append(interpretParams(trans.getReturnValue()) + delim);
		/*sBuilder.append(this.oriInOriOut + delim);
		sBuilder.append(this.transInTransOut + delim);
		sBuilder.append(this.oriInTransIn + delim);
		sBuilder.append(this.oriOutTransOut + delim);
		sBuilder.append(this.result.property.toString() + delim);*/
		sBuilder.append(trans.getFrontend() + delim);
		sBuilder.append(trans.getBackend() + delim);
		sBuilder.append(this.result.holds + newLine);
		return sBuilder.toString();
	}

}
