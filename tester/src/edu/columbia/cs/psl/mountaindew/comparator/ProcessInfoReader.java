package edu.columbia.cs.psl.mountaindew.comparator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessInfoReader extends Thread{
	
	InputStream is;
	String type;
	
	public ProcessInfoReader(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}
	
	@Override
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(this.is);
			BufferedReader br = new BufferedReader(isr);
			String buf = null;
			
			while((buf = br.readLine()) != null) {
				System.out.println(this.type + ">>" + buf);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
