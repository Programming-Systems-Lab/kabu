package edu.columbia.cs.psl.mountaindew.runtime;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.objectweb.asm.Type;

public abstract class AbstractInterceptor {
	private Object interceptedObject;
	
	public AbstractInterceptor(Object intercepted)
	{
		this.interceptedObject = intercepted;
	}
	
	public Object getInterceptedObject() {
		return interceptedObject;
	}
	public abstract int onEnter(Method method, Object[] params);
	
	public void onExit(Object val, int op)
	{
		
	}

	
	public abstract void onExit(Object val, int op,int id);
	
	public final int __onEnter(String methodName, String[] types, Object[] params)
	{
		return onEnter(getCurMethod(methodName,types,interceptedObject.getClass()), params);
	}

	private Method getCurMethod(String methodName,String[] types, Class theClass)
	{
		try {
			for(Method m : theClass.getDeclaredMethods())
			{
				boolean ok = true;
				if(m.getName().equals(methodName))
				{
					Class[] mArgs = m.getParameterTypes();
					if(mArgs.length != types.length)
						break;
					for(int i = 0;i<mArgs.length;i++)
						if(!mArgs[i].getName().equals(types[i]))
							ok = false;
					if(ok)
						return m;
				}
			}
			if(theClass.getSuperclass() != null)
				return getCurMethod(methodName, types, theClass.getSuperclass());
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
}
