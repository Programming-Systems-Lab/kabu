package edu.columbia.cs.psl.mountaindew.adapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Collection;

public class PythonScikitAdapter extends AbstractAdapter{
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSS");
	
	private String transDatapath = null;
	
	private String dataHeader = null;
	
	private int classAttr = -1;
	
	private Map<ArrayList<Number>, Integer> dataMap = new HashMap<ArrayList<Number>, Integer>();
	
	private List<Number> dataList = new ArrayList<Number>();
	
	@Override
	public synchronized Object unboxInput(Object input) {
		if (String.class.isAssignableFrom(input.getClass())) {
			String oriDatapath = (String)input;
			
			File oriFile = new File(oriDatapath);
			
			if (!oriFile.exists()) {
				System.err.println("Original data file does not exist");
				return null;
			}
			
			String now = formatter.format(new Date()) + ".csv";
			
			this.transDatapath = oriDatapath.replace(".csv", now);
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(oriFile));
				
				//Reade header
				String tmp = br.readLine();
				
				this.dataHeader = tmp;
				
				StringTokenizer st = new StringTokenizer(tmp, ",");
				int count = 0;
				int dataNum = 0;
				int attrNum = 0;
				ArrayList<String> attrList = new ArrayList<String>();
				while(st.hasMoreTokens()) {
					if (count == 0 ){
						dataNum = Integer.valueOf(st.nextToken());
						count++;
					} else if (count == 1) {
						attrNum = Integer.valueOf(st.nextToken());
						count++;
					} else if (count > 1) {
						attrList.add(st.nextToken());
						count++;
					}
				}
				
				this.classAttr = attrNum;
				
				ArrayList<ArrayList<Number>> ret = new ArrayList<ArrayList<Number>>();
				while((tmp = br.readLine()) != null) {
					st = new StringTokenizer(tmp, ",");
					
					ArrayList<Number> innerList = new ArrayList<Number>();
					int attrCount = 0;
					while(st.hasMoreTokens()) {
						if (attrCount == classAttr) {
							innerList.add(Integer.valueOf(st.nextToken()));
						} else {
							innerList.add(Double.valueOf(st.nextToken()));
						}
						attrCount++;
					}
					ret.add(innerList);
					
					int classVal = innerList.get(classAttr).intValue();
					this.dataMap.put(innerList, classVal);
					this.dataList.add(classVal);
				}
				
				br.close();
				return ret;
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}			
		}
		return null;
	}

	@Override
	public Object adaptInput(Object transInput) {
		ArrayList<ArrayList<Number>> transInputContent = (ArrayList<ArrayList<Number>>)transInput;
		
		StringBuilder tableBuilder = new StringBuilder();
		tableBuilder.append(this.dataHeader + "\n");
		for (ArrayList<Number>rawdata: transInputContent) {
			StringBuilder rawDataBuilder = new StringBuilder();
			
			for (Number tmpVal: rawdata) {
				rawDataBuilder.append(tmpVal);
				rawDataBuilder.append(",");
			}
			
			String rawDataString = rawDataBuilder.toString();
			rawDataString = rawDataString.substring(0, rawDataString.length() - 1) + "\n";
			tableBuilder.append(rawDataString);
		}
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(this.transDatapath));
			
			bw.write(tableBuilder.toString());
			bw.close();
			
			return this.transDatapath;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	public Object adaptOutput(HashMap<String, Object> stateRecorder,
			Object outputModel, Object... testingData) {
		recordState(stateRecorder, outputModel);
		
		if (Collection.class.isAssignableFrom(outputModel.getClass())) {
			ArrayList tmpList = (ArrayList)outputModel;
			
			if (Collection.class.isAssignableFrom(tmpList.get(0).getClass())) {
				ArrayList<ArrayList> tableList = (ArrayList<ArrayList>)tmpList;
				
				Map<String, Object> newFieldMap = new HashMap<String, Object>();
				newFieldMap.put("PythonResult", tableList);
				
				System.out.println("Check python result: " + newFieldMap);
				
				this.expandStateDefinition(newFieldMap, stateRecorder);
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
	public void complementTransformInput(Object transInput) {
		ArrayList<ArrayList<Number>> transInputContent = (ArrayList<ArrayList<Number>>)transInput;
		
		for (int i = 0; i < transInputContent.size(); i++) {
			transInputContent.get(i).set(this.classAttr, this.dataList.get(i));
		}
	}
}
