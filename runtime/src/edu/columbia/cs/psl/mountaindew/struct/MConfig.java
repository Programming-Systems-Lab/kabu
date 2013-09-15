package edu.columbia.cs.psl.mountaindew.struct;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.gson.stream.JsonReader;

public class MConfig {
		
	private static String configName = "mconfig";
	
	private static String aKey = "Adapter";
	
	private static String tKey = "Transformers";
	
	private static String cKey = "Checkers";
	
	private static String sKeys = "States";
		
	private static String cMemName = "ClassName";
	
	private static String fMemName = "FieldNames";
	
	private String jsonPath;
	
	private String adapter;
	
	private List<String> transformers = new ArrayList<String>();
	
	private List<String> checkers = new ArrayList<String>();
	
	private List<StateItem> states = new ArrayList<StateItem>();
	
	public MConfig(String jsonPath) {
		this.jsonPath = jsonPath;
	}
	
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
	
	public void loadJsonFile() {
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
				
				if (tmpName.equalsIgnoreCase(configName)) {
					this.setupValue(jsonReader);
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
			} else if (tmpName.equalsIgnoreCase(tKey)) {
				reader.beginArray();
				
				while (reader.hasNext()) {
					this.transformers.add(reader.nextString());
				}
				
				reader.endArray();
			} else if (tmpName.equalsIgnoreCase(cKey)) {
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
						} else if (memberName.equalsIgnoreCase(fMemName)) {
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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Adapter: " + this.adapter + "\n");
		sb.append("Transformers: " + this.transformers + "\n");
		sb.append("Checkers: " + this.checkers + "\n");
		sb.append("States: " + this.states);
		
		return sb.toString();
	}
	
	public class StateItem {
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
}
