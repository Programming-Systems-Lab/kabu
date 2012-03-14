package edu.columbia.cs.psl.mountaindew.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.columbia.cs.psl.metamorphic.runtime.AbstractInterceptor;
import edu.columbia.cs.psl.metamorphic.struct.MethodInvocation;
import edu.columbia.cs.psl.mountaindew.property.MetamorphicProperty;
import edu.columbia.cs.psl.mountaindew.property.MetamorphicProperty.PropertyResult;


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
	
	public int onEnter(Object callee, Method method, Object[] params)
	{
		if(isChild(callee))
			return -1;
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
		inv.params = params;
		inv.method = method;
		inv.callee = getInterceptedObject();
		invocations.put(retId, inv);
		return retId;
	}
	
	public void onExit(Object val, int op, int id)
	{
		if(id < 0)
			return;
		MethodInvocation inv = invocations.remove(id);
		inv.returnValue = val;
		for(MetamorphicProperty p : properties.get(inv.method))
		{
			p.logExecution(inv);
			try {
				MethodInvocation inv2 = new MethodInvocation();
				inv2.callee=inv.callee;
				setAsChild(inv2.callee);
				inv2.params=p.getInputProcessor().applyToVariables(inv.params);
				inv2.returnValue = inv.method.invoke(inv.callee,inv2.params);
				setChild(inv2.callee,false);
				p.logExecution(inv2);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

