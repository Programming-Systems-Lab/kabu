package edu.columbia.cs.psl.mountaindew.runtime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import com.google.gson.stream.JsonWriter;
import com.rits.cloning.Cloner;

import edu.columbia.cs.psl.invivo.runtime.AbstractInterceptor;
import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.runtime.MetamorphicInputProcessorGroup;
import edu.columbia.cs.psl.mountaindew.absprop.MetamorphicProperty;
import edu.columbia.cs.psl.mountaindew.absprop.MetamorphicProperty.PropertyResult;
import edu.columbia.cs.psl.mountaindew.adapter.AbstractAdapter;
import edu.columbia.cs.psl.mountaindew.adapter.AdapterLoader;
import edu.columbia.cs.psl.mountaindew.state.State;
import edu.columbia.cs.psl.mountaindew.stats.Correlationer;
import edu.columbia.cs.psl.mountaindew.struct.MConfig;
import edu.columbia.cs.psl.mountaindew.struct.MConfig.TransTuple;
import edu.columbia.cs.psl.mountaindew.struct.MethodProfile;
import edu.columbia.cs.psl.mountaindew.struct.MConfig.StateItem;
import edu.columbia.cs.psl.mountaindew.struct.TransClassTuple;
import edu.columbia.cs.psl.mountaindew.util.MetamorphicConfigurer;


/**
 * Each intercepted object will have its _own_ Interceptor instance.
 * That instance will stick around for the lifetime of the intercepted object.
 * 
 * NB if you want to keep a list of these Interceptors somewhere statically,
 * you probably want to use a WeakHashMap so as to not create memory leaks
 * 
 * @author jon
 *
 */
public class Interceptor extends AbstractInterceptor {
	private static String header = 
			"Method name,ori_input,ori_output,trans_input,trans_output,frontend_transformer,backend_checker,Holds\n";
	private static String holdHeader =
			"Method name, frontend_transformer,backend_checker\n";
	//private static String profileRoot = "/Users/mike/Documents/metamorphic-projects/mountaindew/tester/profiles/";
	private static String profileRoot = "profiles/";
	private static String configString = "config/mutant.property";
	private static String metaConfigString = "config/metamorphic.property";
	private static String metaJson = "config/mconfig.json";
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSS");
	private MetamorphicConfigurer mConfigurer = new MetamorphicConfigurer();
	private HashMap<Method, HashSet<MetamorphicProperty>> properties = new HashMap<Method, HashSet<MetamorphicProperty>>();
	private HashSet<Class<? extends MetamorphicProperty>> propertyPrototypes;
	private HashSet<Class<? extends MetamorphicInputProcessor>> processorPrototypes;
	private HashSet<Class<? extends MetamorphicInputProcessor>> nonValueChangeProcessorPrototypes;
	private HashMap<String, HashMap<Class<? extends MetamorphicProperty>, HashSet<TransClassTuple>>> methodCombinations =
			new HashMap<String, HashMap<Class<? extends MetamorphicProperty>, HashSet<TransClassTuple>>>();
	private HashMap<String, HashMap<MConfig.TransTuple, List<MConfig.StateItem>>> configMap;
	private Class<? extends AbstractAdapter> targetAdapter;
	private HashMap<Integer, MethodInvocation> invocations = new HashMap<Integer, MethodInvocation>();
	private Integer invocationId = 0;
	private List<MethodProfiler> profilerList = new ArrayList<MethodProfiler>();
//	private Cloner cloner = new Cloner();
	private String calleeName;
	private String timeTag = "default";
	private String configRoot = "config";
	private String combKey = "Combinations";
	private String adapterKey = "Adapter";
	private String stopKey = "Stop";
	private String holdTag = "Holds";
	private boolean isGlobal = true;
	
