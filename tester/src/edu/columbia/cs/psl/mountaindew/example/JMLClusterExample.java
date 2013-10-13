package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.tools.data.FileHandler;

public class JMLClusterExample {
	
	public Dataset loadData(String dataPath) {
		Dataset dataset = null;
		try {
			dataset = FileHandler.loadDataset(new File(dataPath), 4, ",");
			return dataset;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static void main(String args[]) {
		
	}

}
