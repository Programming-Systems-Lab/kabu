package edu.columbia.cs.psl.mountaindew.util;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.runtime.MetamorphicInputProcessorGroup;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.LogState;
import edu.columbia.cs.psl.mountaindew.adapter.AbstractAdapter;

public class TransformPlugin {
	
	public static HashSet<Class<? extends MetamorphicInputProcessor>> nonValueChangeProcessors = 
			MetamorphicInputProcessorGroup.getInstance().getNonValueChangeProcessors();
	
	public static<T> T __meta_transform_basic(T obj, AbstractAdapter adapter, MetamorphicInputProcessor processor, Object[] params) {
		Object unboxInput;
		Object transformed;
		
		System.out.println("Check obj in transforming plugin: " + obj.getClass());
		System.out.println("Check processor in transforming plugin: " + processor.getClass());
		
		try {
			if  (nonValueChangeProcessors.contains(processor.getClass())) {
				unboxInput = adapter.unboxInput(obj);
				transformed = processor.apply(unboxInput, params);
				return (T)adapter.adaptInput(obj);
			} else {
				List<Object> skipList = adapter.skipColumn(obj);
				adapter.setSkipList(skipList);
				unboxInput = adapter.unboxInput(obj);
				
				adapter.setupComplementMap(unboxInput);
				transformed = processor.apply(unboxInput, params);

				adapter.complementTransformInput(transformed);
				return (T)adapter.adaptInput(transformed);
			}
		} catch(Exception ex) {
			throw new IllegalArgumentException("Transformation fails for: " + obj + " " + ex);
		}
	}
	
	public static void setAdapterTransformer(Object callee, AbstractAdapter adapter, MetamorphicInputProcessor processor) {
		if (callee == null)
			return ;
		
		if (callee.getClass().getAnnotation(LogState.class) != null) {
			try {
				System.out.println("Check obj: in transforming plugin: " + callee.getClass());				
				callee.getClass().getField("__meta_gen_adapter").set(callee, adapter);
				callee.getClass().getField("__meta_gen_processor").set(callee, processor);	
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static void setupForStaticFields(MethodInvocation inv) {
		String methodThreadName = MetaSerializer.describeMethod(inv.method) + inv.thread.getId();
		try {
			Map sMethodBoolMap = (Map)inv.callee.getClass().getField("__meta_gen_static_bool_map").get(inv.callee);
			Map<String, Boolean> boolMap = inv.getStaticBoolMap();
			sMethodBoolMap.put(methodThreadName, boolMap);
			
			Map sMethodAMap = (Map)inv.callee.getClass().getField("__meta_gen_static_a_map").get(inv.callee);
			sMethodAMap.put(methodThreadName, inv.getAdapter());
		
			Map sMethodPMap = (Map)inv.callee.getClass().getField("__meta_gen_static_p_map").get(inv.callee);
			sMethodPMap.put(methodThreadName, inv.getFrontendProcessor());
			
			Map sMethodParamMap = (Map)inv.callee.getClass().getField("__meta_gen_static_param_map").get(inv.callee);
			sMethodParamMap.put(methodThreadName, inv.getParams());
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
}