	public Interceptor(Object intercepted) {
		super(intercepted);
		System.out.println("Interceptor created");
		mConfigurer.loadConfiguration(metaJson);
		configMap = mConfigurer.getConfigMap();
		propertyPrototypes = MetamorphicObserver.getInstance().registerInterceptor(this);
		processorPrototypes = MetamorphicInputProcessorGroup.getInstance().getProcessors();
		//propertyPrototypes = this.filterCheckers(this.mConfigurer, MetamorphicObserver.getInstance().registerInterceptor(this));
		//processorPrototypes = this.filterTransformers(this.mConfigurer, MetamorphicInputProcessorGroup.getInstance().getProcessors());
		nonValueChangeProcessorPrototypes = MetamorphicInputProcessorGroup.getInstance().getNonValueChangeProcessors();
		targetAdapter = this.getAdapter();
		this.getTimeTag();
	}
	
	private HashSet<Class<? extends MetamorphicProperty>> filterCheckers() {
		HashSet<Class<? extends MetamorphicProperty>> ret = new HashSet<Class<? extends MetamorphicProperty>>();
		Set<String> selectedClasses = this.configMap.keySet();
		
		for (Class<? extends MetamorphicProperty> tmpChecker: this.propertyPrototypes) {
			if (selectedClasses.contains(tmpChecker.getName())) {
				ret.add(tmpChecker);
			}
		}
		
		return ret;
	}
	
	private HashSet<TransClassTuple> filterTransformers(HashMap<MConfig.TransTuple, List<StateItem>>tsMap) {
		HashSet<TransClassTuple> ret = new HashSet<TransClassTuple>();
		/*Set<String> selectedClasses = tsMap.keySet();
				
		for (Class<? extends MetamorphicInputProcessor> tmpTrans: this.processorPrototypes) {
			if (selectedClasses.contains(tmpTrans.getName())) {
				ret.add(tmpTrans);
			}
		}*/
		
		TransClassTuple tct = null;
		for (Class<? extends MetamorphicInputProcessor> tmpTrans: this.processorPrototypes) {
			for (MConfig.TransTuple tt: tsMap.keySet()) {
				if (tmpTrans.getName().equals(tt.getTransformer())) {
					tct = new TransClassTuple(tmpTrans, tt.getTimes());
					ret.add(tct);
				}
			}
		}
		
		return ret;
	}
		
	private HashSet<Class<? extends MetamorphicProperty>> filterCheckers(MetamorphicConfigurer mConfigurer, HashSet<Class<? extends MetamorphicProperty>> allCheckers) {
		List<String> selectedClasses = mConfigurer.getCheckerNames();
		HashSet<Class<? extends MetamorphicProperty>> ret = new HashSet<Class<? extends MetamorphicProperty>>();
		
		for (Class<? extends MetamorphicProperty> tmpChecker: allCheckers) {
			if (selectedClasses.contains(tmpChecker.getName())) {
				ret.add(tmpChecker);
			}
		}
		
		return ret;
	}
	
	private HashSet<Class<? extends MetamorphicInputProcessor>> filterTransformers(MetamorphicConfigurer mConfigurer, HashSet<Class<? extends MetamorphicInputProcessor>> allTransformers) {
		List<String> selectedClasses = mConfigurer.getTransformerNames();
		HashSet<Class<? extends MetamorphicInputProcessor>> ret = new HashSet<Class<? extends MetamorphicInputProcessor>>();
		
		for (Class<? extends MetamorphicInputProcessor> tmpProcessor: allTransformers) {
			if (selectedClasses.contains(tmpProcessor.getName())) {
				ret.add(tmpProcessor);
			}
		}
		
		return ret;
	}
	
