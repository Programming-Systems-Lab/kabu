package edu.columbia.cs.psl.mountaindew.util;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.stream.JsonWriter;

import edu.columbia.cs.psl.mountaindew.struct.MConfig;
import edu.columbia.cs.psl.mountaindew.struct.MConfig.StateItem;

public class JsonManager {
	
	public static void writeMetaResult(HashMap<String, HashSet<MConfig.MethodStateItem>> records, 
			String configRoot, 
			String adapterName) {
		
		JsonWriter jw = null;
		try {
			for (String methodName: records.keySet()) {
				HashSet<MConfig.MethodStateItem> mStateSet = records.get(methodName);
				
				String outputPath = configRoot + "/" + methodName.replace("/", ".") + ".json";
				File outputFile = new File(outputPath);
				System.out.println("Check output path: " + outputFile.getAbsolutePath());
				if (!outputFile.exists())
					outputFile.createNewFile();
				
				jw = new JsonWriter(new FileWriter(outputFile));
				jw.setIndent("	");
				jw.beginObject();
				jw.name("methodConfig");
				
				jw.beginObject();
				jw.name("Adapter").value(adapterName);
				jw.name("HoldStates");
				
				jw.beginArray();
				for (MConfig.MethodStateItem tmpItem: mStateSet) {
					jw.beginObject();
					jw.name("Checker").value(tmpItem.getChecker().replace("C:", ""));
					
					jw.name("Transformer");
					jw.beginObject();
					jw.name(tmpItem.getTransformer().getTransformer().replace("T:", ""));
					jw.beginArray();
					for (Number num: tmpItem.getTransformer().getTimes()) {
						jw.value(num);
					}
					jw.endArray();
					jw.endObject();
					
					jw.name("IsValidCase").value(tmpItem.getIsValidCase());
					jw.name("FieldSetting").value(tmpItem.getFieldSetting());
					
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

}
