package edu.columbia.cs.psl.mountaindew.runtime;

import java.util.HashSet;

import edu.columbia.cs.psl.mountaindew.property.AdditiveByConstant;
import edu.columbia.cs.psl.mountaindew.property.InclusiveByMin;
import edu.columbia.cs.psl.mountaindew.property.MetamorphicProperty;
import edu.columbia.cs.psl.mountaindew.property.MultiplicativeByConstant;
import edu.columbia.cs.psl.mountaindew.property.Shufflable;
import edu.columbia.cs.psl.mountaindew.property.InclusiveByMax;
import edu.columbia.cs.psl.mountaindew.property.Exclusivable;

public class MetamorphicObserver {
	HashSet<Interceptor> interceptors = new HashSet<Interceptor>();
	private static HashSet<Class<? extends MetamorphicProperty>> properties = new HashSet<Class<? extends MetamorphicProperty>>();
	private static MetamorphicObserver instance = new MetamorphicObserver();
	static
	{
//		properties.add(AdditiveByConstant.class);
//		properties.add(MultiplicativeByConstant.class);
		properties.add(InclusiveByMax.class);
		properties.add(InclusiveByMin.class);
//		properties.add(Shufflable.class);
		properties.add(Exclusivable.class);
	}
	public static MetamorphicObserver getInstance() {
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
			i.reportPropertyResults();
		}
	}
}