	private HashMap<Class<? extends MetamorphicProperty>, HashSet<Class<? extends MetamorphicInputProcessor>>> constructMethodSpecificConfiguration(String rawCombinations) {
		List<String> combinationStrings = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(rawCombinations, ",");
		while(st.hasMoreTokens()) {
			combinationStrings.add(st.nextToken());
		}
		
		HashMap<Class<? extends MetamorphicProperty>, HashSet<Class<? extends MetamorphicInputProcessor>>> checkerTransformerMap = 
				new HashMap<Class<? extends MetamorphicProperty>, HashSet<Class<? extends MetamorphicInputProcessor>>>();
		
		String checker = null;
		String transformer = null;
		Class<? extends MetamorphicProperty> checkerClass = null;
		Class<? extends MetamorphicInputProcessor> transformerClass = null;
		for (String rawComb: combinationStrings) {
			rawComb = rawComb.substring(1, rawComb.length()-1);			
			st = new StringTokenizer(rawComb, "+");
			transformer = MetamorphicConfigurer.getTransformerFullName(st.nextToken());
			checker = MetamorphicConfigurer.getCheckerFullName(st.nextToken());
			
			for (Class<? extends MetamorphicProperty> tmpChecker: this.propertyPrototypes) {
				if (tmpChecker.getName().equals(checker)) {
					checkerClass = tmpChecker;
				}
			}
			
			for (Class<? extends MetamorphicInputProcessor> tmpProcessor: this.processorPrototypes) {
				if (tmpProcessor.getName().equals(transformer)) {
					transformerClass = tmpProcessor;
				}
			}
			
			if (checkerTransformerMap.keySet().contains(checkerClass)) {
				checkerTransformerMap.get(checkerClass).add(transformerClass);
			} else {
				HashSet<Class<? extends MetamorphicInputProcessor>> tClassSet = 
						new HashSet<Class<? extends MetamorphicInputProcessor>>();
				tClassSet.add(transformerClass);
				
				checkerTransformerMap.put(checkerClass, tClassSet);
			}
		}
		
		return checkerTransformerMap;
	}
	
	private Class<? extends AbstractAdapter> getAdapter() {
		String targetClass = mConfigurer.getAdapterClassName();
		Class<? extends AbstractAdapter> targetAdapter = AdapterLoader.loadClass(targetClass);
		return targetAdapter;
	}
	
