package edu.columbia.cs.psl.mountaindew.runtime;

import java.util.ArrayList;
import java.util.HashMap;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.mountaindew.absprop.MetamorphicProperty;
import edu.columbia.cs.psl.mountaindew.absprop.MetamorphicProperty.PropertyResult;
import edu.columbia.cs.psl.mountaindew.stats.Correlationer;
import edu.columbia.cs.psl.mountaindew.struct.MethodProfile;

public class MethodProfiler {
	
	private ArrayList<MethodProfile> profiles = new ArrayList<MethodProfile>();
		
	public void addMethodProfile(MethodInvocation ori, MethodInvocation trans, PropertyResult result) {
		MethodProfile mProfile = new MethodProfile(ori, trans, result);
		
		//Target on single input and single output first
		/*mProfile.setOriInOriOut(Correlationer.calCorrelation(ori.getParams(), ori.getReturnValue())[0]);
		mProfile.setTransInTransOut(Correlationer.calCorrelation(trans.getParams(), trans.getReturnValue())[0]);
		mProfile.setOriInTransIn(Correlationer.calCorrelation(ori.getParams(), trans.getParams())[0]);
		mProfile.setOriOutTransOut(Correlationer.calCorrelation(ori.getReturnValue(), trans.getReturnValue())[0]);*/
		
		this.profiles.add(mProfile);
	}
	
	public ArrayList<MethodProfile> getMethodProfiles() {
		return this.profiles;
	}
}
