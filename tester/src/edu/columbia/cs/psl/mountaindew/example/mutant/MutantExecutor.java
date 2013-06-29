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
	
	//private static String cparg = "./bin:\"lib/*\"";
	//private static String cparg = "bin:/Users/mike/Documents/metamorphic-projects/mountaindew/tester/lib/*";
	
	private static String cparg = "";
	
	private static String commandInject = "edu.columbia.cs.psl.mountaindew.runtime.MetamorphicInjector";
	
	private static ArrayList<File> filterFiles = new ArrayList<File>();
	
	private static ExecutorThread[] eArray;
	
	private static int inExecution = 0;
	
	private static int completedMutants = 0;
	
	//Execution limit for each thread: 3 sec
	private static int executionLimit = 3 * 1000;
	
	private static Semaphore processLock = new Semaphore(3, true);
		
	public static void main (String args[]) {
		/*System.out.println("Please input the bin directory for executing");
		Scanner scanner = new Scanner(System.in);
		
		binString = scanner.nextLine();
		
		File binDir = new File(binString);
		
		if (!binDir.isDirectory()) {
			System.err.println("Invalid bin directory");
			return ;
		}*/
		
		File binDir = new File("bin");
		
		if (!binDir.exists()) {
			System.err.println("Cannot find bin folder for executing");
			return ;
		}
		
		System.out.println("Confirm bin direcotry: " + binDir.getAbsolutePath());
		
		filterClassFiles(binDir);
		
		//Find out the issue for multithread
		eArray = new ExecutorThread[filterFiles.size()];
		//eArray = new ExecutorThread[9];
		
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
		
		/*for (File file: filterFiles) {
			String targetClass = parsePathToPackage(file.getAbsolutePath());
			
			while (inExecution == 0) {
				if (executeCommand(targetClass)) {
					inExecution = 1;
				}	
				System.out.println(targetClass + " is still working");
			}
			inExecution = 0;
		}*/
		
		System.out.println("Mutant execution completes");
	}
	
	private static String parsePathToPackage(String absolutePath) {
		int pos = absolutePath.lastIndexOf("bin/") + 4;
		String classPath = absolutePath.substring(pos, absolutePath.length()).replace(".class", "");
		String ret = classPath.replace("/", ".");
		
		return ret;
	}
	
	private static void filterClassFiles(File binDir) {
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
	
	private static boolean executeCommand(String targetClass) {
		Process process = null;
		try {
			File binDir = new File("bin");
			File libDir = new File("lib");
			//cparg = cparg+ binDir.getAbsolutePath() + ":" + libDir.getAbsolutePath() + "/*";
			String cparg = binDir.getAbsolutePath() + ":" + libDir.getAbsolutePath() + "/*";
			//System.out.println("CPARG dir: " + cparg);
			System.out.println("Execution process for class " + targetClass);
			
			ProcessBuilder builder = new ProcessBuilder(commandHead, memory, agent, commandCp, cparg, commandInject, targetClass);
			builder.redirectErrorStream(true);
			
			process = builder.start();

			/*for (String command: builder.command()) {
				System.out.println("Command: " + command);
			}*/
			
			printProcessMsg(process);
			printProcessErrMsg(process);
			
			int retValue = process.waitFor();
			System.out.println("Process returned value for " + targetClass + ": " + retValue);
			process.destroy();
			System.gc();
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			process.destroy();
			System.gc();
			return true;
		}
	}
	
	public static void printProcessMsg(final Process process) {
		
		//Scanner scanner = new Scanner(System.in);
		
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
	
	public static void printProcessErrMsg(final Process process) {
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
	
	public synchronized static void increAndReportMutantInfo() {
		completedMutants++;
		System.out.println("Completed mutatant: " + completedMutants);
		//System.out.println("Total mutants: " + filterFiles.size());
		System.out.println("Total mutants: " + eArray.length);
	}
	
	public static class ExecutorThread extends Thread {
		
		private String targetClass;
		
		private Process process;
		
		private ProcessBuilder builder;
		
		private int eLimit = 30 * 1000;
		
		public ExecutorThread(String targetClass) {
			this.targetClass = targetClass;
		}

		@Override
		public synchronized void run() {
			// TODO Auto-generated method stub
			try {
				processLock.acquireUninterruptibly();
				System.out.println(this.targetClass + " acquire lock " + processLock.availablePermits());
				File binDir = new File("bin");
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
