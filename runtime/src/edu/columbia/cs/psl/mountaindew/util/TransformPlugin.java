package edu.columbia.cs.psl.mountaindew.util;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
			System.out.println("Warning: " + ex.getMessage());
			return obj;
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
}
