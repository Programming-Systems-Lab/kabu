package edu.columbia.cs.psl.mountaindew.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

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
	
	private List<String> transformerNames = new ArrayList<String>();
	
	private List<String> checkerNames = new ArrayList<String>();
	
	private String propertyFilePath;
	
	public MetamorphicConfigurer(String propertyFilePath) {
		this.propertyFilePath = propertyFilePath;
		this.loadConfigProperties();
	}
	
	public void loadConfigProperties() {
		File propertyFile  = new File(propertyFilePath);
		
		if (!propertyFile.exists()) {
			System.err.println("Configuration file for Metamorphic Property Testing does not exist");
			return ;
		}
		
		prop = new Properties();
		
		try {
			prop.load(new FileInputStream(propertyFile.getAbsolutePath()));
			this.adapterName = this.prop.getProperty(adapterKey);
			//this.adapterClassName = adapterPackage + dot +  this.prop.getProperty(adapterKey);
			this.adapterClassName = adapterPackage + dot + this.adapterName;
			this.transformerNames = parseProperty(this.prop.getProperty(transformKey), transformerPackage);
			this.checkerNames = parseProperty(this.prop.getProperty(checkerKey), checkerPackage);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
}
