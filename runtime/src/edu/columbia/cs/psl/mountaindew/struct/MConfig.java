package edu.columbia.cs.psl.mountaindew.struct;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.gson.stream.JsonReader;

import edu.columbia.psl.invivoexpreval.Java.ThisReference;

public class MConfig {
		
	private static String globalConf = "globalConfig";
	
	private static String methodConf = "methodConfig";
	
	private static String aKey = "Adapter";
	
	private static String tKeys = "Transformers";
	
	private static String tKey = "Transformer";
	
	private static String cKeys = "Checkers";
	
	private static String cKey = "Checker";
	
	private static String sKeys = "States";
	
	private static String hKey = "HoldStates";
	
	private static String classKey = "ClassSpec";
		
	private static String cMemName = "ClassName";
	
	private static String fMemNames = "FieldNames";
		
	private String adapter;
	
	private List<String> transformers = new ArrayList<String>();
	
	private List<String> checkers = new ArrayList<String>();
	
	private List<StateItem> states = new ArrayList<StateItem>();
	
	private List<MethodStateItem> mStates = new ArrayList<MethodStateItem>();
	
	//Key checker, Val: Map<transformer, List of class with fields>
	private HashMap<String, HashMap<String, List<StateItem>>> configMap = 
			new HashMap<String, HashMap<String, List<StateItem>>>();
		
	public String getAdapter() {
		return this.adapter;
	}
	
	public List<String> getTransformers() {
		return this.transformers;
	}
	
	public List<String> getCheckers() {
		return this.checkers;
	}
		
	public List<StateItem> getStates() {
		return this.states;
	}
	
	public List<MethodStateItem> getMethodStates() {
		return this.mStates; 
	}
	
	public HashMap<String, HashMap<String, List<StateItem>>> getConfigMap() {
		return this.configMap;
	}
	
