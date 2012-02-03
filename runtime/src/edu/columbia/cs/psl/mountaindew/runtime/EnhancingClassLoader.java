package edu.columbia.cs.psl.mountaindew.runtime;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.WeakHashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import edu.columbia.cs.psl.mountaindew.runtime.annotation.MetamorphicInspected;



public class EnhancingClassLoader extends ClassLoader {
	WeakHashMap<String, Class> loadedClasses = new WeakHashMap<String, Class>();

	static final ClassLoader BASE_CLASS_LOADER = EnhancingClassLoader.class
			.getClassLoader();

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		// System.out.println("Requesting class" + name);

		if (!loadedClasses.containsKey(name)) {
			if (!name.startsWith("net.sf.cglib") && !name.startsWith("java")) {
				// name = name + "Enhanced";
				loadedClasses.put(name, super.loadClass(name, resolve));
				loadedClasses.put(name,
						createProxy(super.loadClass(name, resolve)));
			} else
				loadedClasses.put(name, super.loadClass(name, resolve));
		}
		Class c = loadedClasses.get(name);
		// System.out.println(loadedClasses);
		// System.out.println("Returning " + c.getName() + " For " + name);
		// createProxy(c);
		return c;
		// return createProxy(c);
	}

	private Class createProxy(Class c) {
		boolean shouldRewrite = false;
		for(Method m : c.getMethods())
		{
			for(Annotation a : m.getAnnotations())
			{
				if(a.annotationType().equals(MetamorphicInspected.class))
					shouldRewrite = true;
			}
		}
		if(shouldRewrite)
		{
			try {
				ClassReader cr = new ClassReader(c.getName());
				  ClassWriter cw = new ClassWriter(cr,
			     ClassWriter.COMPUTE_MAXS |
			ClassWriter.COMPUTE_FRAMES);
				  InterceptingAdaptor cv = new InterceptingAdaptor(cw);
				  cv.setClassName(c.getName());
				  cr.accept(cv, ClassReader.EXPAND_FRAMES);
				  
				Class r = defineClass(c.getName(), cw.toByteArray(), 0, cw.toByteArray().length);
				return r;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return c;
		
	}
}
