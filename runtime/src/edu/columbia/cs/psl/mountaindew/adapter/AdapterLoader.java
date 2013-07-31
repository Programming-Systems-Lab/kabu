package edu.columbia.cs.psl.mountaindew.adapter;

import java.net.URL;
import java.net.URLClassLoader;

public class AdapterLoader {

	private String className;
		
	public static Class<? extends AbstractAdapter> loadClass(String className) {
		try {
			URL classUrl = AdapterLoader.class.getProtectionDomain().getCodeSource().getLocation();
			//System.out.println("AdapterLoader confirm current location: " + classUrl.getPath());
			
			URL[] classUrls = {classUrl};
			URLClassLoader ucl = new URLClassLoader(classUrls);
			Class adapterClass = ucl.loadClass(className);
			
			if (AbstractAdapter.class.isAssignableFrom(adapterClass)) {
				return adapterClass;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}

}