	public void loadJsonFile(String jsonPath) {
		File jsonFile = new File(jsonPath);
		
		if (!jsonFile.exists()) {
			System.out.println("Configuration file does not exist: " + jsonFile.getAbsolutePath());
			return ;
		}
		
		JsonReader jsonReader;
		try {
			jsonReader = new JsonReader(new FileReader(jsonPath));
			jsonReader.beginObject();
			
			while(jsonReader.hasNext()) {
				String tmpName = jsonReader.nextName();
				
				if (tmpName.equalsIgnoreCase(globalConf)) {
					this.setupValue(jsonReader);
					this.loadGlobalMap();
				} else if (tmpName.equalsIgnoreCase(methodConf)) {
					this.setupMethodValue(jsonReader);
					this.loadMethodMap();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void setupValue(JsonReader reader) throws IOException {
		reader.beginObject();
		
		String tmpName;
		while (reader.hasNext()) {
			tmpName = reader.nextName();
			
			if (tmpName.equalsIgnoreCase(aKey)) {
				this.adapter = reader.nextString();
			} else if (tmpName.equalsIgnoreCase(tKeys)) {
				reader.beginArray();
				
				while (reader.hasNext()) {
					this.transformers.add(reader.nextString());
				}
				
				reader.endArray();
			} else if (tmpName.equalsIgnoreCase(cKeys)) {
				reader.beginArray();
				
				while (reader.hasNext()) {
					this.checkers.add(reader.nextString());
				}
				
				reader.endArray();
			} else if (tmpName.equalsIgnoreCase(sKeys)){
				reader.beginArray();
				
				while (reader.hasNext()) {
					reader.beginObject();
					
					StateItem si = new StateItem();
					
					while(reader.hasNext()) {
						String memberName = reader.nextName();
						
						if (memberName.equalsIgnoreCase(cMemName)) {
							si.setClassName(reader.nextString());
						} else if (memberName.equalsIgnoreCase(fMemNames)) {
							reader.beginArray();
							
							while (reader.hasNext()) {
								si.addFieldName(reader.nextString());
							}
							
							reader.endArray();
						}
					}	
					this.states.add(si);
					reader.endObject();
				}
				
				reader.endArray();
			}
		}
		
		reader.endObject();
	}
	
	private void loadGlobalMap() {
		for (String tmpChecker: this.checkers) {
			HashMap<String, List<StateItem>> tsMap = new HashMap<String, List<StateItem>>();
			
			for (String tmpTransformer: this.transformers) {
				tsMap.put(tmpTransformer, this.states);
			}
			this.configMap.put(tmpChecker, tsMap);
		}
	}
	
	private void loadMethodMap() {
		String checker;
		String transformer;
		List<StateItem> stateItems;
		
		for (MethodStateItem msi: this.mStates) {
			checker = msi.getChecker();
			transformer = msi.getTransformer();
			stateItems = msi.getStateItems();
			
			if (!this.configMap.keySet().contains(checker)) {
				HashMap<String, List<StateItem>> tsMap = new HashMap<String, List<StateItem>>();
				tsMap.put(transformer, stateItems);
				
				this.configMap.put(checker, tsMap);
			} else {
				HashMap<String, List<StateItem>> tsMap = this.configMap.get(checker);
				
				if (!tsMap.keySet().contains(transformer)) {
					tsMap.put(transformer, stateItems);
				} else {
					tsMap.get(transformer).addAll(stateItems);
				}
			}
		} 
	}
	
	private void setupMethodValue(JsonReader reader) throws IOException {
		reader.beginObject();
		
		String tmpName;
		while(reader.hasNext()) {
			tmpName = reader.nextName();
			
			if (tmpName.equalsIgnoreCase(aKey)) {
				this.adapter = reader.nextString();
			} else if (tmpName.equalsIgnoreCase(hKey)) {
				reader.beginArray();
				
				while (reader.hasNext()) {
					reader.beginObject();
					
					MethodStateItem ms = new MethodStateItem();
					
					String innerTmp;
					while(reader.hasNext()) {
						innerTmp = reader.nextName();
						
						if (innerTmp.equalsIgnoreCase(cKey)) {
							ms.setChecker(reader.nextString());
						} else if (innerTmp.equalsIgnoreCase(tKey)) {
							ms.setTransformer(reader.nextString());
						} else if (innerTmp.equalsIgnoreCase(classKey)) {
							reader.beginArray();
							
							while (reader.hasNext()) {
								reader.beginObject();
								
								StateItem si = new StateItem();
								while (reader.hasNext()) {
									String iitmp = reader.nextName();
									
									if (iitmp.equalsIgnoreCase(cMemName)) {
										si.setClassName(reader.nextString());
									} else if (iitmp.equalsIgnoreCase(fMemNames)) {
										reader.beginArray();
										
										while(reader.hasNext()) {
											si.addFieldName(reader.nextString());
										}
										
										reader.endArray();
									}
								}
								
								ms.addStateItem(si);
								reader.endObject();
							}
							
							reader.endArray();
						}
					}
					
					this.mStates.add(ms);
					reader.endObject();
				}
				
				reader.endArray();
			}
		}
		
		reader.endObject();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Adapter: " + this.adapter + "\n");
		sb.append("Transformers: " + this.transformers + "\n");
		sb.append("Checkers: " + this.checkers + "\n");
		sb.append("States: " + this.states);
		
		return sb.toString();
	}
	
	public static class StateItem {
		private String className;
		
		private HashSet<String> fieldNames = new HashSet<String>();
		
		public void addFieldName(String fieldName) {
			this.fieldNames.add(fieldName);
		}
		
		public void setClassName(String className) {
			this.className = className;
		}
		
		public String getClassName() {
			return this.className;
		}
		
		public HashSet<String> getFieldNames() {
			return this.fieldNames;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("ClassName: " + this.className + "\n");
			sb.append("FieldNames: " + this.fieldNames + "\n");
			return sb.toString();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (! (obj instanceof StateItem))
				return false;
			
			StateItem tmp = (StateItem) obj;
			
			if (!tmp.getClassName().equals(this.className))
				return false;
			
			if (!(tmp.getFieldNames() == this.getFieldNames()))
				return false;
			
			return true;
		}
	}
	
	public static class MethodStateItem {
		
		private String transformer;
		
		private String checker;
		
		private List<StateItem> mStateItems = new ArrayList<StateItem>();
				
		public void setTransformer(String transformer) {
			this.transformer = transformer;
		}
		
		public String getTransformer() {
			return this.transformer;
		}
		
		public void setChecker(String checker) {
			this.checker = checker;
		}
		
		public String getChecker() {
			return this.checker;
		}
		
		public void addStateItem(StateItem si) {
			this.mStateItems.add(si);
		}
		
		public List<StateItem> getStateItems() {
			return this.mStateItems;
		}
		
		@Override
		public boolean equals(Object tmp) {
			if (!(tmp instanceof MethodStateItem))
				return false;
			
			MethodStateItem tmpItem  = (MethodStateItem)tmp;
			if (!tmpItem.getChecker().equals(this.checker))
				return false;
			
			if (!tmpItem.getTransformer().equals(this.transformer))
				return false;
			
			return true;
		}
	}
}
