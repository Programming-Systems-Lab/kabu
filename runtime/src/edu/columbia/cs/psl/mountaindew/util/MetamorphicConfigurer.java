package edu.columbia.cs.psl.mountaindew.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.HashMap;

import edu.columbia.cs.psl.mountaindew.struct.MConfig;

public class MetamorphicConfigurer {

	private static String adapterKey = "Adapter";
	
	private static String transformKey = "Transformers";
	
	private static String checkerKey = "Checkers";
	
	private static String adapterPackage = "edu.columbia.cs.psl.mountaindew.adapter";
	
	private static String transformerPackage = "edu.columbia.cs.psl.metamorphic.inputProcessor.impl";
	
	private static String checkerPackage = "edu.columbia.cs.psl.mountaindew.property";
	
	private static String delim = ",";
	
	private static String dot = ".";
	
	private Properties prop;
	
	//Only one adapter per time
	private String adapterClassName;
	
	private String adapterName;
	
	private List<String> transformerNames = null;
	
	private List<String> checkerNames = null;
	
	private List<MConfig.StateItem> stateItems = null;
	
	private List<MConfig.MethodStateItem> mStateItems = null;
	
	private HashMap<String, HashMap<String, List<MConfig.StateItem>>> configMap;
	
	public static String getCheckerFullName(String checkerName) {
		return checkerPackage + dot + checkerName;
	}
	
	public static String getTransformerFullName(String transformerName) {
		return transformerPackage + dot + transformerName;
	}
		
	public void loadConfiguration(String jsonFilePath) {
		MConfig mconfig = new MConfig();
		mconfig.loadJsonFile(jsonFilePath);
		
		this.adapterName = mconfig.getAdapter();
		this.adapterClassName = adapterPackage + dot + this.adapterName;
		//Add package name to checker and transformer
		this.packageConfigMap(mconfig.getConfigMap());
		/*this.transformerNames = packageClass(mconfig.getTransformers(), transformerPackage);
		this.checkerNames = packageClass(mconfig.getCheckers(), checkerPackage);
		this.stateItems = mconfig.getStates();*/
	}
	
	public HashMap<String, HashMap<String, List<MConfig.StateItem>>> getConfigMap() {
		return this.configMap;
	}
	
	/*public void loadMethodConfiguration(String mJsonFilePath) {
		MConfig mconfig = new MConfig();
		mconfig.loadJsonFile(mJsonFilePath);
		
		this.adapterName = mconfig.getAdapter();
		this.adapterClassName = adapterPackage + dot + this.adapterName;
		this.mStateItems = mconfig.getMethodStates();
	}*/
	
	public void cleanConfigurationSetup() {
		this.adapterName = null;
		this.adapterClassName = null;
		this.transformerNames = null;
		this.checkerNames = null;
		this.stateItems = null;
		this.mStateItems = null;
	}
	
	/*public void loadConfigProperties() {
		File propertyFile  = new File(propertyFilePath);
		
		if (!propertyFile.exists()) {
			System.err.println("Configuration file for Metamorphic Property Testing does not exist");
			return ;
		}
		
		prop = new Properties();
		
		try {
			prop.load(new FileInputStream(propertyFile.getAbsolutePath()));
			this.adapterName = this.prop.getProperty(adapterKey);
			this.adapterClassName = adapterPackage + dot + this.adapterName;
			this.transformerNames = parseProperty(this.prop.getProperty(transformKey), transformerPackage);
			this.checkerNames = parseProperty(this.prop.getProperty(checkerKey), checkerPackage);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}*/
	
	public void packageConfigMap(HashMap<String, HashMap<String, List<MConfig.StateItem>>> rawConfigMap) {
		this.configMap = new HashMap<String, HashMap<String, List<MConfig.StateItem>>>();
		
		String completeChecker;
		HashMap<String, List<MConfig.StateItem>> tmpTransMap;
		for (String tmpChecker: rawConfigMap.keySet()) {
			completeChecker = getCheckerFullName(tmpChecker);
			
			tmpTransMap = rawConfigMap.get(tmpChecker);
			
			String  completeTrans;
			HashMap<String, List<MConfig.StateItem>> completeTransMap = new HashMap<String, List<MConfig.StateItem>>();
			for (String tmpTrans: tmpTransMap.keySet()) {
				completeTrans = getTransformerFullName(tmpTrans);
				completeTransMap.put(completeTrans, tmpTransMap.get(tmpTrans));
			}
			
			this.configMap.put(completeChecker, completeTransMap);
		}
	}
		
	public List<String> packageClass(List<String>rawNames, String packageName) {
		List<String> tokenList = new ArrayList<String>();
		for (String tmp: rawNames) {
			tokenList.add(packageName + dot + tmp);
		}
		return tokenList;
	}
	
	public List<String> parseProperty(String rawInput, String packageName) {
		StringTokenizer st = new StringTokenizer(rawInput, delim);
		List<String> tokenList = new ArrayList<String>();
		
		while(st.hasMoreTokens()) {
			tokenList.add(packageName + dot + st.nextToken());
		}
		return tokenList;
	}
	
	public String getAdapterClassName() {
		return this.adapterClassName;
	}
	
	public String getAdapterName() {
		return this.adapterName;
	}
	
	public List<String> getTransformerNames() {
		return this.transformerNames;
	}
	
	public List<String> getCheckerNames() {
		return this.checkerNames;
	}
	
	public List<MConfig.StateItem> getStates() {
		return this.stateItems;
	}
}
