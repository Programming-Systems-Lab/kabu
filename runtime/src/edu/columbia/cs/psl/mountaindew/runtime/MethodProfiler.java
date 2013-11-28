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
	
	private ArrayList<MethodProfile> holdProfiles = new ArrayList<MethodProfile>();
	
	private HashMap<String, Boolean> holdMap = new HashMap<String, Boolean>();
		
	public void addMethodProfile(MethodInvocation ori, MethodInvocation trans, PropertyResult result) {
		MethodProfile mProfile = new MethodProfile(ori, trans, result);
		this.profiles.add(mProfile);
	}
	
	public void addHoldMethodProfile(MethodInvocation ori, MethodInvocation trans, PropertyResult result) {
		MethodProfile holdProfile = new MethodProfile(ori, trans, result);
		
		if (holdProfile.getResult().holds) {
			this.holdProfiles.add(holdProfile);
		}
	}
	
	public ArrayList<MethodProfile> getMethodProfiles() {
		return this.profiles;
	}
	
	public ArrayList<MethodProfile> getHoldMethodProfiles() {
		//return this.holdProfiles;
		this.summarizeProfilers();
		
		System.out.println("Check hold map: " + this.holdMap);
		ArrayList<MethodProfile> ret = new ArrayList<MethodProfile>();
		String identifier = null;
		for (MethodProfile mp: this.profiles) {
			identifier = mp.getFrontend() + mp.getBackend() + mp.getResult().stateItem;
			
			if (this.holdMap.get(identifier)) {
				ret.add(mp);
			}
		}
		return ret;
	}
	
	private void summarizeProfilers() {
		String identifier = null;
		for (MethodProfile tmp: this.profiles) {
			identifier = tmp.getFrontend() + tmp.getBackend() + tmp.getResult().stateItem;

			if (!holdMap.keySet().contains(identifier)) {
				holdMap.put(identifier, tmp.getResult().holds);
			} else {
				holdMap.put(identifier, holdMap.get(identifier) && tmp.getResult().holds);
			}
		}
	}
}
