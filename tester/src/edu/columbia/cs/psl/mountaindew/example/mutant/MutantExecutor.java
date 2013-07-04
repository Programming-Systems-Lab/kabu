package edu.columbia.cs.psl.mountaindew.example.mutant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class MutantExecutor {
	
	private static String binString = "";
	
	//private static String basicCommand = "java -Xmx6000m -javaagent:lib/mountaindew.jar -cp ./bin:\"lib/*\" edu.columbia.cs.psl.mountaindew.runtime.MetamorphicInjector edu.columbia.cs.psl.mountaindew.example.SimpleExampleAOIS_101";
	
	private static String commandHead = "java";
	
	private static String memory = "-Xmx6000m";
	
	private static String agent = "-javaagent:lib/mountaindew.jar";
	
	private static String commandCp = "-cp";
	
	private static String cparg = "";
	
	private static String commandInject = "edu.columbia.cs.psl.mountaindew.runtime.MetamorphicInjector";
	
	private static int inExecution = 0;
	
	//Execution limit for each thread: 3 sec
	private static int executionLimit = 60 * 1000;
	
	private ArrayList<File> filterFiles = new ArrayList<File>();
	
	private ExecutorThread[] eArray;

	private int completedMutants = 0;
	
	private Semaphore processLock = new Semaphore(3, true);
	
	private String binFileDir;
	
	public MutantExecutor(String binFileDir) {
		this.binFileDir = binFileDir;
		this.setUpClassFiles();
	}
	
	private void setUpClassFiles() {
		File binDir = new File(this.binFileDir);
		
		if (!binDir.exists()) {
			System.err.println("Cannot find bin folder for executing");
			return ;
		}
		
		System.out.println("Confirm bin direcotry: " + binDir.getAbsolutePath());
		
		filterClassFiles(binDir);
	}
	
	public void executeMutantThreads() {
		//Find out the issue for multithread
		this.eArray = new ExecutorThread[filterFiles.size()];
		
		for (int i = 0; i < eArray.length; i++) {
			String targetClass = parsePathToPackage(filterFiles.get(i).getAbsolutePath());
			this.eArray[i] = new ExecutorThread(targetClass);
			this.eArray[i].start();
		}
		
		for (int i = 0; i < eArray.length; i++) {
			try {
				this.eArray[i].join(executionLimit);
				
				if (this.eArray[i].isAlive()) {
					System.out.println(eArray[i].getClassName() + "has not yet died. Interruption starts");
					this.eArray[i].interrupt();
				} else {
					System.out.println(eArray[i].getClassName() + " has died.");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(eArray[i].getClassName() + " is interrupted: " + eArray[i].isInterrupted());
			}
		}
		
		System.out.println("Mutant execution completes");
	}

	private static String parsePathToPackage(String absolutePath) {
		//4 for bin/ and 15 for time tag
		int pos = absolutePath.lastIndexOf("bin/") + 4 + 15;
		String classPath = absolutePath.substring(pos, absolutePath.length()).replace(".class", "");
		String ret = classPath.replace("/", ".");
		
		return ret;
	}
	
	private void filterClassFiles(File binDir) {
		String fileName;
		String tmpExtension;
		for (File childFile: binDir.listFiles()) {
			
			if (childFile.isDirectory()) {
				filterClassFiles(childFile);
			} else {
				fileName = childFile.getName();
				int i = fileName.lastIndexOf(".");
				tmpExtension = fileName.substring(i+1, fileName.length());
				
				if (!tmpExtension.equals("class"))
					continue;
				
				if (fileName.contains("_tests.class"))
					continue;
				
				filterFiles.add(childFile);
			}
		}
	}
	
	public synchronized void increAndReportMutantInfo() {
		this.completedMutants++;
		System.out.println("Completed mutatant: " + this.completedMutants);
		//System.out.println("Total mutants: " + filterFiles.size());
		System.out.println("Total mutants: " + this.eArray.length);
	}
	
	/*public static void main (String args[]) {
	
		File binDir = new File("bin");
	
		if (!binDir.exists()) {
			System.err.println("Cannot find bin folder for executing");
			return ;
		}
	
		System.out.println("Confirm bin direcotry: " + binDir.getAbsolutePath());
	
		filterClassFiles(binDir);

		eArray = new ExecutorThread[filterFiles.size()];
	
		for (int i = 0; i < eArray.length; i++) {
			String targetClass = parsePathToPackage(filterFiles.get(i).getAbsolutePath());
			eArray[i] = new ExecutorThread(targetClass);
			eArray[i].start();
		}
	
		for (int i = 0; i < eArray.length; i++) {
			try {
				eArray[i].join(executionLimit);
			
				if (eArray[i].isAlive()) {
					System.out.println(eArray[i].getClassName() + "has not yet died. Interruption starts");
					eArray[i].interrupt();
				} else {
					System.out.println(eArray[i].getClassName() + " has died.");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(eArray[i].getClassName() + " is interrupted: " + eArray[i].isInterrupted());
			}
		}
	
		System.out.println("Mutant execution completes");
	}*/
	
	public class ExecutorThread extends Thread {
		
		private String targetClass;
		
		private Process process;
		
		private ProcessBuilder builder;
		
		private int eLimit = 60 * 1000;
		
		public ExecutorThread(String targetClass) {
			this.targetClass = targetClass;
		}

		@Override
		public synchronized void run() {
			// TODO Auto-generated method stub
			try {
				processLock.acquireUninterruptibly();
				System.out.println(this.targetClass + " acquire lock " + processLock.availablePermits());
				//File binDir = new File("bin");
				File binDir = new File(binFileDir);
				File libDir = new File("lib");
				//cparg = cparg+ binDir.getAbsolutePath() + ":" + libDir.getAbsolutePath() + "/*";
				String cparg = binDir.getAbsolutePath() + ":" + libDir.getAbsolutePath() + "/*";
				//System.out.println("CPARG dir: " + cparg);
				System.out.println("Execution thread for class " + this.targetClass);
				
				builder = new ProcessBuilder(commandHead, memory, agent, commandCp, cparg, commandInject, this.targetClass);
				builder.redirectErrorStream(true);
				
				/*process = builder.start();
				printProcessMsg(process);
				printProcessErrMsg(process);
				int retValue = process.waitFor();
				System.out.println("Process returned value for " + this.targetClass + ": " + retValue);*/
				
				executeProcess();
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				cleanProcess();
			}
		}
		
		public void executeProcess() {
			Thread execution = new Thread() {
				public void run() {
					try {
						process = builder.start();
						printProcessMsg(process);
						printProcessErrMsg(process);
						
						int retValue = process.waitFor();
						System.out.println("Process returned value for " + targetClass + ": " + retValue);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			};
			
			
			try {
				execution.start();
				execution.join(eLimit);
				
				if (execution.isAlive()) {
					System.out.println("Time is up. Interrup the thread");
					execution.interrupt();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public void cleanProcess() {
			if (process != null) {
				System.out.println("Start to clean process " + this.targetClass);
				process.destroy();
				processLock.release();
				System.out.println(this.targetClass + " releases the lock");
				System.out.println("After release: " + processLock.availablePermits());
				increAndReportMutantInfo();
				this.interrupt();
			}
		}
		
		public String getClassName() {
			return this.targetClass;
		}
		
		public Process getProcess() {
			return this.process;
		}
		
		public void printProcessMsg(final Process process) {
			new Thread() {
				public void run() {
					String line;
					OutputStream stdin = process.getOutputStream();
					InputStream stdout = process.getInputStream();
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
					
					try {
						while ((line = reader.readLine()) != null) {
							if (!line.isEmpty()) {
								System.out.println("Process msg: " + line);
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						try {
							stdin.close();
							stdout.close();
							reader.close();
							writer.close();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}.start();
		}
		
		public void printProcessErrMsg(final Process process) {
			new Thread() {
				public void run() {
					InputStream stderr = process.getErrorStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(stderr));
					
					String line;
					try {
						while ((line = reader.readLine()) != null) {
							if (!line.isEmpty()) {
								System.out.println("Process err msg: " + line);
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						return ;
					} finally {
						try {
							stderr.close();
							reader.close();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}.start();
		}
	}
}
