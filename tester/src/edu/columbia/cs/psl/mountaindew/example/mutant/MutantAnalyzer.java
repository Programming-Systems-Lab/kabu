package edu.columbia.cs.psl.mountaindew.example.mutant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

public class MutantAnalyzer {
	
	private Map<String, List<MutantStruct>> fileMap = new HashMap<String, List<MutantStruct>>();
	private String fileDir;
	private String csv = "csv";
	private String dot = ".";
	private String header = "Method name,frontend,backend,";
	private List<String> fileOrder = new ArrayList<String>();
	private String summaryFileName;
	
	public MutantAnalyzer(String fileDir) {
		this.fileDir = fileDir;
	}
	
	public void loadFiles() {
		File rootDir = new File(this.fileDir);
		
		if (!rootDir.exists()) {
			System.err.println("Invalid file directory: " + this.fileDir);
			return ;
		}
		
		this.selectFiles(rootDir);
	}
	
	private void selectFiles(File rootDir) {
		File[] childFiles = rootDir.listFiles();
		String fileName;
		String fileExtension;
		int dotIndx;
		for (File tmpFile: childFiles) {
			if (tmpFile.isDirectory())
				selectFiles(tmpFile);
			else {
				fileName = tmpFile.getAbsolutePath();
				dotIndx = fileName.lastIndexOf(dot);
				fileExtension = fileName.substring(dotIndx+1, fileName.length());
				
				System.out.println("File name" + fileName);
				System.out.println("File extension: " + fileExtension);
				
				if (fileExtension.equals(this.csv)) {
					if (!this.fileMap.keySet().contains(fileName)) {
						this.fileMap.put(fileName, new ArrayList<MutantStruct>());
						// Keep the order of files
						this.fileOrder.add(fileName);
					}
				}
			}
		}
	}
	
	public void analyzeFiles() {
		BufferedReader br = null;
		File openFile = null;
		boolean isFileNameSet = false;
		
		for(String fileName: this.fileOrder) {
			try {
				openFile = new File(fileName);
				this.header = this.header + openFile.getName() + ",";
				br = new BufferedReader(new FileReader(openFile));
				
				//Skip header
				br.readLine();
				
				String content;
				while ((content = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(content, ",");
					String token;
					int count = 0;
					MutantStruct mutant = new MutantStruct();
					while (st.hasMoreTokens()) {
						token = st.nextToken();
						
						//count 0 for method name, 5 for frontend, 6 for backend and 7 for hold;
						if (count == 0 ) {
							mutant.setMethodName(token);
							
							if (!isFileNameSet) {
								this.summaryFileName = this.fileDir + "/" + token + "Summary";
							}
						} else if (count == 5) {
							mutant.setFrontend(token);
						} else if (count == 6) {
							mutant.setBackend(token);
						} else if (count == 7) {
							mutant.setHold(Boolean.valueOf(token));
						}
						
						count++;
					}
					this.fileMap.get(fileName).add(mutant);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		try {
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void summarizeFiles() {
		this.header = this.header.substring(0, this.header.length() - 1);
		File summaryFile = new File(this.summaryFileName + (new Date()).toString().replaceAll(" ", "") + ".csv");
		
		System.out.println("Confirm summary file name: " + summaryFile.getAbsolutePath());
		
		StringBuilder sb = new StringBuilder();
		sb.append(header + "\n");
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(summaryFile));
			
			HashMap<String, ArrayList<String>> contentMap = new HashMap<String, ArrayList<String>>(); 
			
			//User first file to build up contentMap
			
			for (String fileName: this.fileOrder) {
				System.out.println("File to be summarized: " + fileName);
				List<MutantStruct> contents = this.fileMap.get(fileName);
				
				for (MutantStruct tmpStruct: contents) {
					String key = tmpStruct.getMethodName() + "," + tmpStruct.getFrontend() + "," + tmpStruct.getBackend();
					
					if (!contentMap.containsKey(key)) {
						ArrayList<String> holdList = new ArrayList<String>();
						holdList.add(String.valueOf(tmpStruct.isHold()));
						contentMap.put(key, holdList);
					} else {
						contentMap.get(key).add(String.valueOf(tmpStruct.isHold()));
					}
				}
			}
			
			for (String key: contentMap.keySet()) {
				sb.append(key + ",");
				String combinedHold = "";
				for (String hold: contentMap.get(key)) {
					combinedHold = combinedHold + hold + ",";
				}
				combinedHold = combinedHold.substring(0, combinedHold.length() - 1);
				sb.append(combinedHold + "\n");
			}
			
			bw.write(sb.toString());
			bw.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public Map<String, List<MutantStruct>> getFileMap() {
		return this.fileMap;
	}
	
	public static void main(String args[]) {
		MutantAnalyzer mAnalyzer = new MutantAnalyzer("profiles");
		mAnalyzer.loadFiles();
		mAnalyzer.analyzeFiles();
		mAnalyzer.summarizeFiles();
	}
}