	private void getTimeTag() {
		File mutantConfig = new File(configString);
		
		if (!mutantConfig.exists()) {
			System.err.println("Mutant configuration file does not exist");
		}
		
		FileInputStream fs;
		try {
			fs = new FileInputStream(mutantConfig);
			
			Properties mutantProperty = new Properties();
			mutantProperty.load(fs);
			
			String tmpTag = mutantProperty.getProperty("timetag");
			
			if (tmpTag == null || tmpTag.isEmpty()) {
				System.err.println("Time tag in mutant configuration file is empty");
			} else {
				this.timeTag = tmpTag;
			}
			
			fs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int onEnter(Object callee, Method method, Object[] params)
	{
		if(isChild(callee))
			return -1;
		int retId = 0;
		synchronized(invocationId)
		{
			invocationId++;
			retId = invocationId;
		}
		
		if (!properties.containsKey(method)) {			
			properties.put(method, new HashSet<MetamorphicProperty>());
			
			HashMap<Class<? extends MetamorphicProperty>, HashSet<TransClassTuple>> checkerTransformerMap = 
					new HashMap<Class<? extends MetamorphicProperty>, HashSet<TransClassTuple>>();
			
			//String methodPFile = "config/" + method.getName() + ".property";
			String methodJson = "config/" + method.getName() + ".json";
			File tmpFile = new File(methodJson);
			
			if (tmpFile.exists()) {
				this.mConfigurer.loadConfiguration(methodJson);
				this.configMap = this.mConfigurer.getConfigMap();
				this.isGlobal = false;
			}
			
			System.out.println("Confirm configuration: " + this.configMap);
			
			for (Class<? extends MetamorphicProperty> tmpProperty: this.filterCheckers()) {
				checkerTransformerMap.put(tmpProperty, this.filterTransformers(this.configMap.get(tmpProperty.getName())));
			}
			
			this.methodCombinations.put(method.getName(), checkerTransformerMap);

			for (Class<? extends MetamorphicProperty> c: checkerTransformerMap.keySet()) {
				//HashMap<String, List<MConfig.StateItem>> tsMap = this.configMap.get(c.getName());
				HashMap<MConfig.TransTuple, List<MConfig.StateItem>> tsMap = this.configMap.get(c.getName());
				try {
					MetamorphicProperty p = c.newInstance();
					p.setMethod(method);
					p.setInputProcessors(checkerTransformerMap.get(c));
					p.setTargetAdapter(this.targetAdapter);
					p.setNonValueChangeInputProcessors(this.nonValueChangeProcessorPrototypes);
					p.loadInputProcessors();
					
					if (!this.isGlobal) {
						List<StateItem> tmpItemList;
						for (MetamorphicInputProcessor tmpProcessor: p.getInputProcessors()) {
							
							//Find correct trans tuple
							TransTuple targetTuple = null;
							for (TransTuple tmp: tsMap.keySet()) {
								if (tmp.getTransformer().equals(tmpProcessor.getClass().getName())) {
									targetTuple = tmp;
									break;
								}
							}
							
							//tmpItemList = tsMap.get(tmpProcessor.getClass().getName());
							tmpItemList = tsMap.get(targetTuple);
							HashMap<String, HashSet<String>> classMap = new HashMap<String, HashSet<String>>();
							for (StateItem tmpItem: tmpItemList) {
								classMap.put(tmpItem.getClassName(), tmpItem.getFieldNames());
							}
							tmpProcessor.setClassMap(classMap);
						}
					}
					
					properties.get(method).add(p);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			/*MethodInvocation inv = new MethodInvocation();
			inv.params = deepClone(params);
			inv.method = method;
			inv.callee = getInterceptedObject();
			invocations.put(retId, inv);
			
			this.calleeName = callee.getClass().getName();
			int namePos = this.calleeName.lastIndexOf(".");
			this.calleeName = this.calleeName.substring(namePos+1, calleeName.length());*/
		}
		
		MethodInvocation inv = new MethodInvocation();
		//inv.params = params;
		//In case the input param is also the output of the method
		inv.params = deepClone(params);
		inv.method = method;
		inv.callee = getInterceptedObject();
		invocations.put(retId, inv);
		
		this.calleeName = callee.getClass().getName();
		int namePos = this.calleeName.lastIndexOf(".");
		this.calleeName = this.calleeName.substring(namePos+1, calleeName.length());
		
		ArrayList<MethodInvocation> children = new ArrayList<MethodInvocation>();
		for(MetamorphicProperty p : properties.get(inv.method))
		{
			for(MethodInvocation child : p.createChildren(inv))
			{
				//System.out.println("Check children frontend backend: " + child.getFrontend() + " " + child.getBackend());
				child.callee = deepClone(inv.callee);
				child.method = inv.method;
				children.add(child);
				child.thread = createChildThread(child);
				child.thread.start();
				try {
					child.thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		inv.children = new MethodInvocation[children.size()];
		inv.children = children.toArray(inv.children);
		//For getting the correct local variable map
		inv.thread = Thread.currentThread();
		
		System.out.println("Method name " + inv.getMethod().getName());
		System.out.println("Children size: " + inv.children.length);
		System.out.println("Callee: " + callee.getClass().getName());
		System.out.println("Thread id: " + inv.thread.getId());
		
		this.reportTransformerChecker();
		return retId;
	}
	
	public void onExit(Object val, int op, int id)
	{
		if(id < 0)
			return;
		MethodInvocation inv = invocations.remove(id);
		inv.returnValue = val;
		
		/*ArrayList<MethodInvocation> children = new ArrayList<MethodInvocation>();
		for (MetamorphicProperty p: properties.get(inv.method)) {
			for (MethodInvocation child: p.createChildren(inv)) {
				child.callee = deepClone(inv.callee);
				child.method = inv.method;
				children.add(child);
				child.thread = createChildThread(child);
				child.thread.start();
				try {
					child.thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} 
		
		inv.children = new MethodInvocation[children.size()];
		inv.children = children.toArray(inv.children);
		
		System.out.println("Method name " + inv.getMethod().getName());
		System.out.println("Children size: " + inv.children.length);
		System.out.println("Callee: " + this.calleeName);
		
		this.reportTransformerChecker();*/
		
		for (MethodInvocation inv2: inv.children) {
			try {
				inv2.thread.join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			System.out.println("Log children");
			for (MetamorphicProperty p: properties.get(inv2.method)) {
				p.logExecution(inv2);
			}
		}
		
		for (MetamorphicProperty p: properties.get(inv.method)) {
			p.logExecution(inv);
		}
				
		/*for(MethodInvocation inv2 : inv.children)
		{
			try {
				inv2.thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Log children");
			for(MetamorphicProperty p : properties.get(inv2.method))
			{
				p.logExecution(inv2);
			}
		}
		
		for(MetamorphicProperty p : properties.get(inv.method))
		{
			p.logExecution(inv);
		}*/
				
		//Try to alleviate heap space issue
		System.gc();
	}
	
	public void reportPropertyResults()
	{
		for(Method m : properties.keySet())
		{
			System.out.println(m);
			for(MetamorphicProperty p : properties.get(m))
			{
				PropertyResult r = p.propertyHolds();
				System.out.println(r);
				profilerList.add(p.getMethodProfiler());
			}
		}
	}
	
	public void reportPropertyResultList() {
		for (Method m: properties.keySet()) {
			System.out.println(m);
			
			for (MetamorphicProperty p: properties.get(m)) {
				List<PropertyResult> resultList = p.propertiesHolds();
				
				for (PropertyResult result: resultList) {
					System.out.println(result + "\n");
				}
				profilerList.add(p.getMethodProfiler());
			}
		}
		
		/*for (Method m: properties.keySet()) {
			System.out.println(m);
			
			for (MetamorphicProperty p: properties.get(m)) {
				List<PropertyResult> resultList = p.propertiesHolds();
				
				for (PropertyResult result: resultList) {
					System.out.println(result + "\n");
				}
				profilerList.add(p.getMethodProfiler());
			}
		}*/
	}
	
	public void exportMethodProfile() {
		
		StringBuilder sBuilder = new StringBuilder();
		
		sBuilder.append(header);
		
		/*MethodProfiler tmpProfiler;
		while (!this.profilerList.isEmpty()) {
			tmpProfiler = this.profilerList.remove(0);
			for (MethodProfile mProfile: tmpProfiler.getMethodProfiles()) {
				sBuilder.append(mProfile.toString());
			}
			System.gc();
		}*/
		
		for (MethodProfiler mProfiler: this.profilerList) {
			for (MethodProfile mProfile: mProfiler.getMethodProfiles()) {
				sBuilder.append(mProfile.toString());
			}
		}
		//System.out.println("Test export string: " + sBuilder.toString());
		
		File rootDir = new File(profileRoot + this.timeTag);
		
		String absPath = "";
		if (rootDir.exists() && rootDir.isDirectory()) {
			absPath = rootDir.getAbsolutePath() + "/";
			System.out.println("Confirm root directory for exporting: " + absPath);
		} else if (!rootDir.exists()) {
			System.out.println("Profile directory does not exists. Create one...");
			boolean success = rootDir.mkdir();
			
			if (success) {
				System.out.println("Profile directory creation succeeds.");
				absPath = rootDir.getAbsolutePath() + "/";
				System.out.println("Confirm root direcotry for exporting: " + absPath);
			} else {
				System.out.println("Profile directory creation fails");
				return ;
			}
		} else {
			System.out.println("For some reason, profile directory creation fails.");
			return ;
		}
		
		try {
			FileWriter fWriter = new FileWriter(absPath + this.calleeName + formatter.format(new Date()) + ".csv");
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			bWriter.write(sBuilder.toString());
			bWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void exportHoldStates() {
		HashMap<String, HashSet<MConfig.MethodStateItem>> records = this.collectMethodStates();
		
		JsonWriter jw = null;
		
		try {
			for (String methodName: records.keySet()) {
				HashSet<MConfig.MethodStateItem> mStateSet = records.get(methodName);
				
				String outputPath = this.configRoot + "/" + methodName + ".json";
				
				jw = new JsonWriter(new FileWriter(outputPath));
				jw.setIndent("	");
				jw.beginObject();
				jw.name("methodConfig");
				
				jw.beginObject();
				jw.name("Adapter").value(this.mConfigurer.getAdapterName());
				jw.name("HoldStates");
				
				jw.beginArray();
				for (MConfig.MethodStateItem tmpItem: mStateSet) {
					jw.beginObject();
					jw.name("Checker").value(tmpItem.getChecker().replace("C:", ""));
					//jw.name("Transformer").value(tmpItem.getTransformer().getTransformer().replace("T:", ""));
					
					jw.name("Transformer");
					jw.beginObject();
					jw.name(tmpItem.getTransformer().getTransformer().replace("T:", ""));
					jw.beginArray();
					for (Number num: tmpItem.getTransformer().getTimes()) {
						jw.value(num);
					}
					jw.endArray();
					jw.endObject();
					
					jw.name("ClassSpec");
					
					jw.beginArray();
					for (StateItem tmpSI: tmpItem.getStateItems()) {
						jw.beginObject();
						jw.name("ClassName").value(tmpSI.getClassName());
						jw.name("FieldNames");
						
						jw.beginArray();
						for (String tmpField: tmpSI.getFieldNames()) {
							jw.value(tmpField);
						}
						jw.endArray();
						
						jw.endObject();
					}
					jw.endArray();
					
					jw.endObject();
				}
				jw.endArray();
				jw.endObject();
				
				jw.endObject();
				
				jw.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public HashMap<String, HashSet<MConfig.MethodStateItem>> collectMethodStates() {
		HashMap<String, HashSet<MConfig.MethodStateItem>> records = 
				new HashMap<String, HashSet<MConfig.MethodStateItem>>();
		for (MethodProfiler mProfiler: this.profilerList) {		
			for (MethodProfile mProfile: mProfiler.getHoldMethodProfiles()) {
				String methodName = mProfile.getOri().getMethod().getName();
				
				String checker = mProfile.getBackend();
				String transformer = mProfile.getFrontend();
				String stateItem = mProfile.getResult().stateItem;
				
				StringTokenizer st = new StringTokenizer(stateItem, ":");
				String className = null;
				String fieldName = null;
				int count = 0;
				while(st.hasMoreTokens()) {
					if (count == 0) {
						className = st.nextToken();
						count++;
					} else if (count == 1) {
						fieldName = st.nextToken();
						count = 0;
					}
				}
				
				if (records.keySet().contains(methodName)) {
					boolean foundMS = false;
					for (MConfig.MethodStateItem ms: records.get(methodName)) {
						if (ms.getChecker().equalsIgnoreCase(checker) && ms.getTransformer().getTransformer().equalsIgnoreCase(transformer)) {
							boolean foundSI = false;
							for (StateItem tmpSI: ms.getStateItems()) {
								if (tmpSI.getClassName().equalsIgnoreCase(className)) {
									tmpSI.addFieldName(fieldName);
									foundSI = true;
								}
							}
							
							if (!foundSI) {
								StateItem si = new StateItem();
								si.setClassName(className);
								si.addFieldName(fieldName);
								ms.addStateItem(si);
							}
							foundMS = true;
						}
					}
					
					TransTuple tmpTrans = null;
					if (!foundMS) {
						MConfig.MethodStateItem ms = new MConfig.MethodStateItem();
						ms.setChecker(checker);
						
						//Empty list for users to fill in
						tmpTrans = new TransTuple(transformer, new ArrayList<Number>());
						ms.setTransformer(tmpTrans);
						
						StateItem si = new StateItem();
						si.setClassName(className);
						si.addFieldName(fieldName);
						ms.addStateItem(si);
						
						records.get(methodName).add(ms);
					}
					
				} else {
					MConfig.MethodStateItem ms = new MConfig.MethodStateItem();
					ms.setChecker(checker);
					
					TransTuple transTuple = new TransTuple(transformer, new ArrayList<Number>());
					ms.setTransformer(transTuple);
					
					StateItem si = new StateItem();
					si.setClassName(className);
					si.addFieldName(fieldName);
					ms.addStateItem(si);
					
					HashSet<MConfig.MethodStateItem> tmpSet = new HashSet<MConfig.MethodStateItem>();
					tmpSet.add(ms);
					
					records.put(methodName, tmpSet);
				}
			}
		}
		return records;
	}
	
	public void exportHoldMethodProfile() {
		//Need to retrieve all methods first
		HashMap<String, Boolean> allMethodMap = new HashMap<String, Boolean>();
		List<MethodProp> methodPropList = new ArrayList<MethodProp>();
		List<MethodProfile> tmpHoldList;
		for (MethodProfiler mProfiler: this.profilerList) {
			for (MethodProfile mProfile: mProfiler.getMethodProfiles()) {
				allMethodMap.put(mProfile.getOri().getMethod().getName(), false);
			}
			
			String methodName;
			for (MethodProfile mProfile: mProfiler.getHoldMethodProfiles()) {
				methodName = mProfile.getOri().getMethod().getName();
				
				boolean found = false;
				for (MethodProp tmpProp: methodPropList) {
					if (tmpProp.getMethodName().equals(methodName)) {
						tmpProp.addCombination(mProfile.getFrontend(), mProfile.getBackend());
						tmpProp.setAdapterString(this.mConfigurer.getAdapterName());
						found = true;
					}
				}
				
				if (!found) {
					MethodProp mProp = new MethodProp(methodName);
					mProp.addCombination(mProfile.getFrontend(), mProfile.getBackend());
					mProp.setAdapterString(this.mConfigurer.getAdapterName());
					methodPropList.add(mProp);
				}
			}
		}
		
		try {
			Properties prop = new Properties();
			String propertyFileName;
			File propertyFile;
			for (MethodProp tmpProp: methodPropList) {
				System.out.println("Start to load property");
				propertyFileName = this.configRoot + "/" + tmpProp.getMethodName() + ".property";
				propertyFile = new File(propertyFileName);
				
				if (propertyFile.exists()) {
					propertyFile.delete();
				}
				
				//prop.load(new FileInputStream(propertyFileName));
				prop.setProperty(this.combKey, tmpProp.getCombinationListString());
				prop.setProperty(this.adapterKey, tmpProp.getAdapterString());
				prop.setProperty(this.stopKey, "false");
				
				prop.store(new FileOutputStream(propertyFileName), null);
				allMethodMap.put(tmpProp.getMethodName(), true);
			}
			
			for (String methodName: allMethodMap.keySet()) {
				if (!allMethodMap.get(methodName)) {
					this.shutDownPropertyFile(methodName);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	private void shutDownPropertyFile(String methodName) {
		Properties prop = new Properties();
		File propFile = new File(this.configRoot + "/" + methodName + ".property");
		
		if (propFile.exists())
			propFile.delete();
		
		try {
			prop.setProperty(this.combKey, "");
			prop.setProperty(this.adapterKey, "");
			prop.setProperty(this.stopKey, "true");
			
			prop.store(new FileOutputStream(propFile), null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void exportHoldMethodProperty() {
		
		Properties prop = new Properties();
		HashSet<String> remainedT = new HashSet<String>();
		HashSet<String> remainedC = new HashSet<String>();
		
		String methodKey = "Method";
		String tKey = "Transformers";
		String cKey = "Checkers";
		
		StringBuilder sBuilder = new StringBuilder();
		
		sBuilder.append(holdHeader);
		
		MethodProfiler tmpProfiler;
		for (MethodProfiler mProfiler: this.profilerList) {
			for (MethodProfile mProfile: mProfiler.getHoldMethodProfiles()) {
				sBuilder.append(mProfile.getOri().getMethod().getName() + ",");
				sBuilder.append(mProfile.getFrontend() + ",");
				sBuilder.append(mProfile.getBackend() + "\n");
				//sBuilder.append(mProfile.getResult().holds + "\n");
			}
		}
		//System.out.println("Test export string: " + sBuilder.toString());
		
		//File rootDir = new File(profileRoot + this.timeTag);
		File rootDir = new File(profileRoot + this.holdTag);
		
		String absPath = "";
		if (rootDir.exists() && rootDir.isDirectory()) {
			absPath = rootDir.getAbsolutePath() + "/";
			System.out.println("Confirm root directory for exporting: " + absPath);
		} else if (!rootDir.exists()) {
			System.out.println("Profile directory does not exists. Create one...");
			boolean success = rootDir.mkdir();
			
			if (success) {
				System.out.println("Profile directory creation succeeds.");
				absPath = rootDir.getAbsolutePath() + "/";
				System.out.println("Confirm root direcotry for exporting: " + absPath);
			} else {
				System.out.println("Profile directory creation fails");
				return ;
			}
		} else {
			System.out.println("For some reason, profile directory creation fails.");
			return ;
		}
		
		try {
			File holdFile = new File(absPath + this.calleeName + ".csv");
			System.out.println("Confirm exporting file: " + holdFile.getAbsolutePath());
			
			if (holdFile.exists()) {
				holdFile.delete();
			}
			
			//FileWriter fWriter = new FileWriter(absPath + this.calleeName + "_" + (new Date()).toString().replaceAll(" ", "") + "_holds.csv");
			FileWriter fWriter = new FileWriter(holdFile);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			bWriter.write(sBuilder.toString());
			bWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private void reportTransformerChecker() {		
		System.out.println("");
		
		System.out.println("Registered Adapter: ");
		System.out.println(this.targetAdapter.getName());
		
		System.out.println("");
		
		System.out.println("Method-specific Setting");
		HashMap<Class<? extends MetamorphicProperty>, HashSet<TransClassTuple>> tmpCombineMap;
		for (String tmpMethod: this.methodCombinations.keySet()) {
			System.out.println("Method Name: " + tmpMethod);
			System.out.println("Registered Combinations:");
			
			tmpCombineMap = this.methodCombinations.get(tmpMethod);
			for (Class<? extends MetamorphicProperty> tmpChecker: tmpCombineMap.keySet()) {
				for (TransClassTuple tmp: tmpCombineMap.get(tmpChecker)) {
					System.out.println(tmp.getTransClass() + " => " + tmpChecker);
					System.out.println("Parameter setting: " + tmp.getTimes());
				}
			}
		}
	}
	
	private static class MethodProp {
		
		private String methodName;
		
		private List<Combination> combinationList = new ArrayList<Combination>();
		
		private HashSet<String> transformers = new HashSet<String>();
		
		private HashSet<String> checkers = new HashSet<String>();
		
		private String adapter;
		
		public MethodProp(String methodName) {
			this.methodName = methodName;
		}
		
		public void addCombination(String transformer, String checker) {
			Combination comb = new Combination(transformer.replace("T:", ""), checker.replace("C:", ""));
			
			if (this.combinationList.contains(comb)) {
				int idx = this.combinationList.indexOf(comb);
				this.combinationList.get(idx).increCount();
			} else {
				this.combinationList.add(comb);
			}
		}
				
		public List<Combination> getCombinationList() {
			return this.combinationList;
		}
		
		public String getCombinationListString() {
			int max = 0;
			
			for (Combination comb: this.combinationList) {
				if (comb.getCount() > max) {
					max = comb.getCount();
				}
			}
			
			List<Combination> selectedCombs = new ArrayList<Combination>();
			for (Combination comb: this.combinationList) {
				if (comb.getCount() == max) {
					selectedCombs.add(comb);
				}
			}
			
			StringBuilder sb = new StringBuilder();
			for (Combination comb: selectedCombs) {
				sb.append(comb.toString());
				sb.append(",");
			}
			
			sb.deleteCharAt(sb.length() - 1);
			return sb.toString();
		}
				
		public void setAdapterString(String adapter) {
			this.adapter = adapter;
		}
		
		public String getAdapterString() {
			return this.adapter;
		}
		
		public String getMethodName() {
			return this. methodName;
		}
	}
	
	private static class Combination {
		private String transformer;
		
		private String checker;
		
		private int count = 1;
		
		public Combination(String transformer, String checker) {
			this.transformer = transformer;
			this.checker = checker;
		}
		
		public String getTransformer() {
			return this.transformer;
		}
		
		public String getChecker() {
			return this.checker;
		}
		
		public void increCount() {
			this.count++;
		}
		
		public int getCount() {
			return this.count;
		}
		
		public boolean equals(Object input) {
			if (input == this)
				return true;
			
			if (!(input instanceof Combination))
				return false;
			
			Combination tmpInput = (Combination)input;
			
			if (tmpInput.getTransformer().equals(this.getTransformer()) && tmpInput.getChecker().equals(this.getChecker()))
				return true;
			else
				return false;
		}
		
		public String toString() {
			return "{" + this.transformer + "+" + this.checker + "}";
		}
	}
} 

