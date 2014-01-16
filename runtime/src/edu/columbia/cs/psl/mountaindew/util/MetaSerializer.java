package edu.columbia.cs.psl.mountaindew.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

public class MetaSerializer {
	
	public static String localSuffix = "_local";
	
	public static String fieldSuffix = "_field";
	
	public static Map<String, String> bytecodeMap = new HashMap<String, String>();
	
	static {
		bytecodeMap.put("int", "I");
		bytecodeMap.put("double", "");
		bytecodeMap.put("float", "");
		bytecodeMap.put("short", "");
		bytecodeMap.put("long", "");
		bytecodeMap.put("char", "");
	}
	
	public static String extractVersion(Object obj) {
		if (obj == null)
			return null;
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
	
	public static Map<String, Map<Integer,String>> deserializedAllClassLocalVarMap(String className, Set<Method> mSet) {
		Map<String, Map<Integer, String>> ret = new HashMap<String, Map<Integer, String>>();
		
		String tmpKey;
		Map tmpVarMap;
		for (Method m: mSet) {
			//Tmp key = classname:methodname:returntype:parameter:modifier
			tmpKey = composeFullMethodName(className, m);
			
			System.out.println("Check path before deserialization: " + tmpKey);
			
			tmpVarMap = deserializeLocalVarMap(tmpKey);
			
			if (tmpVarMap == null)
				continue;
			
			ret.put(tmpKey, tmpVarMap);
		}
		
		return ret;
	}
	
	public static String composeFullMethodName(String className, Method m) {
		StringBuilder sb = new StringBuilder();
		sb.append(className + ":");
		sb.append(m.getName() + "->");
		sb.append(Type.getMethodDescriptor(m) + "->");
		sb.append(m.getModifiers());
		
		return sb.toString().replace("/", ".");
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
