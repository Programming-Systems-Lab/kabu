package edu.columbia.cs.psl.mountaindew.runtime;

import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("edu.columbia.cs.psl.mountaindew.runtime.annotation.MetamorphicInspected")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class MetamorphicAnnotationProcessor extends AbstractProcessor implements Processor {
	private String className;


	@Override
	public boolean process(Set<? extends TypeElement> elements,
			RoundEnvironment env) {
		Messager messager = processingEnv.getMessager();

		   //Create a hash table to hold the option switch to option bean mapping
		   HashMap<String, String> values = new HashMap<String, String>();

		   //Loop through the annotations that we are going to process
		   //In this case there should only be one: Option
		   for (TypeElement te: elements) {

		      //Get the members that are annotated with Option
		      for (Element e: env.getElementsAnnotatedWith(te))
		         //Process the members. processAnnotation is our own method
//		         processAnnotation(e, values, messager);
		    	  className = e.getEnclosingElement().toString();
		      try {
				generateOptionProcessor(processingEnv.getFiler());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		   }

		   return (true);
	}
	private void generateOptionProcessor(Filer filer) throws Exception {
		       
		   String generatedClassName = className + "Metamorphic";
		   JavaFileObject jfo = filer.createSourceFile(generatedClassName);
		      Writer writer = jfo.openWriter();
		   writer.write("/* Generated on " + new Date() + " */\n");
		       
		   writer.write("public class " + generatedClassName + " {\n");
		       
		   writer.write("\tpublic static " + className + " process(String[] args) {\n");
		       
		   writer.write("\t\t" + className + " options = new " + className + "();\n");
		   writer.write("\t\tint idx = 0;\n");
		       
		   writer.write("\t\t\tSystem.err.println(\"Unknown option: \" + args[idx++]);\n");
		       
		   writer.write("\t\t}\n");
		       
		   writer.write("\t\treturn (options);\n");
		   writer.write("\t}\n");
		       
		   writer.write("}");
		       
		   writer.flush();
		   writer.close();
		}        
}
