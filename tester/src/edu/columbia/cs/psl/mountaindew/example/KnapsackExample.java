package edu.columbia.cs.psl.mountaindew.example;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.LogState;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import knapsack.Knapsack;
import knapsack.KnapsackItem;

@LogState
public class KnapsackExample {

	public int[] buildKnapsack(int[][]data, int capacity) {
		System.out.println("Confirm capacity: " + capacity);
		
		int dataNum = data.length;
		KnapsackItem[] input = new KnapsackItem[dataNum];
		
		int count = 0;
		for (int i = 0; i < dataNum; i++) {
			KnapsackItem item = new KnapsackItem(data[i][0], data[i][1]);
			input[count++] = item;
		}
		
		Knapsack k = new Knapsack();
		int[] sol = k.solve(input, capacity);
		return sol;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		KnapsackExample ex = new KnapsackExample();
		int[][] inputData = new int[][]{{1, 2}, {4, 10}, {1, 1}, {2, 2}, {12, 4}};
		
		int[] solution = ex.buildKnapsack(inputData, 15);
		
		for (int i = 0; i < solution.length; i++) {
			System.out.println("Sol: " + solution[i]);
		}
	}

}
