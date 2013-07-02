package edu.columbia.cs.psl.mountaindew.example.mutant;

public class MutantStruct implements Comparable<MutantStruct>{
	private String fileName;
	
	private String methodName;
	
	private String backend;
	
	private String frontend;
	
	private boolean hold = true;
	
	public MutantStruct (String fileName) {
		this.fileName = fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
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
	
	@Override
	public boolean equals(Object mutant) {
		if (!(mutant instanceof MutantStruct))
			return false;
		
		if (mutant == this)
			return true;
		
		MutantStruct tmpStruct = (MutantStruct) mutant;
		
		if (!tmpStruct.getFileName().equals(this.getFileName()))
			return false;
		
		if (!tmpStruct.getMethodName().equals(this.getMethodName()))
			return false;
		
		if (!tmpStruct.getBackend().equals(this.getBackend()))
			return false;
		
		if (!tmpStruct.getFrontend().equals(this.getFrontend()))
			return false;
		
		return true;
	}
	
	public String toString() {
		return this.fileName + "," + this.methodName + "," + this.frontend + "," + this.backend + "," + String.valueOf(this.hold);
	}
	
	@Override
	public int compareTo(MutantStruct comp) {
		String myString = this.methodName + this.frontend + this.backend;
		String compString = comp.getMethodName() + comp.getFrontend() + comp.getBackend();
		
		return myString.compareTo(compString);
	}
}
