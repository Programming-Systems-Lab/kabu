package edu.columbia.cs.psl.mountaindew.runtime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.columbia.cs.psl.metamorphic.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.struct.Variable;
import edu.columbia.cs.psl.mountaindew.property.MetamorphicProperty;
import edu.columbia.cs.psl.mountaindew.property.MetamorphicProperty.PropertyResult;
import edu.columbia.cs.psl.mountaindew.runtime.visitor.TaintClassVisitor;


/**
 * Each intercepted object will have its _own_ Interceptor instance.
 * That instance will stick around for the lifetime of the intercepted object.
 * 
 * NB if you want to keep a list of these Interceptors somewhere statically,
 * you probably want to use a WeakHashMap so as to not create memory leaks
 * 
 * @author jon
 *
 */
public class Interceptor extends AbstractInterceptor {
	private HashMap<Method, HashSet<MetamorphicProperty>> properties = new HashMap<Method, HashSet<MetamorphicProperty>>();
	private HashSet<Class<? extends MetamorphicProperty>> propertyPrototypes;
	private HashMap<Integer, MethodInvocation> invocations = new HashMap<Integer, MethodInvocation>();
	private Integer invocationId = 0;
	
	public Interceptor(Object intercepted) {
		super(intercepted);
		propertyPrototypes = MetamorphicObserver.getInstance().registerInterceptor(this);
	}
	
	public int onEnter(Method method, Object[] params)
	{
		int retId = 0;
		synchronized(invocationId)
		{
			invocationId++;
			retId = invocationId;
			if(!properties.containsKey(method))
			{
				properties.put(method, new HashSet<MetamorphicProperty>());
				for(Class<? extends MetamorphicProperty> c : propertyPrototypes)
				{
					try {
						MetamorphicProperty p = c.newInstance();
						p.setMethod(method);
						properties.get(method).add(p);
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}
		MethodInvocation inv = new MethodInvocation();
		inv.params = new Variable[params.length];
		for(int i=0;i<params.length;i++)
		{
			Variable v = new Variable();
			v.position = i;
			v.value = params[i];
			inv.params[i]=v;
			i++;
		}
		inv.method = method;
		invocations.put(retId, inv);
		return retId;
	}
	
	public void onExit(Object val, int op, int id)
	{
		MethodInvocation inv = invocations.remove(id);
		inv.returnValue = val;
		for(MetamorphicProperty p : properties.get(inv.method))
		{
			p.logExecution(inv);
		}
//		System.out.println("On exit: <" + val+"> " + op);
	}
	
	public void reportPropertyResults()
	{
		for(Method m : properties.keySet())
		{
			System.out.println(m);
			for(MetamorphicProperty p : properties.get(m))
			{
				PropertyResult r = p.propertyHolds();
				System.out.println(r);
			}

		}
	}
} 

