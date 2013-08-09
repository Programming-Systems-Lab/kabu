package edu.columbia.cs.psl.mountaindew.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import edu.columbia.cs.psl.invivo.runtime.InvivoPreMain;




public class MetamorphicInjector {
	
	private static String configRoot = "config/";
	
	private static String metaPropertyFile = "metamorphic.property";
	
	private static String mutantPropertyFile = "mutant.property";
	
	public void go(String[] args)
	{
//		InvivoPreMain.config = new Configuration();
//		InterceptorClassLoader l = new InterceptorClassLoader();
//		Thread.currentThread().setContextClassLoader(l);
		try {
//			System.out.println("Check args: " + args[0]);
			Class<?> c = Class.forName(args[0]);
			String[] args2 = new String[args.length-1];
			for(int i = 1; i<args.length;i++)
				args2[i-1] = args[i];
			c.getMethod("main", String[].class).invoke(null, (Object) args2);
			
			System.out.println("Reporting results");
			MetamorphicObserver.getInstance().reportResults();
//			c.getMethod("go",String.class).invoke(c.newInstance(),"zzz");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		SimpleExample.main(args);
	}
	
	public static void main(String[] args) {
		if(args.length == 0)
		{
			System.err.println("Usage: java edu.columbia.cs.psl.metamorphic.runtime.MetamorphicInjector nameOfClassWithMain [Optional arguments for said class]");
			System.exit(0);
		}
		//new MetamorphicInjector().go(args);
		
		File metaPropFile = new File("config/metamorphic.property");
		
		if (!metaPropFile.exists()) {
			System.err.println("Load no metamorphic configuration file: " + metaPropFile.getAbsolutePath());
			return ;
		}
		
		try {
			Properties metaProp = new Properties();
			metaProp.load(new FileInputStream(metaPropFile));
			
			int roundNumber = Integer.valueOf(metaProp.getProperty("Round"));
			
			System.out.println("Confirm round number: " + roundNumber);
			
			for (int i = 0; i < roundNumber; i++) {
				System.out.println("Round execution: " + i);
				new MetamorphicInjector().go(args);
				System.out.println();
			}
			
			cleanPropertyFiles();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static void cleanPropertyFiles() {
		File configDir = new File(configRoot);
		String tmpFileName;
		for (File tmpFile: configDir.listFiles()) {
			tmpFileName = tmpFile.getName();
			if (tmpFileName.equals(metaPropertyFile))
				continue;
			
			if (tmpFileName.equals(mutantPropertyFile))
				continue;
			
			tmpFile.delete();
		}
	}
}

