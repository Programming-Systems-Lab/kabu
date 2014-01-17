package edu.columbia.cs.psl.mountaindew.absprop;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.rits.cloning.Cloner;

import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.inputProcessor.DependentProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.MetamorphicInputProcessor;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Reverse;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Shuffle;
import edu.columbia.cs.psl.metamorphic.runtime.MetamorphicInputProcessorGroup;
import edu.columbia.cs.psl.mountaindew.adapter.AbstractAdapter;
import edu.columbia.cs.psl.mountaindew.adapter.AdapterLoader;
import edu.columbia.cs.psl.mountaindew.adapter.DefaultAdapter;
import edu.columbia.cs.psl.mountaindew.runtime.MethodProfiler;
import edu.columbia.cs.psl.mountaindew.struct.PossiblyMetamorphicMethodInvocation;
import edu.columbia.cs.psl.mountaindew.struct.TransClassTuple;
import edu.columbia.cs.psl.mountaindew.util.FieldCollector;
import edu.columbia.cs.psl.mountaindew.util.MetamorphicConfigurer;

public abstract class MetamorphicProperty {
	public abstract String getName();

	public abstract MetamorphicInputProcessor getInputProcessor();
	
	public abstract MethodProfiler getMethodProfiler();

	public String getDescription() {
		return "Metamorphic property: " + getName();
	}
	
	//private HashSet<Class<? extends MetamorphicInputProcessor>> processorPrototypes = MetamorphicInputProcessorGroup.getInstance().getProcessors();
	
	//private HashSet<Class<? extends MetamorphicInputProcessor>> nonValueChangeProcessors = MetamorphicInputProcessorGroup.getInstance().getNonValueChangeProcessors();
	
	//private HashSet<Class<? extends MetamorphicInputProcessor>> processorPrototypes;
	
	private HashSet<TransClassTuple> processorPrototypes;
	
	private HashSet<Class<? extends MetamorphicInputProcessor>> nonValueChangeProcessors;
	
	private List<MetamorphicInputProcessor> processors = new ArrayList<MetamorphicInputProcessor>();
	
	private ArrayList<MethodInvocation> invocations = new ArrayList<MethodInvocation>();
	
	protected AbstractAdapter targetAdapter;
	
	protected ArrayList<MethodInvocation> getInvocations() {
		return invocations;
	}

	public abstract PropertyResult propertyHolds();
	
	public abstract List<PropertyResult> propertiesHolds();

	public void logExecution(MethodInvocation data) {
		invocations.add(data);
	}

	public static class PropertyResult {
		public String stateItem;
		public boolean holds;
		public int supportingSize;
		public double probability;
		public int antiSupportingSize;
		public HashSet<MethodInvocation[]> supportingInvocations = new HashSet<MethodInvocation[]>();
		public HashSet<MethodInvocation[]> antiSupportingInvocations = new HashSet<MethodInvocation[]>();

		public enum Result {
			HOLDS, DOES_NOT_HOLD, UNKNOWN
		};

		public Result result;
		public Object data;
		public Class<? extends MetamorphicProperty> property;
		public String combinedProperty;

		@Override
		public String toString() {
			String supportingInvocationsString = "[";
			String antiSupportingInvocationsString = "[";
			for (MethodInvocation[] ar : supportingInvocations) {
				supportingInvocationsString += "{ORI: " + ar[0] + ", TRANS: " + ar[1] + "}, ";
			}
			supportingInvocationsString = supportingInvocationsString.substring(0,
					(supportingInvocationsString.length() > 2 ? supportingInvocationsString.length() - 2 : 1))
					+ "]";
			for (MethodInvocation[] ar : antiSupportingInvocations) {
				antiSupportingInvocationsString += "{ORI: " + ar[0] + ", TRANS: " + ar[1] + "}, ";
			}
			antiSupportingInvocationsString = antiSupportingInvocationsString.substring(0,
					(antiSupportingInvocationsString.length() > 2 ? antiSupportingInvocationsString.length() - 2 : 1))
					+ "]";

			/*return "PropertyResult [holds=" + holds + ", supportingSize=" + supportingSize + ", probability=" + probability + ", antiSupportingSize="
					+ antiSupportingSize + ", supportingInvocations=" + supportingInvocationsString + ", antiSupportingInvocations="
					+ antiSupportingInvocationsString + ", result=" + result + ", property=" + property + ", combinedProperty=" + combinedProperty + "]";*/
			
			return "PropertyResult [state item=" + stateItem + ", holds=" + holds + ", supportingSize=" + supportingSize + ", probability=" + probability + ", antiSupportingSize="
			+ antiSupportingSize + ", supportingInvocations=" + supportingInvocationsString + ", antiSupportingInvocations="
			+ antiSupportingInvocationsString + ", result=" + result + ", combinedProperty=" + combinedProperty + "]";
		}

	}

