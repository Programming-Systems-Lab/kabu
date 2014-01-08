package edu.columbia.cs.psl.mountaindew.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;

public class MetaSerializer {
	
	public static String localSuffix = "_local";
	
	public static String fieldSuffix = "_field";
	
	public static String extractVersion(Object obj) {
		try {
			Class objClass = obj.getClass();
			String objFullName = objClass.getName().replace(".", "/") + ".class";
			String objPath = objClass.getClassLoader().getResource(objFullName).toString();
			String binPath = objPath.substring(0, objPath.indexOf(objFullName));
			File binParDir = (new File(binPath)).getParentFile();
			String binParPath = binParDir.getPath();
			String version = binParPath.substring(binParPath.lastIndexOf("/") + 1);
			
			System.out.println("Extracted version: " + version);
			
			return version;
		} catch (Exception ex) {
			System.out.println("Find no class path for object: " + obj.getClass().getName());
			return null;
		}
	}
	
	public static Map<Integer, String> deserializeLocalVarMap(String className) {
		String path = "ser/" + className + ".ser";
		Map<Integer, String> localVarMap = (Map<Integer, String>)deserializeBasic(path);
		return localVarMap;
	}
	
	public static void serializeClassFieldMap(String version, Map<String, Set<String>> classFieldMap) {
		String path = "ser/" + version + fieldSuffix + ".ser";
		serializeBasic(path, classFieldMap);
	}
	
	public static Map<String, Set<String>> deserializeClassFieldMap(String version) {
		String path = "ser/" + version + fieldSuffix + ".ser";
		Map<String, Set<String>> classFieldMap = (Map<String, Set<String>>)deserializeBasic(path);
		return classFieldMap;
	}
	
	private static Object deserializeBasic(String path) {
		try {
			File file = new File(path);
			
			if (!file.exists()) {
				System.err.println("File does not exist: " + file.getAbsolutePath());
				return null;
			}
			
			FileInputStream in = new FileInputStream(file);
			ObjectInputStream reader = new ObjectInputStream(in);
			Object ret = reader.readObject();
			
			System.out.println("Complete deserialization: " + file.getAbsolutePath());
			return ret;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private static void serializeBasic(String path, Object obj) {
		try {
			File file = new File(path);
			
			if (file.exists()) {
				file.delete();
			}
			
			FileOutputStream out = new FileOutputStream(file);
			ObjectOutputStream writer = new ObjectOutputStream(out);
			writer.writeObject(obj);
			
			out.close();
			writer.close();
			
			System.out.println("Complete serialization: " + file.getAbsolutePath());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
