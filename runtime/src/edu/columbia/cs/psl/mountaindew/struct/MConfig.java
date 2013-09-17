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
	
	private static String classKey = "ClassSpecs";
		
	private static String cMemName = "ClassName";
	
	private static String fMemNames = "FieldNames";
		
	private String adapter;
	
	private List<String> transformers = new ArrayList<String>();
	
	private List<String> checkers = new ArrayList<String>();
	
	private List<StateItem> states = new ArrayList<StateItem>();
	
	private List<MethodStateItem> mStates = new ArrayList<MethodStateItem>();
		
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
				} else if (tmpName.equalsIgnoreCase(methodConf)) {
					this.setupMethodValue(jsonReader);
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
	
	public void setupMethodValue(JsonReader reader) throws IOException {
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
							reader.beginObject();
							
							StateItem si = new StateItem();
							
							String nestTmp;
							while(reader.hasNext()) {
								nestTmp = reader.nextString();
								
								if (nestTmp.equalsIgnoreCase(cMemName)) {
									si.setClassName(reader.nextString());
								} else if (nestTmp.equalsIgnoreCase(fMemNames)) {
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
					}
					
					this.mStates.add(ms);
					reader.endObject();
				}
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
			
			if (!tmpItem.getChecker().equals(this.checker))
				return false;
			
			if (!tmpItem.getTransformer().equals(this.transformer))
				return false;
			
			return true;
		}
	}
}
