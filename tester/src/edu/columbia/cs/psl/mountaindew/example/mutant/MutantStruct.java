package edu.columbia.cs.psl.mountaindew.example.mutant;

public class MutantStruct implements Comparable<MutantStruct>{
	private String methodName;
	
	private String backend;
	
	private String frontend;
	
	private boolean hold;
	
	public MutantStruct () {
		
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getBackend() {
		return backend;
	}

	public void setBackend(String backend) {
		this.backend = backend;
	}

	public String getFrontend() {
		return frontend;
	}

	public void setFrontend(String frontend) {
		this.frontend = frontend;
	}

	public boolean isHold() {
		return hold;
	}

	public void setHold(boolean hold) {
		this.hold = hold;
	}
	
	public boolean equals(Object mutant) {
		if (!(mutant instanceof MutantStruct))
			return false;
		
		if (mutant == this)
			return true;
		
		MutantStruct tmpStruct = (MutantStruct) mutant;
		
		if (tmpStruct.getMethodName() != this.getMethodName())
			return false;
		
		if (tmpStruct.getBackend() != this.getBackend())
			return false;
		
		if (tmpStruct.getFrontend() != this.getFrontend())
			return false;
		
		if (!String.valueOf(tmpStruct.isHold()).equals(String.valueOf(this.isHold())))
			return false;
		
		return true;
	}
	
	public String toString() {
		return this.methodName + "," + this.frontend + "," + this.backend + "," + String.valueOf(this.hold);
	}
	
	@Override
	public int compareTo(MutantStruct comp) {
		String myString = this.backend + this.frontend;
		String compString = comp.getBackend() + comp.getFrontend();
		
		return myString.compareTo(compString);
	}
}
