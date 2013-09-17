package edu.columbia.cs.psl.mountaindew.state;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;


import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.DependentProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.mountaindew.absprop.MetamorphicProperty;
import edu.columbia.cs.psl.mountaindew.adapter.AbstractAdapter;
import edu.columbia.cs.psl.mountaindew.struct.MConfig;
import edu.columbia.cs.psl.mountaindew.struct.PossiblyMetamorphicMethodInvocation;
import edu.columbia.cs.psl.mountaindew.util.MetamorphicConfigurer;

public class State {
	
	private static String propFilePath = "config/metamorphic.property";
	
	private static String stateKey = "States";
	
	private Method method;
	
	private MetamorphicConfigurer mconfigurer;
	
	private HashMap<String, HashSet<String>> stateDefinition = new HashMap<String, HashSet<String>>();
	
	private HashSet<MetamorphicProperty> checkers = new HashSet<MetamorphicProperty>();
	
	public State(Method method, MetamorphicConfigurer mconfigurer) {
		this.method = method;
		this.mconfigurer = mconfigurer;
	}
	
	public void addChecker(MetamorphicProperty checker) {
		checker.getTargetAdapter().setStateDefinition(this.stateDefinition);
		this.checkers.add(checker);
	}
	
	public HashSet<MetamorphicProperty> getAllCheckers() {
		return this.checkers;
	}
	
	public HashSet<String> getInterestedFieldsByClass(String className) {
		return this.stateDefinition.get(className);
	}
	
	public HashMap<String, HashSet<String>> getStateDefintion() {
		return this.stateDefinition;
	}
	
	public void defineState() {
		List<MConfig.StateItem> stateItems = this.mconfigurer.getStates();
		
		for (MConfig.StateItem tmpItem: stateItems) {
			String tmpClassName = tmpItem.getClassName();
			HashSet<String> tmpFields = tmpItem.getFieldNames();
			
			if (!this.stateDefinition.keySet().contains(tmpClassName)) {
				this.stateDefinition.put(tmpClassName, tmpFields);
			}
		}
	}
}
