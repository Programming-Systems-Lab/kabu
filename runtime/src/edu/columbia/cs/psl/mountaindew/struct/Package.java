package edu.columbia.cs.psl.mountaindew.struct;

import java.util.ArrayList;

public class Package {
	private String name;
	private ArrayList<Class> classes;
	private Package parent;
	private ArrayList<Package> children;

	public Package(String name, Package parent) {
		super();
		this.name = name;
		this.classes = new ArrayList<Class>();
		this.parent = parent;
		this.children = new ArrayList<Package>();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Class> getClasses() {
		return classes;
	}
	public void setClasses(ArrayList<Class> classes) {
		this.classes = classes;
	}
	public Package getParent() {
		return parent;
	}
	public void setParent(Package parent) {
		this.parent = parent;
	}
	public ArrayList<Package> getChildren() {
		return children;
	}
	public void setChildren(ArrayList<Package> children) {
		this.children = children;
	}
	public String getFullName()
	{
		if(parent != null)
			return parent.getFullName()+"."+name;
		return name;
	}
	@Override
	public String toString() {
		return name;
	}
}
