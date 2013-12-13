package edu.columbia.cs.psl.mountaindew.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.Date;

public class MRExporter {
	
	private static String rootFolder = "metaresult";
	
	private static String resultSuffix = "_meta_result_";
	
	static {
		File rootFile = new File(rootFolder);
		
		if (!rootFile.exists() || !rootFile.isDirectory()) {
			rootFile.mkdir();
		}
	}
	
	public static void exportMetaResult(String methodName, String result) {
		File root = new File(rootFolder);
		
		if (!root.exists()) {
			System.err.println("Root folder " + root.getAbsolutePath() + " does not exist");
			return ;
		}
		
		String fileName = root.getAbsolutePath() + "/" + methodName + resultSuffix + (new Date()).getTime();
		System.out.println("Confirm metaresult file name: " + fileName);
		
		try {
			File targetFile = new File(fileName);
			BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile));
			
			writer.write(result);
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
}
