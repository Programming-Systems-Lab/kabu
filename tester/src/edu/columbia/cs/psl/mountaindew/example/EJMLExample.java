package edu.columbia.cs.psl.mountaindew.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.ejml.alg.dense.linsol.AdjustableLinearSolver;
import org.ejml.alg.dense.linsol.LinearSolverSafe;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolver;
import org.ejml.factory.LinearSolverFactory;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

public class EJMLExample {
	
	public static ArrayList<double[]> loadData(String filePath) {
		try {
			ArrayList<Double> xdata = new ArrayList<Double>();
			ArrayList<Double> ydata = new ArrayList<Double>();
			File file = new File("data/matrixtest.csv");
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String line;
			String[] dataString;
			while((line = br.readLine()) != null) {
				dataString = line.split(",");
				xdata.add(Double.valueOf(dataString[0]));
				ydata.add(Double.valueOf(dataString[1]));
			}
			
			//Convert ArrayList to array
			double[] xarray = new double[xdata.size()];
			double[] yarray = new double[ydata.size()];
			
			for (int i = 0; i < xdata.size(); i++) {
				xarray[i] = xdata.get(i);
				yarray[i] = ydata.get(i);
			}
			
			ArrayList<double[]> allData = new ArrayList<double[]>();
			allData.add(xarray);
			allData.add(yarray);
			return allData;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public SimpleMatrix matrixizeData(double[] oriData, int k) {
		double[][] matrix = new double[oriData.length][2*k+1];
		for (int i = 0; i < oriData.length; i++) {
			matrix[i][2 * k] = 1; 
			for (int j = 0; j < 2*k+1; j++) {
				if (j < k) {
					matrix[i][j] = Math.cos(oriData[i] * (k - j));
				} else if (j >= k && j < 2*k) {
					matrix[i][j] = Math.sin(oriData[i] * (2*k - j));
				}
			}
		}
		
		SimpleMatrix ret = new SimpleMatrix(matrix);
		System.out.println("Check ret: " + ret);
		return ret;
	}
	
	public SimpleMatrix solve(SimpleMatrix xdata, SimpleMatrix ydata) {
		/*AdjustableLinearSolver solver = LinearSolverFactory.adjustable();
		if (solver.setA(xdata))
			throw new RuntimeException("Solver failed to load");*/
		SimpleMatrix theta = xdata.solve(ydata);
		return theta;
		
	}
	
	public LinearSolver getSolver(int rows, int columns) {
		LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.general(rows, columns);
		solver = new LinearSolverSafe<DenseMatrix64F>(solver);
		return solver;
	}
	
	@Metamorphic
	public SimpleMatrix solveAll(SimpleMatrix all) {
		SimpleMatrix xdata = all.extractMatrix(0, all.numRows(), 0, all.numCols() - 1);
		SimpleMatrix ydata = all.extractMatrix(0, all.numRows(), all.numCols() - 1, all.numCols());
		
		System.out.println("Check xdata: " + xdata);
		System.out.println("Check ydata: " + ydata);
		
		LinearSolver<DenseMatrix64F> solver = this.getSolver(xdata.numRows(), xdata.numCols());        
        System.out.println("Check runtime solver: "+ solver.getClass().getName());

        if( !solver.setA(xdata.getMatrix()) )
            throw new RuntimeException("Matrix cannot be solved");
        
        DenseMatrix64F x = new DenseMatrix64F(xdata.numCols(), 1);

        solver.solve(ydata.getMatrix(),x);	
		//SimpleMatrix theta = xdata.solve(ydata);
        SimpleMatrix theta = new SimpleMatrix(x);
		
		return theta;
	}
	
	public SimpleMatrix applyModelOnTestdata(SimpleMatrix testData, SimpleMatrix model) {
		SimpleMatrix hyp = testData.mult(model);
		
		return hyp;
	}
	
	public double calError(SimpleMatrix testY, SimpleMatrix hyp) {
		SimpleMatrix diff = testY.minus(hyp);
		SimpleMatrix dSquare = diff.elementMult(diff);
		
		double squareSum = dSquare.elementSum();
		//System.out.println("Check squareSum: " + squareSum);
		double error = squareSum/(2*testY.numRows());
		
		return error;
	}
	
	public SimpleMatrix generateMatrix(double[] data) {
		SimpleMatrix ret = new SimpleMatrix(data.length, 1);
		
		for (int i = 0; i < data.length; i++) {
			ret.set(i, 0, data[i]);
		}
		
		return ret;
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
			
			ArrayList<double[]> allData = loadData("data/matrixtest.csv");
			double[] xdata = allData.get(0);
			double[] ydata = allData.get(1);
			
			double[] xtrain = Arrays.copyOfRange(xdata, 0, 100);
			double[] xtest = Arrays.copyOfRange(xdata, 100, 200);
			double[] ytrain = Arrays.copyOfRange(ydata, 0, 100);
			double[] ytest = Arrays.copyOfRange(ydata, 100, 200);
			
			
			EJMLExample ex = new EJMLExample();
			SimpleMatrix xtMatrix = ex.matrixizeData(xtrain, 3);
			SimpleMatrix ytMatrix = ex.generateMatrix(ytrain);
			
			SimpleMatrix xteMatrix = ex.matrixizeData(xtest, 3);
			SimpleMatrix yteMatrix = ex.generateMatrix(ytest);
			
			System.out.println("Check x train: " + xtMatrix);
			System.out.println("Check y train: " + ytMatrix);
			System.out.println("Check x test: " + xteMatrix);
			System.out.println("Check y test: " + yteMatrix);
			
			SimpleMatrix allMatrix = xtMatrix.combine(0, SimpleMatrix.END, ytMatrix);
			System.out.println("Check all matrix: " + allMatrix);
			
			ex.solveAll(allMatrix);
			
			SimpleMatrix theta = ex.solveAll(allMatrix);
			System.out.println("Check solve all: " + theta);
			
			SimpleMatrix hypOnTrain = ex.applyModelOnTestdata(xtMatrix, theta);
			System.out.println("Check hyp on train: " + hypOnTrain);
			
			System.out.println("Train error: " + ex.calError(ytMatrix, hypOnTrain));
			
			SimpleMatrix hypOnTest = ex.applyModelOnTestdata(xteMatrix, theta);
			System.out.println("Check hyp on test: " + hypOnTest);
			
			System.out.println("Test error: " + ex.calError(yteMatrix, hypOnTest));
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	} 
}
