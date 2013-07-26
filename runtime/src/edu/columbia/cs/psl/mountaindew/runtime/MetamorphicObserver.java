package edu.columbia.cs.psl.mountaindew.runtime;

import java.util.HashSet;

import edu.columbia.cs.psl.mountaindew.absprop.MetamorphicProperty;
import edu.columbia.cs.psl.mountaindew.property.AdditiveByConstant;
import edu.columbia.cs.psl.mountaindew.property.ConstructRelChecker;
import edu.columbia.cs.psl.mountaindew.property.DirRelChecker;
import edu.columbia.cs.psl.mountaindew.property.InclusiveByMid;
import edu.columbia.cs.psl.mountaindew.property.InclusiveByMin;
import edu.columbia.cs.psl.mountaindew.property.Invertable;
import edu.columbia.cs.psl.mountaindew.property.MultiplicativeByConstant;
import edu.columbia.cs.psl.mountaindew.property.Shufflable;
import edu.columbia.cs.psl.mountaindew.property.InclusiveByMax;
import edu.columbia.cs.psl.mountaindew.property.InvRelChecker;
import edu.columbia.cs.psl.mountaindew.property.ExclusiveByMax;
import edu.columbia.cs.psl.mountaindew.property.ExclusiveByMid;
import edu.columbia.cs.psl.mountaindew.property.ExclusiveByMin;
import edu.columbia.cs.psl.mountaindew.property.Negatable;
import edu.columbia.cs.psl.mountaindew.property.SizeChecker;
import edu.columbia.cs.psl.mountaindew.property.ProjectionEqualer;
import edu.columbia.cs.psl.mountaindew.property.ContentEqualer;
import edu.columbia.cs.psl.mountaindew.property.InorderEqualer;
import edu.columbia.cs.psl.mountaindew.property.CentroidEqualer;
import edu.columbia.cs.psl.mountaindew.property.SuperWordEqualer;

public class MetamorphicObserver {
	HashSet<Interceptor> interceptors = new HashSet<Interceptor>();
	private static HashSet<Class<? extends MetamorphicProperty>> properties = new HashSet<Class<? extends MetamorphicProperty>>();
	private static MetamorphicObserver instance = new MetamorphicObserver();
	static
	{
//		properties.add(AdditiveByConstant.class);
		properties.add(MultiplicativeByConstant.class);
//		properties.add(InclusiveByMax.class);
//		properties.add(InclusiveByMin.class);
//		properties.add(InclusiveByMid.class);
//		properties.add(ExclusiveByMax.class);
//		properties.add(ExclusiveByMid.class);
//		properties.add(ExclusiveByMin.class);
//		properties.add(Invertable.class);
//		properties.add(Negatable.class);
//		properties.add(Shufflable.class);
//		properties.add(DirRelChecker.class);
//		properties.add(InvRelChecker.class);
//		properties.add(ConstructRelChecker.class);
//		properties.add(SizeChecker.class);
//		properties.add(ProjectionEqualer.class);
//		properties.add(ContentEqualer.class);
//		properties.add(SuperWordEqualer.class);
//		properties.add(InorderEqualer.class);
//		properties.add(CentroidEqualer.class);
	}
	public static MetamorphicObserver getInstance() {
//		System.out.println("Get metamorphic observer instance");
		return instance;
	}
	
	public HashSet<Class<? extends MetamorphicProperty>> registerInterceptor(Interceptor i)
	{
		interceptors.add(i);
		return properties;
	}
	public void reportResults()
	{
		for(Interceptor i : interceptors)
		{
			//i.reportPropertyResults();
			i.reportPropertyResultList();
			System.out.println("");
			i.exportMethodProfile();
		}
	}
}
