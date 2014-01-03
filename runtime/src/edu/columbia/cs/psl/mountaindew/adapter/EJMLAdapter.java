package edu.columbia.cs.psl.mountaindew.adapter;

import java.util.List;

import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;
import org.ejml.factory.LinearSolver;

public class EJMLAdapter extends AbstractAdapter{
	
	private Class targetClass;

	@Override
	public Object unboxInput(Object input) {
		Class inputClass = input.getClass();
		
		if (inputClass == SimpleMatrix.class) {
			this.targetClass = SimpleMatrix.class;
			SimpleMatrix iMatrix = (SimpleMatrix)input;
			double[][] ret = new double[iMatrix.numRows()][iMatrix.numCols()];
			
			for (int i = 0; i < ret.length; i++) {
				for (int j = 0; j < ret[0].length; j++) {
					ret[i][j] = iMatrix.get(i, j);
				}
			}
			
			return ret;
		} else if (inputClass == DenseMatrix64F.class) {
			this.targetClass = DenseMatrix64F.class;
			DenseMatrix64F iDense = (DenseMatrix64F)input;
			double[][] ret = new double[iDense.numRows][iDense.numCols];
			
			for (int i = 0; i < ret.length; i++) {
				for (int j = 0; j < ret[0].length; j++) {
					ret[i][j] = iDense.get(i, j); 
				}
			}
		}
		return input;
	}

	@Override
	public Object adaptInput(Object transInput) {
		if (targetClass == null)
			return transInput;
		
		double[][] transData = (double[][])transInput;
		
		if (targetClass == SimpleMatrix.class) {
			SimpleMatrix ret = new SimpleMatrix(transData.length, transData[0].length);
			
			for (int i = 0; i < transData.length; i++) {
				for (int j = 0; j < transData[0].length; j++) {
					ret.set(i, j, transData[i][j]);
				}
			}
			
			return ret;
		} else if (targetClass == DenseMatrix64F.class) {
			DenseMatrix64F ret = new DenseMatrix64F(transData);
			return ret;
		}
		return null;
	}

	@Override
	public Object adaptOutput(Object outputModel, Object... testingData) {
		double[][] ret = null;
		
		if (LinearSolver.class.isAssignableFrom(outputModel.getClass())) {
			LinearSolver solver = (LinearSolver)outputModel;
			
			Object tmpObj = testingData[0];
			
			if (SimpleMatrix.class.isAssignableFrom(tmpObj.getClass())) {
				SimpleMatrix all = (SimpleMatrix)tmpObj;
				
				SimpleMatrix xdata = all.extractMatrix(0, all.numRows(), 0, all.numCols() - 1);
				SimpleMatrix ydata = all.extractMatrix(0, all.numRows(), all.numCols() - 1, all.numCols());
				
		        DenseMatrix64F x = new DenseMatrix64F(xdata.numCols(), 1);

		        solver.solve(ydata.getMatrix(),x);
		        
		        SimpleMatrix theta = new SimpleMatrix(x);
		        
		        return adaptOutput(theta);
			}
		} else if (outputModel.getClass() == SimpleMatrix.class) {
			SimpleMatrix outputMatrix = (SimpleMatrix)outputModel;
			
			ret = new double[outputMatrix.numRows()][outputMatrix.numCols()];
			for (int i = 0; i < ret.length; i++) {
				for (int j = 0; j < ret[0].length; j++) {
					ret[i][j] = outputMatrix.get(i, j);
				}
			}
		} else if (outputModel.getClass() == DenseMatrix64F.class) {
			DenseMatrix64F outputMatrix = (DenseMatrix64F)outputModel;
			
			ret = new double[outputMatrix.numRows][outputMatrix.numCols];
			for (int i = 0; i < ret.length; i++) {
				for (int j = 0; j < ret[0].length; j++) {
					ret[i][j] = outputMatrix.get(i, j);
				}
			}
		} else {
			return outputModel;
		}
		return ret;
	}

	@Override
	public List<Object> skipColumn(Object input) {
		// TODO Auto-generated method stub
		return null;
	}

}
