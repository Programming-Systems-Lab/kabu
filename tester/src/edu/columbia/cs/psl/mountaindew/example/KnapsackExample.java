package edu.columbia.cs.psl.mountaindew.example;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import knapsack.Knapsack;
import knapsack.KnapsackItem;

public class KnapsackExample {
	
	@Metamorphic
	public Knapsack buildKnapsack(int[][] inputData) {
		int capacity = inputData[0][2];
		int dataNum = inputData.length;
		
		System.out.println("Confirm capacity: " + capacity);
		
		KnapsackItem[] input = new KnapsackItem[dataNum];
		
		int count = 0;
		for (int i = 0; i < dataNum; i++) {
			KnapsackItem item = new KnapsackItem(inputData[i][0], inputData[i][1]);
			input[count++] = item;
		}
		
		Knapsack k = new Knapsack();
		k.solve(input, capacity);
		
		return k;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		KnapsackExample ex = new KnapsackExample();
		int[][] inputData = new int[][]{{12, 4, 15}, {2, 2, 15}, {1, 1, 15}, {4, 10, 15}, {1, 2, 15}};
		//int[][] inputData = new int[][]{{1, 2, 15}, {4, 10, 15}, {1, 1, 15}, {2, 2, 15}, {12, 4, 15}};
		Knapsack k = ex.buildKnapsack(inputData);
		
		int[] solution = k.getSolution();
		
		for (int i = 0; i < solution.length; i++) {
			System.out.println("Sol: " + solution[i]);
		}
		
		Integer a = new Integer(120);
		Double b = new Double(120.0);
		double neg = -1;
		System.out.println("Check equal: " + (a.equals(b)));
		System.out.println("Check equal: " + (-1 == 1 * neg));
	}

}
