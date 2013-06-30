package edu.columbia.cs.psl.mountaindew.example.mutant;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MutantTestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String rootDir = "profiles";
		
		MutantAnalyzer analyzer = new MutantAnalyzer(rootDir);
		analyzer.loadFiles();
		analyzer.analyzeFiles();
		Map<String, List<MutantStruct>> files = analyzer.getFileMap();
		
		List<MutantStruct> contentList;
		for (String fileName: files.keySet()) {
			System.out.println("File name: " + fileName);
			
			contentList = files.get(fileName);
			Collections.sort(contentList);
			
			for (MutantStruct struct: contentList) {
				System.out.println(struct.getMethodName() + " " + struct.getFrontend() + " " + struct.getBackend() + " " + struct.isHold());
			}
			System.out.println("");
		}
		analyzer.summarizeFiles();
	}

}
