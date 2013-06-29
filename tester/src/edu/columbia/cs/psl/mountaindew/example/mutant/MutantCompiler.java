package edu.columbia.cs.psl.mountaindew.example.mutant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;

public class MutantCompiler {
	
	//private static String mutantRoot = "mutants/double_arrayDiv(int)/";
	
	private static String mutantRoot = "";
	
	private static String oriRoot = "";
	
	//private static String copyFileDir = mutantRoot + "integrate/";
	
	//private static String copyFileDir = "src/edu/columbia/cs/psl/mountaindew/example/mutantsource/";
	
	private static String copyFileDir = "src/";
	
	private static String fileExtension = "java";
	
	private static String targetClass = "SimpleExample";
	
	private static String ClassSig = "public class ";
	
	private static String targetClassInFile = ClassSig + targetClass;
	
	//private static String targetMethod = "public double[] arrayDiv(int[] in)";
	
	private static String targetMethod = "";
	
	private static String metamorphicTag = "@Metamorphic";
	
	private static String newLine = "\n";
	
	private static String packageName = "package edu.columbia.cs.psl.mountaindew.example";
	
	//private static String addPack = ".mutantsource;";
	
	private static int copyID = 0;
	
	public static void main (String args[]) {
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Please input the mutant source directory");
		MutantCompiler.mutantRoot = scanner.nextLine();
		
		System.out.println("Please input the original source directory");
		MutantCompiler.oriRoot = scanner.nextLine();
		
		/*System.out.println("Please input the target source direoctry");
		MutantCompiler.copyFileDir = scanner.nextLine();*/
		
		System.out.println("Please input the target method for tagging");
		MutantCompiler.targetMethod = scanner.nextLine();
		
		copyOriFile();
		System.out.println("Original file duplication completes");
		
		File mutantRootDir = new File(mutantRoot);
		
		if (!mutantRootDir.exists()) {
			System.err.println("Mutant directory does not exists");
			return ;
		}
		
		System.out.println("Confirm mutant root directory: " + mutantRootDir.getAbsolutePath());
		
		//Construct a integration directory for all mutant java files after inserting @Metamorphic
		File integrateFile = new File(copyFileDir);
		if (!integrateFile.exists()) {
			boolean success = integrateFile.mkdir();
			
			if (!success) {
				System.err.println("Integration directory creation fails");
				return ;
			} else {
				System.out.println("Confirm integration directory: " + integrateFile.getAbsolutePath());
			}
		} else {
			System.out.println("Confirm integration directory: " + integrateFile.getAbsolutePath());
			//deleteFiles(integrateFile);
		}
		
		System.out.println("Confirm target method: " + MutantCompiler.targetMethod);

		traverseFile(mutantRootDir);
		System.out.println("Mutant modification completes");
	}
	
	private static void copyOriFile() {
		String oriFileName = MutantCompiler.oriRoot + targetClass + ".java";
		String copyFileName = MutantCompiler.copyFileDir + targetClass + ".java";
		File oriFile = new File(oriFileName);
		
		if (!oriFile.exists()) {
			System.err.println("Original file does not exists");
			return ;
		}
		
		File copyFile = new File(copyFileName);
		
		BufferedWriter bw = null;
		Scanner oriScanner = null;
		
		try {
			bw = new BufferedWriter(new FileWriter(copyFile));
			oriScanner = new Scanner(new FileReader(oriFile));
			String content;
			
			while(oriScanner.hasNextLine()) {
				content = oriScanner.nextLine();
				bw.append(content + "\n");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				bw.close();
				oriScanner.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private static void deleteFiles(File file) {
		File[] childFiles = file.listFiles();
		if (file.isDirectory()) {
			for (File childFile: childFiles) {
				deleteFiles(childFile);
			}
		} else {
			file.delete();
		}
	}
	
	private static void traverseFile(File rootDir) {
		File[] childFiles;
		
		if (rootDir.getName().equals("integrate"))
			return ;
		
		if (rootDir.isDirectory()) {
			childFiles = rootDir.listFiles();
			
			for (File cFile: childFiles) {
				MutantCompiler.traverseFile(cFile);
			}
		} else {
			String fileName = rootDir.getName();
			int i = fileName.lastIndexOf(".");
			String tmpExtension = fileName.substring(i+1, fileName.length());
			
			if (tmpExtension.equals(fileExtension)) {
				MutantCompiler.modifyFile(rootDir, rootDir.getParentFile().getName());
			}
		}
	}
	
	private static void modifyFile(File targetFile, String parentDir) {
		Scanner readin = null;
		String copyFileName = MutantCompiler.copyFileDir + MutantCompiler.targetClass + parentDir + ".java";
		File newFile = new File(copyFileName);
		BufferedWriter newBufferWriter = null;
		
		try {
			readin = new Scanner(new FileReader(targetFile));
			newBufferWriter = new BufferedWriter(new FileWriter(newFile));
			int methodIndex = Integer.MIN_VALUE;
			int classIndex = Integer.MIN_VALUE;
			//int packIndex = Integer.MIN_VALUE;
			int objIndex = Integer.MIN_VALUE;
			String modifiedClass = MutantCompiler.targetClass + parentDir;
			
			System.out.println("Original class: " + MutantCompiler.targetClass); 
			System.out.println("Modified class: " + modifiedClass);
			String content;
			String methodCheck;
			
			while(readin.hasNextLine()) {
				content = readin.nextLine();
				//methodIndex = content.indexOf(targetMethod);
				methodCheck = content.replaceAll(" ", "");
				methodIndex = methodCheck.indexOf(targetMethod.replaceAll(" ", ""));
				classIndex = content.indexOf(targetClassInFile);
				objIndex = content.indexOf(targetClass);
				//packIndex = content.indexOf(packageName);
				
				
				if (classIndex >= 0) {
					newBufferWriter.append(MutantCompiler.metamorphicTag + MutantCompiler.newLine);
					newBufferWriter.append(targetClassInFile + parentDir);
				} else if (objIndex >= 0) {
					content = content.replaceAll(targetClass, modifiedClass);
					System.out.println("Content: " + content);
					newBufferWriter.append(content + MutantCompiler.newLine);
				} else if (methodIndex >= 0) {
					newBufferWriter.append(MutantCompiler.metamorphicTag + MutantCompiler.newLine);
					newBufferWriter.append(content + MutantCompiler.newLine);
				} else {
					newBufferWriter.append(content + MutantCompiler.newLine);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				readin.close();
				newBufferWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
