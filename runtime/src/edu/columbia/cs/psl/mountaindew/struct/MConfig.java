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
	
	private List<TransTuple> transformers = new ArrayList<TransTuple>();
	
	private List<String> checkers = new ArrayList<String>();
	
	private List<StateItem> states = new ArrayList<StateItem>();
	
	private List<MethodStateItem> mStates = new ArrayList<MethodStateItem>();
	
	//Key checker, Val: Map<transformer, List of class with fields>
	private HashMap<String, HashMap<TransTuple, List<StateItem>>> configMap = 
			new HashMap<String, HashMap<TransTuple, List<StateItem>>>();
		
	public String getAdapter() {
		return this.adapter;
	}
	
	public List<TransTuple> getTransformers() {
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
	
	public HashMap<String, HashMap<TransTuple, List<StateItem>>> getConfigMap() {
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
					reader.beginObject();
					
					String trans = reader.nextName();
					List<Number> times = new ArrayList<Number>();
					
					reader.beginArray();
					while(reader.hasNext()) {
						times.add(reader.nextInt());
					}
					reader.endArray();
					
					this.transformers.add(new TransTuple(trans, times));
					
					reader.endObject();
				}
				
				reader.endArray();
			} else if (tmpName.equalsIgnoreCase(cKeys)) {
				reader.beginArray();
				
				while (reader.hasNext()) {
					this.checkers.add(reader.nextString());
				}
				
				reader.endArray();
			} 
		}
		
		reader.endObject();
	}
	
	private void loadGlobalMap() {
		for (String tmpChecker: this.checkers) {
			HashMap<TransTuple, List<StateItem>> tsMap = 
					new HashMap<TransTuple, List<StateItem>>();
			
			for (TransTuple tmpTransformer: this.transformers) {
				tsMap.put(tmpTransformer, null);
			}
			this.configMap.put(tmpChecker, tsMap);
		}
	}
	
	private void loadMethodMap() {
		String checker;
		TransTuple transformer;
		List<StateItem> stateItems;
		
		for (MethodStateItem msi: this.mStates) {
			checker = msi.getChecker();
			transformer = msi.getTransformer();
			stateItems = msi.getStateItems();
			
			if (!this.configMap.keySet().contains(checker)) {
				HashMap<TransTuple, List<StateItem>> tsMap = new HashMap<TransTuple, List<StateItem>>();
				tsMap.put(transformer, stateItems);
				
				this.configMap.put(checker, tsMap);
			} else {
				HashMap<TransTuple, List<StateItem>> tsMap = this.configMap.get(checker);
				
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
							reader.beginObject();
							String transformer = reader.nextName();
							ArrayList<Number> times = new ArrayList<Number>();
							
							reader.beginArray();
							while(reader.hasNext()) {
								times.add(reader.nextDouble());
							}
							reader.endArray();
							
							ms.setTransformer(new TransTuple(transformer, times));
							reader.endObject();
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
	
	public static class TransTuple {
		private String transformer;
		
		private List<Number> times = new ArrayList<Number>();
		
		public TransTuple(String transformer, List<Number>times) {
			this.transformer = transformer;
			this.times = times;
		}
		
		public String getTransformer() {
			return this.transformer;
		}
		
		public List<Number> getTimes() {
			return this.times;
		}
		
		public void setTransformer(String transformer) {
			this.transformer = transformer;
		}
		
		public void setTimes(ArrayList<Number> times) {
			this.times = times;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Transformer: " + this.transformer);
			sb.append("Param: " + this.times);
			return sb.toString();
		}
		
		@Override
		public boolean equals(Object tmp) {
			if (!(tmp instanceof TransTuple))
				return false;
			
			TransTuple tmpTuple = (TransTuple)tmp;
			if (!tmpTuple.getTransformer().equals(this.transformer))
				return false;
			
			if (!tmpTuple.getTimes().equals(this.times))
				return false;
			
			return true;
		}
		
		@Override
		public int hashCode() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.transformer);
			for(Number tmp: this.times) {
				sb.append(tmp);
			}
			return sb.toString().hashCode();
		}
	}
	
	public static class StateItem {
		private String className;
		
		private HashSet<String> fieldNames = new HashSet<String>();
		
		private HashSet<String> allFields = new HashSet<String>();
		
		public void addFieldName(String fieldName) {
			this.fieldNames.add(fieldName);
		}
		
		public void addAllFieldName(String fieldName) {
			this.allFields.add(fieldName);
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
		
		public HashSet<String> getAllFields() {
			return this.allFields;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("ClassName: " + this.className + "\n");
			sb.append("FieldNames: " + this.fieldNames + "\n");
			sb.append("AllFields: " + this.allFields + "\n");
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
			
			if (!(tmp.getAllFields() == this.getAllFields())) {
				return false;
			}
			
			return true;
		}
		
		@Override
		public int hashCode() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.className);
			
			for (String tmp: this.fieldNames) {
				sb.append(tmp);
			}
			
			return sb.toString().hashCode();
		}
	}
	
	public static class MethodStateItem {
		
		private TransTuple transformer;
		
		private String checker;
		
		private List<StateItem> mStateItems = new ArrayList<StateItem>();
				
		public void setTransformer(TransTuple transformer) {
			this.transformer = transformer;
		}
		
		public TransTuple getTransformer() {
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
		
		@Override
		public int hashCode() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.transformer);
			sb.append(this.checker);
			
			for (StateItem si: this.mStateItems) {
				sb.append(si.toString());
			}
			
			return sb.toString().hashCode();
		}
	}
}
