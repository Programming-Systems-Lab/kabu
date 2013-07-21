package edu.columbia.cs.psl.mountaindew.runtime;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ObjectCloner {

	public static synchronized Object deepCloneBySerialize(Object obj) {
		
		Object cloneObj = null;
		
		try {			
			File objTmp = new File("objTmp");
			
			if (!objTmp.exists()) {
				objTmp.mkdir();
			}
			
			File tmpObjFile = new File(objTmp.getAbsolutePath() + "/tmp.ser");
			if (tmpObjFile.exists()) {
				tmpObjFile.delete();
			}
			
			OutputStream selFile = new FileOutputStream(tmpObjFile.getAbsolutePath());
			OutputStream buf = new BufferedOutputStream(selFile);
			ObjectOutputStream oos = new ObjectOutputStream(buf);
			
			oos.writeObject(obj);
			oos.close();
			
			InputStream deselFile = new FileInputStream(tmpObjFile.getAbsolutePath());
			InputStream inputBuf = new BufferedInputStream(deselFile);
			ObjectInputStream ois = new ObjectInputStream(inputBuf);
			
			Object retObj = ois.readObject();
			ois.close();
			
			return retObj;
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
		
	}

}
