package edu.columbia.cs.psl.mountaindew.runtime;


import java.lang.instrument.Instrumentation;

import edu.columbia.cs.psl.invivo.runtime.InvivoPreMain;


public class PreMain {
	public static void premain(String args, Instrumentation inst) {
		InvivoPreMain.premain(args, inst,new Configuration());
	}
}
