package edu.columbia.cs.psl.mountaindew.example.mutant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class MutantCompiler {
	
	//private static String mutantRoot = "mutants/double_arrayDiv(int)/";
	
	private static String mutantRoot = "";
	
	private static String oriRoot = "";
	
	//private static String copyFileDir = mutantRoot + "integrate/";
	
	//private static String copyFileDir = "src/edu/columbia/cs/psl/mountaindew/example/mutantsource/";
	
	private static String copyFileDir = "src/";
	
	private static String copyFileBinDir = "bin/";
	
	private static String fileExtension = "java";
	
	private static String targetClass = "";
	
	private static String ClassSig = "public class ";
	
	private static String targetClassInFile = "";
	
	//private static String targetMethod = "public double[] arrayDiv(int[] in)";
	
	private static String targetMethod = "";
	
	private static String metamorphicTag = "@Metamorphic";
	
	private static String newLine = "\n";
	
	//private static String packageName = "package edu.columbia.cs.psl.mountaindew.example";
	
	private static String propertyFile = "config/mutant.property";
	
	private List<File> mutantFileList = new ArrayList<File>();
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
	
	private String rtCopyFileDir;
	
	private String rtCopyFileBinDir;
	
	private String timeTag;
	
	private static String getTimeMark() {
		Date date = new Date();
		return MutantCompiler.formatter.format(date);
	}
	
	public MutantCompiler() {
		this.loadMutantProperty();
	}
	
	public synchronized void loadMutantProperty() {
		File propertyFile = new File(MutantCompiler.propertyFile);
		
		if (!propertyFile.exists()) {
			System.err.println("Mutant property file does not exist");
			return ;
		}
		
		Properties mutantProperty = new Properties();
		FileInputStream fs;
		FileOutputStream fos;
		try {
			fs = new FileInputStream(propertyFile);
			mutantProperty.load(fs);
			
			MutantCompiler.mutantRoot = mutantProperty.getProperty("mutantsource");
			MutantCompiler.oriRoot = mutantProperty.getProperty("orisource");
			MutantCompiler.targetClass = mutantProperty.getProperty("targetclass");
			MutantCompiler.targetMethod = mutantProperty.getProperty("targetmethod").replaceAll("\"", "");
			
			MutantCompiler.targetClassInFile = ClassSig + targetClass;
			
			//Set up time tage for executor to store raw csv files
			this.timeTag = MutantCompiler.getTimeMark();
			mutantProperty.setProperty("timetag", this.timeTag);
			fos = new FileOutputStream(propertyFile);
			mutantProperty.store(fos, null);
			
			fs.close();
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		System.out.println("Confirm target class: " + MutantCompiler.targetClass);
		System.out.println("Confirm target method: " + MutantCompiler.targetMethod);
	}
	
	public void copyModifyCorrespondingFiles() throws IOException {				
		//Construct a integration directory for all mutant java files after inserting @Metamorphic
		this.rtCopyFileDir = MutantCompiler.copyFileDir + this.timeTag +"/";
		this.rtCopyFileBinDir = MutantCompiler.copyFileBinDir + this.timeTag + "/";
		
		//File integrateFile = new File(copyFileDir);
		File integrateFile = new File(this.rtCopyFileDir);
		if (!integrateFile.exists()) {
			boolean success = integrateFile.mkdir();
					
			if (!success) {
				System.err.println("Integration src directory creation fails");
				return ;
			} else {
				System.out.println("Confirm integration src directory: " + integrateFile.getCanonicalPath());
			}
		} else {
			System.out.println("Confirm integration src directory: " + integrateFile.getCanonicalPath());
			//deleteFiles(integrateFile);
		}
				
		//File binIntegrateFile = new File(copyFileBinDir);
		File binIntegrateFile = new File(this.rtCopyFileBinDir);
		if (!binIntegrateFile.exists()) {
			boolean success = binIntegrateFile.mkdir();
					
			if (!success) {
				System.err.println("Integration bin directory creation fails");
				return ;
			} else {
				System.out.println("Confirm integration bin directoyr: " + binIntegrateFile.getCanonicalPath());
			}
		} else {
			System.out.println("Confirm integration bin directory: " + binIntegrateFile.getCanonicalPath());
		}
		
		//copyOriFile();
		File oriRootDir = new File(oriRoot);
				
		if (!oriRootDir.exists()) {
			System.err.println("Original file directory does not exists");
			return ;
		}
				
		traverseFile(oriRootDir);
		System.out.println("Original file duplication completes");
				
		File mutantRootDir = new File(mutantRoot);
				
		if (!mutantRootDir.exists()) {
			System.err.println("Mutant directory does not exists");
			return ;
		}
				
		System.out.println("Confirm mutant root directory: " + mutantRootDir.getCanonicalPath());

		this.traverseFile(mutantRootDir);
		System.out.println("Mutant modification completes");
	}

	private void traverseFile(File rootDir) {
		File[] childFiles;
		
		if (rootDir.getName().equals("integrate"))
			return ;
		
		if (rootDir.isDirectory()) {
			childFiles = rootDir.listFiles();
			
			for (File cFile: childFiles) {
				this.traverseFile(cFile);
			}
		} else {
			String fileName = rootDir.getName();
			int i = fileName.lastIndexOf(".");
			String tmpExtension = fileName.substring(i+1, fileName.length());
			
			if (tmpExtension.equals(fileExtension)) {
				this.modifyFile(rootDir, rootDir.getParentFile().getName());
			}
		}
	}
	
	private void modifyFile(File targetFile, String parentDir) {
		Scanner readin = null;
		//String copyFileName = MutantCompiler.copyFileDir + MutantCompiler.targetClass + parentDir + ".java";
		String copyFileName = this.rtCopyFileDir + MutantCompiler.targetClass + parentDir + ".java";
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
					//System.out.println("Content: " + content);
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
		this.mutantFileList.add(newFile);
	}
	
	public void compileFiles() {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null);
		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();
		//String[] options = new String[]{"-d", "bin", "-sourcepath", "src", "-cp", "./bin:lib/columbus2.jar"};
		String[] options = new String[]{"-d", this.rtCopyFileBinDir, "-sourcepath", this.rtCopyFileDir, "-cp", "./bin:lib/columbus2.jar"};
		List<String> optionList = Arrays.asList(options);
		
		Iterable<? extends JavaFileObject> javaFiles = fileManager.getJavaFileObjectsFromFiles(this.mutantFileList);
		
		CompilationTask compileTasks = compiler.getTask(null, fileManager, collector, optionList, null, javaFiles);
		
		boolean status = compileTasks.call();
		
		if (!status) {
			for (Diagnostic<? extends JavaFileObject> diagnostic: collector.getDiagnostics()) {
				System.out.format("Line number: %d error msg: %s\n", diagnostic.getLineNumber(), diagnostic);
			}
		} else {
			System.out.println("Compilation succeeds.");
		}
		
		try {
			fileManager.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public String getTimeTag() {
		return this.timeTag;
	}
	
	public String getRtCopyFileDir() {
		return this.rtCopyFileDir;
	}
	
	public String getRtCopyBinFileDir() {
		return this.rtCopyFileBinDir;
	}
	
	/*private static void copyOriFile() throws IOException {
		String oriFileName = MutantCompiler.oriRoot + targetClass + ".java";
		String copyFileName = MutantCompiler.copyFileDir + targetClass + ".java";
		File oriFile = new File(oriFileName);
		
		if (!oriFile.exists()) {
			System.err.println("Original file does not exists");
			return ;
		}
		
		System.out.println("Confirm original file directory: " + oriFile.getCanonicalPath());
		
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
		mutantFileList.add(oriFile);
	}*/
	
	private void deleteFiles(File file) {
		File[] childFiles = file.listFiles();
		if (file.isDirectory()) {
			for (File childFile: childFiles) {
				deleteFiles(childFile);
			}
		} else {
			file.delete();
		}
	}
	
	/*public static void main (String args[]) throws IOException {
		
		File propertyFile = new File(MutantCompiler.propertyFile);
		
		if (!propertyFile.exists()) {
			System.err.println("Mutant property file does not exist");
			return ;
		}
		
		Properties mutantProperty = new Properties();
		FileInputStream fs;
		try {
			fs = new FileInputStream(propertyFile);
			mutantProperty.load(fs);
			
			MutantCompiler.mutantRoot = mutantProperty.getProperty("mutantsource");
			MutantCompiler.oriRoot = mutantProperty.getProperty("orisource");
			MutantCompiler.targetClass = mutantProperty.getProperty("targetclass");
			MutantCompiler.targetMethod = mutantProperty.getProperty("targetmethod").replaceAll("\"", "");
			
			MutantCompiler.targetClassInFile = ClassSig + targetClass;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		System.out.println("Confirm target class: " + MutantCompiler.targetClass);
		System.out.println("Confirm target method: " + MutantCompiler.targetMethod);
		
		File oriRootDir = new File(oriRoot);
		
		if (!oriRootDir.exists()) {
			System.err.println("Original file directory does not exists");
			return ;
		}
		
		traverseFile(oriRootDir);
		System.out.println("Original file duplication completes");
		
		File mutantRootDir = new File(mutantRoot);
		
		if (!mutantRootDir.exists()) {
			System.err.println("Mutant directory does not exists");
			return ;
		}
		
		System.out.println("Confirm mutant root directory: " + mutantRootDir.getCanonicalPath());
		
		String curTimeMark = MutantCompiler.getTimeMark();
		MutantCompiler.copyFileDir = MutantCompiler.copyFileDir + curTimeMark;
		MutantCompiler.copyFileBinDir = MutantCompiler.copyFileBinDir + curTimeMark;
				
		File integrateFile = new File(copyFileDir);
		if (!integrateFile.exists()) {
			boolean success = integrateFile.mkdir();
			
			if (!success) {
				System.err.println("Integration src directory creation fails");
				return ;
			} else {
				System.out.println("Confirm integration src directory: " + integrateFile.getCanonicalPath());
			}
		} else {
			System.out.println("Confirm integration src directory: " + integrateFile.getCanonicalPath());
		}
		
		File binIntegrateFile = new File(copyFileBinDir);
		if (!binIntegrateFile.exists()) {
			boolean success = binIntegrateFile.mkdir();
			
			if (!success) {
				System.err.println("Integration bin directory creation fails");
				return ;
			} else {
				System.out.println("Confirm integration bin directoyr: " + binIntegrateFile.getCanonicalPath());
			}
		} else {
			System.out.println("Confirm integration bin directory: " + binIntegrateFile.getCanonicalPath());
		}

		traverseFile(mutantRootDir);
		System.out.println("Mutant modification completes");
		
		compileFiles();
	}*/
}
