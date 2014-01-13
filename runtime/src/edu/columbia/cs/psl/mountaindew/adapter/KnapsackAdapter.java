package edu.columbia.cs.psl.mountaindew.adapter;

import knapsack.Knapsack;

public class KnapsackAdapter extends DefaultAdapter{
	
	@Override
	public Object adaptOutput(Object model, Object...testingData) {
		Class outputClass = model.getClass();
		return model;
	}

}
