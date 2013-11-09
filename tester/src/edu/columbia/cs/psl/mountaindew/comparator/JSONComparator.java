package edu.columbia.cs.psl.mountaindew.comparator;

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.google.gson.stream.JsonReader;

public class JSONComparator {
			
	public static void main(String args[]) {
		String firstJson = args[0];
		String secondJson = args[1];
		
		try {
			File firstFile = new File(firstJson);
			File secondFile = new File(secondJson);
			
			if (!firstFile.exists() || !secondFile.exists())
				throw new Exception("One or multiple json file does not exist");
			
			System.out.println("Confirm first file path: " + firstFile.getCanonicalPath());
			System.out.println("Confirm second file path: " + secondFile.getCanonicalPath());
			
			ReaderThread f1Reader = new ReaderThread(firstFile);
			ReaderThread f2Reader = new ReaderThread(secondFile);
			
			f1Reader.start();
			f2Reader.start();
			
			f1Reader.join();
			f2Reader.join();
			
			Set<StateObject>f1States = f1Reader.getStates();
			Set<StateObject>f2States = f2Reader.getStates();
			
			Set<StateObject> f1BasedDiff = diffStates(f1States, f2States);
			
			System.out.println("Check " + firstFile.getName() + " based diff");
			for (StateObject tmp: f1BasedDiff) {
				System.out.println(tmp);
			}
			
			Set<StateObject> f2BasedDiff = diffStates(f2States, f1States);
			System.out.println("Check " + secondFile.getName() + " based diff");
			for (StateObject tmp: f2BasedDiff) {
				System.out.println(tmp);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static Set<StateObject> diffStates(Set<StateObject> target, Set<StateObject> compare) {
		Set<StateObject> ret = new HashSet<StateObject>();
		//If s2 does not have, add directly
		//If s2 has, check class spec
		for (StateObject so1: target) {
			StateObject so2 = getStateWithSameID(so1, compare);
			if (so2 == null) {
				ret.add(so1);
			} else {
				StateObject diffObj = new StateObject();
				
				diffObj.setAdapter(so1.getAdapter());
				diffObj.setTransformer(so1.getTransformer());
				diffObj.setChecker(so1.getChecker());
				
				ret.add(diffObj);
				
				HashSet<ClassSpec> cs1 = so1.getClassSpecs();
				HashSet<ClassSpec> cs2 = so2.getClassSpecs();
				
				ClassSpec c2tmp;
				for (ClassSpec c1tmp: cs1) {
					c2tmp = getSpecWithSameClass(c1tmp, cs2);
					
					if (c2tmp == null) {
						diffObj.addSpec(c1tmp);
					} else {
						HashSet<String> c1fields = c1tmp.getFieldNames();
						HashSet<String> c2fields = c2tmp.getFieldNames();
						
						ClassSpec diffSpec = new ClassSpec();
						diffSpec.setClassName(c1tmp.getClassName());
						diffObj.addSpec(diffSpec);
						
						for (String c1field: c1fields) {
							if (!c2fields.contains(c1field)) {
								diffSpec.addFieldName(c1field);
							}
						}
					}
				}
			}
		}
		
		return ret;
	}
	
	public static StateObject getStateWithSameID(StateObject so, Set<StateObject> target) {
		for (StateObject tmp: target) {
			if (tmp.getIdentifier().equals(so.getIdentifier()))
				return tmp;
		}
		return null;
	}
	
	public static ClassSpec getSpecWithSameClass(ClassSpec cs, HashSet<ClassSpec> target) {
		for (ClassSpec tmp: target) {
			if (tmp.getClassName().equals(cs.getClassName())) {
				return tmp;
			}
		}
		
		return null;
	}

}



class ReaderThread extends Thread{
	private static String configName = "methodConfig";
	private static String aName = "Adapter";
	private static String hName = "HoldStates";
    private static String cName = "Checker";
    private static String tName = "Transformer";
    private static String csName = "ClassSpec";
    private static String cnName = "ClassName";
    private static String fName = "FieldNames";
	private String adapterName;
	private JsonReader reader;
	private File source;
	private Set<StateObject> states = new HashSet<StateObject>();
	
	public ReaderThread(File source) {
		this.source = source;
	}
	
	public void run() {
		try {
			this.reader = new JsonReader(new FileReader(this.source));
			this.reader.beginObject();
			
			while(reader.hasNext()) {
				String configName = reader.nextName();
				
				if (configName.equals(ReaderThread.configName)) {
					this.setupResult(this.reader);
				}
			}
			this.reader.endObject();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setupResult(JsonReader reader) {
		try {
			reader.beginObject();
			while(reader.hasNext()) {
				String tmpName = reader.nextName();
				
				if (tmpName.equals(ReaderThread.aName)) {
					this.adapterName = reader.nextString();
				} else if (tmpName.equals(ReaderThread.hName)) {
					reader.beginArray();
					
					while (reader.hasNext()) {
						this.states.add(this.setupStateObj(reader));
					}
					reader.endArray();
				}
			}
			reader.endObject();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public StateObject setupStateObj(JsonReader reader) {
		try {
			StateObject so = new StateObject();
			so.setAdapter(this.adapterName);
			
			reader.beginObject();
			while(reader.hasNext()) {
				String tmp2 = reader.nextName();
				
				if (tmp2.equals(ReaderThread.cName)) {
					so.setChecker(reader.nextString());
				} else if (tmp2.equals(ReaderThread.tName)) {
					so.setTransformer(reader.nextString());
				} else if (tmp2.equals(ReaderThread.csName)) {
					reader.beginArray();
					
					while(reader.hasNext()) {
						so.addSpec(this.setupClassSpec(reader));
					}
					
					reader.endArray();
				}
			}
			
			reader.endObject();
			
			return so;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public ClassSpec setupClassSpec(JsonReader reader) {
		
		try {
			ClassSpec cs = new ClassSpec();
			
			reader.beginObject();
			while(reader.hasNext()) {
				String tmp3Name = reader.nextName();
				
				if (tmp3Name.equals(cnName)) {
					cs.setClassName(reader.nextString());
				} else if (tmp3Name.equals(fName)) {
					reader.beginArray();
					
					while(reader.hasNext()) {
						cs.addFieldName(reader.nextString());
					}
					
					reader.endArray();
				}
			}
			reader.endObject();
			return cs;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public Set<StateObject> getStates() {
		return this.states;
	}
}

class StateObject implements Comparable<StateObject>{
	
	private String adapter;
	
	private String transformer;
	
	private String checker;
	
	private HashSet<ClassSpec> specSet = new HashSet<ClassSpec>();
	
	public void setAdapter(String adapter) {
		this.adapter = adapter;
	}
	
	public String getAdapter() {
		return this.adapter;
	}
	
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
	
	public void addSpec(ClassSpec spec) {
		this.specSet.add(spec);
	}
	
	public HashSet<ClassSpec> getClassSpecs() {
		return this.specSet;
	}

	public String getIdentifier() {
		return this.adapter + ":" + this.transformer + ":" + this.checker;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Adapter: " + this.adapter + "\n");
		sb.append("Transformer: " + this.transformer + "\n");
		sb.append("Checker: " + this.checker + "\n");
		sb.append("Class spec: " + this.specSet.toString() + "\n");
		return sb.toString();
	}

	@Override
	public int compareTo(StateObject o) {
		return this.getIdentifier().compareTo(o.getIdentifier());
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof StateObject))
			return false;
		
		StateObject tmp = (StateObject)obj;
		
		return this.toString().equals(tmp.toString());
	}
}

class ClassSpec {
	private String className;
	
	private HashSet<String> fieldNames = new HashSet<String>();
		
	public void setClassName(String className) {
		this.className = className;
	}
	
	public void addFieldName(String fieldName) {
		this.fieldNames.add(fieldName);
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public HashSet<String> getFieldNames() {
		return this.fieldNames;
	}
	
	public boolean eqauals(Object tmp) {
		if (! (tmp instanceof ClassSpec))
			return false;
		
		ClassSpec tmpSpec = (ClassSpec)tmp;
		
		if (!tmpSpec.getClassName().equals(this.getClassName()))
			return false;
		
		if (!tmpSpec.getFieldNames().equals(this.getFieldNames()))
			return false;
		
		return true;
		
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Class name: " + this.className + "\n");
		sb.append("Filed names: " + this.fieldNames.toString() + "\n");
		return sb.toString();
	}
	
}
