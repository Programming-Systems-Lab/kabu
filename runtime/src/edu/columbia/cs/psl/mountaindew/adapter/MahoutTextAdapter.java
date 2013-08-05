package edu.columbia.cs.psl.mountaindew.adapter;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;


public class MahoutTextAdapter extends AbstractAdapter{
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSS");
	
	private String tmpCopyDir;
	
	private String copyMatricPath;
	
	private String targetMatricFilePath;
	
	private Configuration conf;
	
	private FileSystem fs;
	
	private List keyList = new ArrayList();
	
	private HashMap keyMap = new HashMap();
	
	private Class keyClass;
	
	private Class valueClass;

	@Override
	public Object unboxInput(Object input) {
		// unboxInput for text mining should take the dir string as input, retrieve tfidf vectors and give a double[][] back
		
		if (input.getClass().isArray() && String.class.isAssignableFrom(Array.get(input, 0).getClass())) {
			
			String srcDirPath = (String)Array.get(input, 0);
			String matricPath = (String)Array.get(input, 1);
			File srcDirFile = new File(srcDirPath);
			if (!srcDirFile.exists()) {
				System.err.println("Source director does not exist: " + srcDirFile.getAbsolutePath());
				return null;
			}
			
			String dirIdentifier = formatter.format(new Date());
			
			tmpCopyDir = srcDirPath + "_sandbox/" + dirIdentifier;
			copyMatricPath = matricPath;
			
			Path srcPath = new Path(srcDirPath);
			File destPath = new File(tmpCopyDir);
			
			if (destPath.exists()) {
				try {
					FileUtil.fullyDelete(destPath);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
				
			try {
				conf = new Configuration();
				fs = FileSystem.get(conf);
				
				boolean copySuccess = FileUtil.copy(fs, srcPath, destPath, false, conf);
				
				if (copySuccess) {
					System.out.println("Directory copying succeeds: " + srcPath.toString() + " " + destPath.getAbsolutePath());
				} else {
					System.err.println("Directory copying fails: " + destPath.getAbsolutePath());
					return null;
				}
				
				SequenceFile.Reader reader;
				List<Vector> vecList = new ArrayList<Vector>();
				if (matricPath.contains("tfidf-vectors") || matricPath.contains("tf-vectors")) {
					targetMatricFilePath = destPath.getAbsolutePath() + matricPath + "/part-r-00000";
					this.keyClass = Text.class;
					this.valueClass = VectorWritable.class;
					
					System.out.println("Confirm targetMatricFilePath: " + targetMatricFilePath);
					
					reader = new SequenceFile.Reader(fs, new Path(targetMatricFilePath), conf);
					Text key = new Text();
					VectorWritable val = new VectorWritable();
					
					Vector tmpVec;
					while(reader.next(key, val)) {
						System.out.println("Key: " + key);
						System.out.println("Ori val: " + val.get());
						
						Text copyKey = new Text();
						copyKey.set(key.toString());
						
						tmpVec = val.get();
						
						keyList.add(copyKey);
						keyMap.put(tmpVec, copyKey);
						
						vecList.add(tmpVec);
					}
					reader.close();
				} else {
					targetMatricFilePath = destPath.getAbsolutePath() + matricPath + "/matrix";
					this.keyClass = IntWritable.class;
					this.valueClass = VectorWritable.class;
					
					System.out.println("Confirm targetMatricFilePath: " + targetMatricFilePath);
					
					reader = new SequenceFile.Reader(fs, new Path(targetMatricFilePath), conf);
					IntWritable key = new IntWritable();
					VectorWritable val = new VectorWritable();
					
					Vector tmpVec;
					while(reader.next(key, val)) {
						System.out.println("Key: " + key);
						System.out.println("Ori val: " + val.get());
						
						IntWritable copyKey = new IntWritable();
						copyKey.set(key.get());

						
						tmpVec = val.get();
						
						keyList.add(copyKey);
						keyMap.put(tmpVec, copyKey);
						
						vecList.add(tmpVec);
					}
					reader.close();					
				}
				
				int dataNum = vecList.size();
				int dataLength = vecList.get(0).size();
				
				double[][] ret = new double[dataNum][dataLength];
				
				Vector tmpVec;
				for (int i = 0 ;i < dataNum; i++) {
					tmpVec = vecList.get(i);
					for (int j = 0; j < dataLength; j++) {
						ret[i][j] = tmpVec.get(j);
					}
				}
				
				return ret;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		return null;
	}
	
	@Override
	public Object adaptInput(Object transInput) {
		double[][] realInput = (double[][])transInput;
		int dataNum = realInput.length;
		
		File targetMatricFile = new File(this.targetMatricFilePath);
		
		if (targetMatricFile.exists()) {
			targetMatricFile.delete();
		}
		
		
		try {
			SequenceFile.Writer writer = new SequenceFile.Writer(fs, 
					conf, new Path(targetMatricFile.getAbsolutePath()), this.keyClass, this.valueClass);
			
			Vector transform;
			VectorWritable transformWritable;
			
			System.out.println("Traverse keyMap");
			for (Object key: this.keyMap.keySet()) {
				System.out.println("Key: " + key);
				System.out.println("Val: " + this.keyMap.get(key));
			}
			
			for (int i = 0; i < dataNum; i++) {
				transform = new DenseVector(realInput[i].length);
				transform.assign(realInput[i]);
				//System.out.println("Trans: key val " + this.keyList.get(i) + " " + transform);
				
				transformWritable = new VectorWritable(transform);
				
				writer.append(this.keyMap.get(transform), transformWritable);
			}
			
			writer.close();
							
			System.out.println("Check new transform dir: " + tmpCopyDir + " " + copyMatricPath);
			return new String[]{tmpCopyDir, copyMatricPath};
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Object adaptOutput(Object outputModel, Object... testingData) {
		if (Collection.class.isAssignableFrom(outputModel.getClass())) {
			List outputList = new ArrayList((Collection)outputModel);
			
			if (Vector.class.isAssignableFrom(outputList.get(0).getClass())) {
				List<Vector> realOutput = (List<Vector>)outputList;
				
				int dataNum = realOutput.size();
				int dataSize = realOutput.get(0).size();
				
				double[][] ret = new double[dataNum][dataSize];
				
				Vector tmpVec;
				for (int i = 0; i < ret.length; i++) {
					tmpVec = realOutput.get(i);
					for (int j = 0; j < dataSize; j++) {
						ret[i][j] = tmpVec.get(j);
					}
				}
				
				return ret;
			}
		} else if (Map.class.isAssignableFrom(outputModel.getClass())) {
			Map outputMap = (Map)outputModel;
			
			Object keySentinel = outputMap.keySet().iterator().next();
			Object valSentinel = outputMap.get(keySentinel);
			
			if (Number.class.isAssignableFrom(keySentinel.getClass()) && Vector.class.isAssignableFrom(valSentinel.getClass())) {
				Map<Number, Vector>realMap = (Map<Number, Vector>)outputMap;
				
				int dataNum = realMap.size();
				int dataLen = realMap.get(realMap.keySet().iterator().next()).size();
				
				double[][] ret = new double[dataNum][dataLen];
				
				Vector tmpVec;
				for (int i = 0; i < dataNum; i++) {
					tmpVec = realMap.get(i);
					for (int j = 0; j < dataLen; j++) {
						ret[i][j] = tmpVec.get(j);
					}
				}
				
				return ret;
			}
		}
		
		return null;
	}

	@Override
	public List<Object> skipColumn(Object input) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void complementTransformInput(Object input) {
		double[][] realInput = (double[][])input;
		this.keyMap = new HashMap();
		Vector tmpVector;
		for (int i = 0; i < realInput.length; i++) {
			tmpVector = new DenseVector(realInput[i]);
			this.keyMap.put(tmpVector, this.keyList.get(i));
			
			//System.out.println("Check name vec: " + this.nameList.get(i) + " " + tmpVector);
		}
	}

}
