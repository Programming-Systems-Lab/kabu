package edu.columbia.cs.psl.mountaindew.example.mutant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;

public class MutantCompiler {
	
	private static String mutantRoot = "mutants/double_arrayDiv(int)/";
	
	//private static String copyFileDir = mutantRoot + "integrate/";
	
	private static String copyFileDir = "src/edu/columbia/cs/psl/mountaindew/example/mutantsource/";
	
	private static String fileExtension = "java";
	
	private static String targetClass = "SimpleExample";
	
	private static String ClassSig = "public class ";
	
	private static String targetClassInFile = ClassSig + targetClass;
	
	private static String targetMethod = "public  double[] arrayDiv( int[] in )";
	
	private static String metamorphicTag = "@Metamorphic";
	
	private static String newLine = "\n";
	
	private static String packageName = "package edu.columbia.cs.psl.mountaindew.example";
	
	private static String addPack = ".mutantsource;";
	
	private static int copyID = 0;
	
	public static void main (String args[]) {
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
			System.out.println("Confrim integration directory: " + integrateFile.getAbsolutePath());
			deleteFiles(integrateFile);
		}
		
		traverseFile(mutantRootDir);
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
			int packIndex = Integer.MIN_VALUE;
			String content;
			
			while(readin.hasNextLine()) {
				content = readin.nextLine();
				methodIndex = content.indexOf(targetMethod);
				classIndex = content.indexOf(targetClassInFile);
				packIndex = content.indexOf(packageName);
				
				if (packIndex >= 0 ) {
					newBufferWriter.append(MutantCompiler.packageName + MutantCompiler.addPack);
				} else if (classIndex >= 0) {
					newBufferWriter.append(MutantCompiler.metamorphicTag + MutantCompiler.newLine);
					newBufferWriter.append(targetClassInFile + parentDir);
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
