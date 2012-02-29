package edu.columbia.cs.psl.mountaindew.runtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import edu.columbia.cs.psl.metamorphic.struct.Package;
public class ClassLister {
	InterceptorClassLoader tempLoader;
	public void go()
	{
		generateClassList();
		for(Package p : packages)
		{
			System.out.println(p);
		}
	}
	public ArrayList<Package> listPackages()
	{
		if(packages == null)
			generateClassList();
		return packages;
	}
	private void generateClassList() {
		tempLoader = new InterceptorClassLoader();
		packages = new ArrayList<Package>();
		String classpath = System.getProperty("java.class.path");
		String[] locations = classpath.split(System.getProperty("path.separator"));
		for(String cp : locations)
		{
			retrievePackagesInPath(cp,null,0);
		}
		for(Package p : packages)
		{
			prunePackage(p);
		}
	}
	
	private void prunePackage(Package p) {
		if(p.getChildren().size() == 0 && p.getClasses().size() == 0)
			p.getParent().getChildren().remove(p);
		for(Package pa : p.getChildren())
		{
			prunePackage(pa);
		}
		
	}
	private void retrievePackagesInPath(String cp,Package parentPackage,int depth) {
		File f = new File(cp);
		if(f.exists())
		{
			if(f.isDirectory())
			{
				Package curPackage = (depth == 0 ? null : getPackage(parentPackage,f.getName()));
				for(String z : f.list())
					retrievePackagesInPath(f.getAbsolutePath()+"/"+z,curPackage,depth+1);
			}
			else if(f.getName().endsWith(".class"))
			{
				Class c;
				try {
					c = tempLoader.loadClass(parentPackage.getFullName()+"."+f.getName().replace(".class", ""));
					if(!c.isInterface())
						parentPackage.getClasses().add(c);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(f.getName().endsWith(".jar"))
			{
				try {
					JarFile jf = new JarFile(f);
					Enumeration<JarEntry> entries = jf.entries();
					while(entries.hasMoreElements())
					{
						JarEntry e = entries.nextElement();
						if(e.getName().endsWith(".class"))
						{
							String[] path = e.getName().split(System.getProperty("file.separator"));
							Package p = null;
							for(String pa : path)
							{
								if(!pa.endsWith(".class"))
								p = getPackage(p, pa);
							}
							Class c;
							try {
								c = tempLoader.loadClass(e.getName().replace(".class", "").replace(System.getProperty("file.separator"), "."));
								if(!c.isInterface())
									p.getClasses().add(c);
							} catch (ClassNotFoundException ea) {
								// TODO Auto-generated catch block
								ea.printStackTrace();
							}
//							p.getClasses().add(e.getName().substring(1+e.getName().lastIndexOf(System.getProperty("file.separator"))).replace(".class", ""));
						}
					}
				} catch (IOException e) {

				}
			}
		}
	}
	private Package getPackage(Package parentPackage, String name) {
		if(parentPackage == null)
		{
			for(Package p : packages)
				if(p.getName().equals(name))
					return p;
			Package p = new Package(name, parentPackage);
			packages.add(p);
			return p;
		}
		for(Package p : parentPackage.getChildren())
		{
			if(p.getName().equals(name))
				return p;
		}
		Package p = new Package(name,parentPackage);
		parentPackage.getChildren().add(p);
		return p;

	}
	private ArrayList<Package> packages;
	public static void main(String[] args) {
		new ClassLister().go();
	}
}
