package edu.columbia.cs.psl.mountaindew.example.mutant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

public class MutantAnalyzer {
	
	private Map<String, List<MutantStruct>> fileMap = new HashMap<String, List<MutantStruct>>();
	private Map<String, List<String>> holdMap = new HashMap<String, List<String>>();
	private String fileDir;
	private String srcDir;
	private String summaryDir = "summary";
	private String csv = "csv";
	private String dot = ".";
	private String header = "Method name,frontend,backend,";
	private List<String> fileOrder = new ArrayList<String>();
	private String summaryFileName;
	private int totalMutantNumber = 0;
	private int noExceptionMutantNumber = 0;
	private String originalFileName;
	private String configFile = "config/mutant.property";
	private int originalFileRank = -1;
	private List<PropertyRank> rankList = new ArrayList<PropertyRank>();
	
	public MutantAnalyzer(String fileDir, String srcDir) {
		this.fileDir = fileDir;
		this.srcDir = srcDir;
	}
	
	public void loadFiles() {
		File rootDir = new File(this.fileDir);
		
		if (!rootDir.exists()) {
			System.err.println("Invalid file directory: " + this.fileDir);
			return ;
		}
		
		this.selectFiles(rootDir);
		//Don't count original file
		this.noExceptionMutantNumber = this.fileOrder.size() - 1;
		System.out.println("Check total non-exceptional mutant number: " + this.noExceptionMutantNumber);
	}
	
	public void countMutants() {
		File srcDir = new File(this.srcDir);
		
		if (!srcDir.exists()) {
			System.err.println("src directory does not exists.");
			return ;
		}
		
		int pos;
		String fileName;
		String extension;
		int count = 0;
		for (File tmp: srcDir.listFiles()) {
			fileName = tmp.getAbsolutePath();
			pos = fileName.lastIndexOf(dot);
			extension = fileName.substring(pos+1, fileName.length());
			
			if (extension.equals("java"))
				count++;
		}
		
		//Need to reduct 1 for original file that is not a mutant
		this.totalMutantNumber = count - 1;
		System.out.println("Check total mutant number: " + this.totalMutantNumber);
	}
	
	public void composeOriginalFileName() {
		File propertyFile = new File(this.configFile);
		
		if (!propertyFile.exists()) {
			System.err.println("Mutant configuration file does not exist");
			return ;
		}
		
		FileInputStream fs;
		
		try {
			fs = new FileInputStream(propertyFile);
			Properties mutantProperty = new Properties();
			mutantProperty.load(fs);
			String targetClass = mutantProperty.getProperty("targetclass");
			//Temporarily hardcode original
			this.originalFileName = targetClass + "original";
			System.out.println("Original file name: " + this.originalFileName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
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
						
						if (fileName.contains(this.originalFileName)) {
							this.originalFileRank = fileOrder.size() - 1;
						}
					}
				}
			}
		}
		
		System.out.println("Original file rank: " + this.originalFileRank);
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
					//MutantStruct mutant = new MutantStruct();
					String key = "";
					String value = "";
					while (st.hasMoreTokens()) {
						token = st.nextToken();
						
						//count 0 for method name, 5 for frontend, 6 for backend and 7 for hold;
						if (count == 0 ) {
							//mutant.setMethodName(token);
							key = key + token + ",";
							
							if (!isFileNameSet) {
								this.summaryFileName = this.summaryDir + "/" + token + "Summary";
								isFileNameSet = true;
							}
						} else if (count == 5) {
							//mutant.setFrontend(token);
							key = key + token + ",";
						} else if (count == 6) {
							//mutant.setBackend(token);
							key = key + token + ",";
						} else if (count == 7) {
							//mutant.setHold(Boolean.valueOf(token));
							value = token;
						}
						
						count++;
					}
					//this.fileMap.get(fileName).add(mutant);
					
					key = key.substring(0, key.length() - 1);
					
					if (!this.holdMap.containsKey(key)) {
						ArrayList<String> holdList = new ArrayList<String>();
						holdList.add(value);
						this.holdMap.put(key, holdList);
					} else {
						this.holdMap.get(key).add(value);
					}
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
	
	public void exportSummary() {
		//this.header = this.header.substring(0, this.header.length() - 1);
		this.header = this.header + "Mutant killed\n";
		File summaryFile = new File(this.summaryFileName + (new Date()).toString().replaceAll(" ", "") + ".csv");
		
		System.out.println("Confirm summary file name: " + summaryFile.getAbsolutePath());
		
		/*(for (String key: this.holdMap.keySet()) {
			System.out.println("Key: " + key);
			System.out.println("Value: " + this.holdMap.get(key));
		}*/
		
		StringBuilder sb = new StringBuilder();
		sb.append(this.header);
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(summaryFile));
			
			String oriVal;
			List<String> holdList;
			PropertyRank pr;
			for (String key: this.holdMap.keySet()) {
				sb.append(key + ",");
				
				String holdString = "";
				holdList = this.holdMap.get(key);
				oriVal = holdList.get(this.originalFileRank);
				int killCount = 0;
				for (String hold: holdList) {
					holdString = holdString + hold + ",";
					
					if (!hold.equals(oriVal))
						killCount++;
				}
				
				//holdString = holdString.substring(0, holdString.length());
				sb.append(holdString);
				sb.append(String.valueOf(killCount) + "\n");
				
				if (killCount > 0) {
					pr = new PropertyRank(key, killCount);
					this.rankList.add(pr);
				}
			}
			
			reportRank(sb);
			
			bw.write(sb.toString());
			bw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	
	private void reportRank(StringBuilder sb) {
		Collections.sort(this.rankList);
		
		sb.append("\n");
		sb.append("Killed mutant info:\n");
		for (PropertyRank pr: this.rankList) {
			sb.append(pr.getProperty() + "," + pr.getNumber() + "\n");
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
	
	public Map<String, List<String>> getHoldMap() {
		return this.holdMap;
	}
	
	public static void main(String args[]) {
		MutantAnalyzer mAnalyzer = new MutantAnalyzer("profiles", "src");
		mAnalyzer.composeOriginalFileName();
		mAnalyzer.countMutants();
		mAnalyzer.loadFiles();
		mAnalyzer.analyzeFiles();
		//mAnalyzer.summarizeFiles();
		mAnalyzer.exportSummary();
	}
}
