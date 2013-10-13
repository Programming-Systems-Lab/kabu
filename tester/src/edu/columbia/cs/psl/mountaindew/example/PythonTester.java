package edu.columbia.cs.psl.mountaindew.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.stream.JsonReader;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

public class PythonTester {
	
	private static String pythonPath = "/opt/local/Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7/site-packages";
	
	private String appPath;
	
	private void setAppPath(String appPath) {
		this.appPath = appPath;
	}
	
	private ArrayList<ArrayList<Double>> loadResultJson(String jsonPath) {
		try {
			JsonReader reader = new JsonReader(new FileReader(jsonPath));
			reader.beginObject();
			
			ArrayList<ArrayList<Double>> matrix = new ArrayList<ArrayList<Double>>();
			while(reader.hasNext()) {
				String name = reader.nextName();
				
				if (name.equalsIgnoreCase("Result")) {
					reader.beginArray();
					while (reader.hasNext()) {
						reader.beginArray();
						
						ArrayList<Double> innerList = new ArrayList<Double>();
						while(reader.hasNext()) {
							innerList.add(reader.nextDouble());
						}
						matrix.add(innerList);
						reader.endArray();
					}
					reader.endArray();
				}
			}
			
			reader.endObject();
			return matrix;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
		
	}
	
	@Metamorphic
	public ArrayList<ArrayList<Double>> pythonInvoker(String datapath) {
		String command = "python " + appPath + " " + datapath;
		System.out.println("Confirm command: " + command);
		
		List<String> commandList = new ArrayList<String>();
		commandList.add("python");
		commandList.add(appPath);
		commandList.add(datapath);
		
		try {
			ProcessBuilder pb = new ProcessBuilder(commandList);
			Map env = pb.environment();
			
			env.put("PYTHONPATH", pythonPath);
			
			Process process = pb.start();
			
			InputStream is = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			InputStream es = process.getErrorStream();
			BufferedReader br2 = new BufferedReader(new InputStreamReader(es));
			
			String tmp;
			while((tmp = br.readLine()) != null) {
				System.out.println(tmp);
			}
			
			while((tmp = br2.readLine()) != null) {
				System.err.println(tmp);
			}
			
			process.waitFor();
			System.out.println("Exit value: " + process.exitValue());
			
			return this.loadResultJson(this.appPath.replace(".py", ".json"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static void main(String args[]) {
		
		String appPath = "/Users/mikefhsu/Desktop/scikit/scikit_classification.py";
		String dataPath = "/Users/mikefhsu/Desktop/scikit/data/iris.csv";
		PythonTester pt = new PythonTester();
		pt.setAppPath(appPath);
		ArrayList<ArrayList<Double>>ret = pt.pythonInvoker(dataPath);
		System.out.println("Result from json: " + ret);
		
		//System.out.println(pt.loadResultJson("/Users/mikefhsu/Desktop/scikit/scikit_classification.json"));
		
	}

}
