package edu.columbia.cs.psl.mountaindew.absprop;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.mahout.classifier.ConfusionMatrix;

import weka.classifiers.functions.SMO;
import weka.core.Instances;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;

public abstract class EqualerAbstract extends PairwiseMetamorphicProperty{
	
	@Override
	public MetamorphicInputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected double roundDouble(double numberToRound, int digit) {
		int roundMultiplier = (int)Math.pow(10, digit);
		numberToRound = numberToRound * roundMultiplier;
		numberToRound = Math.round(numberToRound);
		numberToRound = numberToRound / roundMultiplier;
		return numberToRound;
	}
	
	protected abstract boolean returnValuesApply(Object p1, Object returnValue1, Object p2, Object returnValue2);
	protected abstract boolean propertyApplies(MethodInvocation i1, MethodInvocation i2, int interestedVariable);
	//protected abstract boolean checkEquivalence(Collection c1, Collection c2);
	protected abstract boolean checkEquivalence(Object c1, Object c2);
	//protected abstract boolean checkEquivalence(Object[] objArray1, Object[] objArray2);

}
