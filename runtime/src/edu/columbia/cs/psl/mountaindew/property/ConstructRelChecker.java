package edu.columbia.cs.psl.mountaindew.property;

import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.mountaindew.stats.Correlationer;

public class ConstructRelChecker extends RelationAbstract{

	@Override
	protected boolean returnValuesApply(Object p1, Object returnValue1,
			Object p2, Object returnValue2) {
		if (!checkParameterType(p1, returnValue1, p2, returnValue2))
			return false;
		double[] oriInput = Correlationer.returnDoubleArray(p1);
		double[] oriOutput = Correlationer.returnDoubleArray(returnValue1);
		double[] transInput = Correlationer.returnDoubleArray(p2);
		double[] transOutput = Correlationer.returnDoubleArray(returnValue2);
		
		if (oriInput.length != transInput.length)
			return false;
		
		if (oriOutput.length != transOutput.length)
			return false;
		
		double inputRelation = Correlationer.calSingleCorrelation(oriInput, transInput);
		double outputRelation = Correlationer.calSingleCorrelation(oriOutput, transOutput);
		
		//System.out.println("Construct input output relation: " + inputRelation + " " + outputRelation);
		
		//If input has no relation but output has high relation, the method attempts to construct relation
		if (inputRelation < 0.9 && inputRelation > -0.9 && outputRelation > 0.9)
			return true;
		
		if (inputRelation < 0.9 && inputRelation > -0.9 && outputRelation < -0.9)
			return true;
		
		return false;
	}

	@Override
	public String getName() {
		return "ConstructRelationChecker";
	}

	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

}
