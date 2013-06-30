package edu.columbia.cs.psl.mountaindew.example.mutant;

public class PropertyRank implements Comparable<PropertyRank>{

	private String property;
	
	private int number;
	
	public PropertyRank(String property, int number) {
		this.property = property;
		this.number = number;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public int compareTo(PropertyRank comp) {
		//Descending
		return (new Integer(comp.getNumber())).compareTo(new Integer(this.number));
	}
}
