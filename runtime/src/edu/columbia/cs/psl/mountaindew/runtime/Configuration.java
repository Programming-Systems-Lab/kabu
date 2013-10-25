package edu.columbia.cs.psl.mountaindew.runtime;

import java.lang.annotation.Annotation;

import edu.columbia.cs.psl.invivo.runtime.AbstractConfiguration;
import edu.columbia.cs.psl.invivo.runtime.AbstractInterceptor;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.LogState;

public class Configuration extends AbstractConfiguration{

	@Override
	public Class<? extends AbstractInterceptor> getInterceptorClass() {
		return Interceptor.class;
	}

	@Override
	public Class<? extends Annotation> getAnnotationClass() {
		return Metamorphic.class;
	}
	
	@Override
	public Class<? extends Annotation> getLogStateAnnotationClass() {
		return LogState.class;
	}
}
