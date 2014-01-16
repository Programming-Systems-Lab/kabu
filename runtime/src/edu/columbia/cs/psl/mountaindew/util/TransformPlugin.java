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
		
		System.out.println("Check processor in transforming plugin: " + processor.getClass());
		
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
	}
	
	public static void recursiveSetAdapterProcessor(Object callee, AbstractAdapter adapter, MetamorphicInputProcessor processor) {
		if (callee.getClass().getAnnotation(LogState.class) != null) {
			try {
				callee.getClass().getField("__meta_gen_adapter").set(callee, adapter);
				callee.getClass().getField("__meta_gen_processor").set(callee, processor);
				
				for (Field f: callee.getClass().getFields()) {
					if (f.getName().equals("__meta_obj_map")) {
						System.out.println("Recursive set the obj map");
						
						f.setAccessible(true);
						Map objMap = (Map)f.get(callee);
						System.out.println("Check obj map: " + objMap);
						
						for (Object key: objMap.keySet()) {
							recursiveSetAdapterProcessor(objMap.get(key), adapter, processor);
						}
					}
					
					recursiveSetAdapterProcessor(f, adapter, processor);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