	private Method method;

	protected Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	/*public void setInputProcessors (HashSet<Class<? extends MetamorphicInputProcessor>> processorPrototypes) {
		this.processorPrototypes = processorPrototypes;
	}*/
	
	public void setInputProcessors(HashSet<TransClassTuple> processorPrototypes) {
		this.processorPrototypes = processorPrototypes;
	}
	
	public void setNonValueChangeInputProcessors (HashSet<Class<? extends MetamorphicInputProcessor>> nonValuePrototypes) {
		this.nonValueChangeProcessors = nonValuePrototypes;
	}
	
	public void setTargetAdapter(Class<? extends AbstractAdapter> targetClass) {
		try {
			this.targetAdapter = (AbstractAdapter)targetClass.newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Loading target adapter fails. Use the defult adapter");
			this.targetAdapter = new DefaultAdapter();
		}
	}
	
	public AbstractAdapter getTargetAdapter() {
		return this.targetAdapter;
	}
	
	public void loadInputProcessors() {
		/*for (Class<? extends MetamorphicInputProcessor> processorClass: this.processorPrototypes) {
			try {
				MetamorphicInputProcessor inputProcessor = processorClass.newInstance();
				this.processors.add(inputProcessor);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}*/
		
		for (TransClassTuple tuple: this.processorPrototypes) {
			try {
				MetamorphicInputProcessor inputProcessor = tuple.getTransClass().newInstance();
				inputProcessor.addBoundaryDefaultParameters(tuple.getTimes());
				this.processors.add(inputProcessor);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<MetamorphicInputProcessor> getInputProcessors() {
		return this.processors;
	}
	
	/*private void loadTargetAdapter() {
		String className = this.mConfigurer.getAdapterClassName();
		try {
			this.targetAdapter = (AbstractAdapter)AdapterLoader.loadClass(className).newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Loading target adapter fails. Use default adapter");
			this.targetAdapter = new DefaultAdapter();
		} 
	}*/

	private Cloner cloner = new Cloner();

	public HashSet<PossiblyMetamorphicMethodInvocation> createChildren(MethodInvocation inv) {
		//this.loadTargetAdapter();
		
		//Check all fields
		List<Field> collector = new ArrayList<Field>();
		FieldCollector.collectFields(inv.callee.getClass(), collector);
		System.out.println("Confirm all fields for creating children: " + collector);
		
		HashSet<PossiblyMetamorphicMethodInvocation> ret = new HashSet<PossiblyMetamorphicMethodInvocation>();
		
		for (MetamorphicInputProcessor processor: this.processors) {
			boolean[] paramFlipping = new boolean[inv.params.length + collector.size()];
			HashMap<String, HashSet<String>> classMap = processor.getClassMap();
			
			ArrayList<boolean[]> combis = computeCombinations(paramFlipping);
			
			System.out.println("Check combis: " + combis.size());
			for (boolean[] tmp: combis) {
				System.out.println("Check in combis: " + tmp.length);
				for (Boolean t: tmp) {
					System.out.println(t);
				}
			}
			
			for (Object[] propertyParams: processor.getBoundaryDefaultParameters()) {
				CombiLoop: for (boolean[] pset: combis) {
					PossiblyMetamorphicMethodInvocation child = new PossiblyMetamorphicMethodInvocation();
					child.callee = cloner.deepClone(inv.callee);
					child.parent = inv;
					child.params = new Object[inv.params.length];
					child.inputFlippedParams = new boolean[pset.length];
					child.propertyParams = new Object[pset.length][];
					child.setFrontendProcessor(processor);
					System.out.println("Set children processor: " + child.getFrontend());
					//Probabaly need to clone?
					child.setAdapter(this.targetAdapter);
					child.setBackendC(this.getName());
					child.setClassMap(classMap);
					boolean atLeastOneTrue = false;
					for (int i = 0; i < pset.length; i++) {
						atLeastOneTrue = atLeastOneTrue || pset[i];
						if (pset[i]) {
							child.inputFlippedParams[i] = true;
							
							if (DependentProcessor.class.isAssignableFrom(processor.getClass())) {
								((DependentProcessor)processor).setParentResult(inv.returnValue);
							}
							
							try {
								child.propertyParams[i] = propertyParams;
								//If branch handle input, else handles field
								if (i < inv.params.length) {
									Object input = (Object)cloner.deepClone(inv.params[i]);
									Object unboxInput;
									Object transformed;
									
									if  (this.nonValueChangeProcessors.contains(processor.getClass())) {
										unboxInput = this.targetAdapter.unboxInput(input);
										transformed = processor.apply(unboxInput, propertyParams);
										child.params[i] = this.targetAdapter.adaptInput(transformed);
									} else {
										List<Object> skipList = this.targetAdapter.skipColumn(input);
										this.targetAdapter.setSkipList(skipList);
										unboxInput = this.targetAdapter.unboxInput(input);
										
										this.targetAdapter.setupComplementMap(unboxInput);
										transformed = processor.apply(unboxInput, propertyParams);

										this.targetAdapter.complementTransformInput(transformed);
										child.params[i] = this.targetAdapter.adaptInput(transformed);
									}
								} else {
									Field f = collector.get(pset.length - i - 1);
									
									//Temporarily skip static field
									if (Modifier.isStatic(f.getModifiers()))
										continue;
									
									String shouldTrans = "__meta_should_trans_" + f.getName();
									String adapterField = "__meta_gen_adapter";
									String processField = "__meta_gen_processor";
									child.callee.getClass().getField(shouldTrans).set(child.callee, true);
									child.callee.getClass().getField(adapterField).set(child.callee, child.getAdapter());
									child.callee.getClass().getField(processField).set(child.callee, child.getFrontendProcessor());
									
									System.out.println("Double check child callee: " + child.callee.getClass().getField(adapterField).get(child.callee));
								}
							} catch (Exception ex) {
								ex.printStackTrace();
								continue CombiLoop;
							}

						} else {
							if (i < inv.params.length)
								child.params[i] = cloner.deepClone(inv.params[i]);
						}
					}
					
					if(atLeastOneTrue)
					{
						ret.add(child);
					}
				}
			}
		}

		return ret;
	}
	
	private ArrayList<boolean[]> computeCombinations(boolean[] restOfVals) {
		return computeCombinations(new boolean[0], restOfVals);
	}
	
	private ArrayList<boolean[]> computeCombinations(boolean[] prefix, boolean[] restOfVals) {
		if(restOfVals.length == 0 )
		{
			ArrayList<boolean[]> ret = new ArrayList<boolean[]>();
			ret.add(prefix);
			return ret;
		}
		else {
			ArrayList<boolean[]> newList = new ArrayList<boolean[]>();
			newList.addAll(prependToEach(appendElement(prefix,false), computeCombinations(removeFirstElement(restOfVals))));
			newList.addAll(prependToEach(appendElement(prefix,true), computeCombinations(removeFirstElement(restOfVals))));
			return newList;
		}

	}
	
	private boolean[] appendElement(boolean[] ar, boolean v)
	{
		boolean[] ret = new boolean[ar.length + 1];
		for(int i =0;i<ar.length;i++)
		{
			ret[i]=ar[i];
		}
		ret[ar.length] = v;
		return ret;
	}
	
	private boolean[] removeFirstElement(boolean[] ar)
	{
		boolean[] ret = new boolean[ar.length -1];
		for(int i =1;i<ar.length;i++)
		{
			ret[i-1]=ar[i];
		}
		return ret;
	}
	
	private ArrayList<boolean[]> prependToEach(boolean[] prefix, ArrayList<boolean[]> vals) {
		ArrayList<boolean[]> ret = new ArrayList<boolean[]>();
		for (boolean[] o : vals) {
			boolean[] r = new boolean[o.length + prefix.length];
			for(int i = 0; i<prefix.length;i++)
			{
				r[i] = prefix[i];
			}
			for(int i = 0; i<o.length;i++)
			{
				r[i+prefix.length]=o[i];
			}
			ret.add(r);
		}
		return ret;
	}
}
