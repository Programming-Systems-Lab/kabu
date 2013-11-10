package edu.columbia.cs.psl.mountaindew.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

public class EJMLExample {
	
	public static SimpleMatrix loadData(String filePath) {
		try {
			ArrayList<double[]> data = new ArrayList<double[]>();
			File file = new File("data/matrixtest.csv");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			
			String[] dataString;
			while((line = br.readLine()) != null) {
				dataString = line.split(",");
				
				double[] dTmp = new double[dataString.length];
				for (int i = 0; i < dTmp.length; i++) {
					dTmp[i] = Double.valueOf(dataString[i]);
				}
				data.add(dTmp);
			}
			
			//Convert ArrayList to array
			double[][] finalData = new double[data.size()][data.get(0).length];
			for (int i = 0; i < finalData.length; i++) {
				finalData[i] = data.get(i);
			}
			
			SimpleMatrix dataMatrix = new SimpleMatrix(finalData);
			
			return dataMatrix;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static void main(String args[]) {
		DenseMatrix64F a = new DenseMatrix64F(3, 3);
		DenseMatrix64F x = new DenseMatrix64F(3, 1);
		DenseMatrix64F b = new DenseMatrix64F(3, 1);
		
		for (int i = 0; i < 3; i++) {
			for (int j = 3; j < 3; j++) {
				a.set(i, j, 0);
			}
		} 
		
		a.set(0, 0, 1);
		a.set(1, 1, 1);
		a.set(2, 2, 1);
		
		for (int i = 0; i < 3; i++) {
			b.set(i, 0, i+1);
		}
		
		System.out.println("Check a: " + a);
		System.out.println("Check b: " + b);
		
		try {
			if (!CommonOps.solve(a, b, x)) {
				System.err.println("Cannot solve");
			} else {
				System.out.println("Solution: " + x);
			}
			
			//SimpleMatrix sm = new SimpleMatrix(a);
			//sm.saveToFileCSV("data/simple.csv");
			
			//SimpleMatrix sm2 = SimpleMatrix.loadCSV("data/matrixtest.csv");
			//System.out.println("Check sm2: " + sm2);
			
			SimpleMatrix dataMatrix = loadData("data/matrixtest.csv");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	} 
}
