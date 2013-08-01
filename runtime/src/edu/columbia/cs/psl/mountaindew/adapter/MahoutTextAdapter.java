package edu.columbia.cs.psl.mountaindew.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

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
	
	private String tmpCopyDir;
	
	private Configuration conf;
	
	private FileSystem fs;
	
	private List<Text> keyList;

	@Override
	public Object unboxInput(Object input) {
		// unboxInput for text mining should take the dir string as input, retrieve tfidf vectors and give a double[][] back
		
		if (String.class.isAssignableFrom(input.getClass())) {
			String srcDirPath = input.toString();
			File srcDirFile = new File(srcDirPath);
			if (!srcDirFile.exists()) {
				System.err.println("Source director does not exist: " + srcDirFile.getAbsolutePath());
				return null;
			}
			
			tmpCopyDir = srcDirPath + "_copy";
			keyList = new ArrayList<Text>();
			
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
				
				String srcTFPath = srcDirPath + "/vec/tfidf-vectors/part-r-00000";
				
				SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(srcTFPath), conf);
				Text key = new Text();
				VectorWritable val = new VectorWritable();
				
				Vector tmpVec;
				List<Vector> vecList = new ArrayList<Vector>();
				while(reader.next(key, val)) {
					System.out.println("Key: " + key);
					System.out.println("Ori val: " + val.get());
					
					keyList.add(key);
					
					tmpVec = val.get();
					vecList.add(tmpVec);
				}
				reader.close();
				
				int dataNum = vecList.size();
				int dataLength = vecList.get(0).size();
				
				System.out.println("Check input data number: " + dataNum);
				System.out.println("Check input data length: " + dataLength);
				
				double[][] ret = new double[dataNum][dataLength];
				
				
				for (int i = 0 ;i < dataNum; i++) {
					tmpVec = vecList.get(i);
					//System.out.println("Ori: " + tmpVec);
					for (int j = 0; j < dataLength; j++) {
						ret[i][j] = tmpVec.get(j);
					}
				}
				
				/*System.out.println("Check new lda base directory: " + tmpCopyDir);
				System.out.println("Check new lda matrix: " + destFile.getAbsolutePath());
				return tmpCopyDir;*/
				
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
		
		File destFile = new File(this.tmpCopyDir + "/vec/tfidf-vectors/part-r-00000");
		
		if (destFile.exists()) {
			destFile.delete();
		}
		
		try {
			SequenceFile.Writer writer = new SequenceFile.Writer(fs, 
					conf, new Path(destFile.getAbsolutePath()), Text.class, VectorWritable.class);
			
			Vector transform;
			VectorWritable transformWritable;
			
			System.out.println("Check transform input data number: " + dataNum);
			System.out.println("Check transform input data length: " + realInput[0].length);
			
			for (int i = 0; i < dataNum; i++) {
				transform = new DenseVector(realInput[i].length);
				transform.assign(realInput[i]);
				//System.out.println("Trans: " + transform);
				
				transformWritable = new VectorWritable(transform);
				
				writer.append(this.keyList.get(i), transformWritable);
			}
			
			writer.close();
				
			System.out.println("Check new transform dir: " + tmpCopyDir);
			return tmpCopyDir;
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
				
				System.out.println("Trans output data num: " + dataNum);
				System.out.println("Trans output data size: " + dataSize);
				
				double[][] ret = new double[dataNum][dataSize];
				
				Vector tmpVec;
				for (int i = 0; i < ret.length; i++) {
					tmpVec = realOutput.get(i);
					System.out.println("Check trans output: " + tmpVec);
					for (int j = 0; j < dataSize; j++) {
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
		
	}

}
