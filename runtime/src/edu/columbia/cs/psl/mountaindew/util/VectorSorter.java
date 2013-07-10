package edu.columbia.cs.psl.mountaindew.util;

import java.util.Comparator;

import org.apache.mahout.math.Vector;

public class VectorSorter implements Comparator<Vector>{

	@Override
	public int compare(Vector v1, Vector v2) {
		// TODO Auto-generated method stub
		for(int i = 0; i < v1.size(); i++) {
			if (v1.get(i) > v2.get(i)) {
				return -1;
			} else if (v1.get(i) < v2.get(i)) {
				return 1;
			}
		}
		return 0;
	}

}
