package edu.columbia.cs.psl.mountaindew.example;

import com.google.common.io.Closeables;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.classifier.AbstractVectorClassifier;
import org.apache.mahout.classifier.naivebayes.ComplementaryNaiveBayesClassifier;
import org.apache.mahout.classifier.naivebayes.NaiveBayesModel;
import org.apache.mahout.classifier.naivebayes.StandardNaiveBayesClassifier;
import org.apache.mahout.classifier.naivebayes.training.TrainNaiveBayesJob;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class NaiveBayesTest {

  private Configuration conf;
  private File inputFile;
  private File outputDir;
  private File tempDir;

  static final Text LABEL_STOLEN = new Text("/stolen/");
  static final Text LABEL_NOT_STOLEN = new Text("/not_stolen/");

  static final Vector.Element COLOR_RED = elem(0, 1);
  static final Vector.Element COLOR_YELLOW = elem(1, 1);
  static final Vector.Element TYPE_SPORTS = elem(2, 1);
  static final Vector.Element TYPE_SUV = elem(3, 1);
  static final Vector.Element ORIGIN_DOMESTIC = elem(4, 1);
  static final Vector.Element ORIGIN_IMPORTED = elem(5, 1);
  
  public static void main(String args[]) throws Exception {
	  NaiveBayesTest test = new NaiveBayesTest();
	  test.setUp();
	  test.toyData();
  }


  public void setUp() throws Exception {

    conf = new Configuration();

    //inputFile = getTestTempFile("trainingInstances.seq");
    inputFile = new File("bayes/vec/file1");
    outputDir = new File("bayes/model");
    outputDir.delete();
    tempDir = new File("bayes/tmp");

    SequenceFile.Writer writer = new SequenceFile.Writer(FileSystem.get(conf), conf,
        new Path(inputFile.getAbsolutePath()), Text.class, VectorWritable.class);

    try {
    	System.out.println("Test trainingInstance: " + trainingInstance(COLOR_RED, TYPE_SPORTS, ORIGIN_DOMESTIC));
    	
      writer.append(LABEL_STOLEN, trainingInstance(COLOR_RED, TYPE_SPORTS, ORIGIN_DOMESTIC));
      writer.append(LABEL_NOT_STOLEN, trainingInstance(COLOR_RED, TYPE_SPORTS, ORIGIN_DOMESTIC));
      writer.append(LABEL_STOLEN, trainingInstance(COLOR_RED, TYPE_SPORTS, ORIGIN_DOMESTIC));
      writer.append(LABEL_NOT_STOLEN, trainingInstance(COLOR_YELLOW, TYPE_SPORTS, ORIGIN_DOMESTIC));
      writer.append(LABEL_STOLEN, trainingInstance(COLOR_YELLOW, TYPE_SPORTS, ORIGIN_IMPORTED));
      writer.append(LABEL_NOT_STOLEN, trainingInstance(COLOR_YELLOW, TYPE_SUV, ORIGIN_IMPORTED));
      writer.append(LABEL_STOLEN, trainingInstance(COLOR_YELLOW, TYPE_SUV, ORIGIN_IMPORTED));
      writer.append(LABEL_NOT_STOLEN, trainingInstance(COLOR_YELLOW, TYPE_SUV, ORIGIN_DOMESTIC));
      writer.append(LABEL_NOT_STOLEN, trainingInstance(COLOR_RED, TYPE_SUV, ORIGIN_IMPORTED));
      writer.append(LABEL_STOLEN, trainingInstance(COLOR_RED, TYPE_SPORTS, ORIGIN_IMPORTED));
    } finally {
      Closeables.closeQuietly(writer);
    }
  }

  @Test
  public void toyData() throws Exception {
    TrainNaiveBayesJob trainNaiveBayes = new TrainNaiveBayesJob();
    trainNaiveBayes.setConf(conf);
    trainNaiveBayes.run(new String[] { "--input", inputFile.getAbsolutePath(), "--output", outputDir.getAbsolutePath(),
        "-el", "--tempDir", tempDir.getAbsolutePath() });

    NaiveBayesModel naiveBayesModel = NaiveBayesModel.materialize(new Path(outputDir.getAbsolutePath()), conf);

    AbstractVectorClassifier classifier = new StandardNaiveBayesClassifier(naiveBayesModel);

   // assertEquals(2, classifier.numCategories());

    Vector prediction = classifier.classifyFull(trainingInstance(COLOR_RED, TYPE_SUV, ORIGIN_DOMESTIC).get());
    
    System.out.println("Prediction: " + prediction);
    System.out.println("Selected category: " + prediction.maxValueIndex());
    System.out.println("Test restul: " + (prediction.get(0) < prediction.get(1)));

    // should be classified as not stolen
    //assertTrue(prediction.get(0) < prediction.get(1));
  }

  @Test
  public void toyDataComplementary() throws Exception {
    TrainNaiveBayesJob trainNaiveBayes = new TrainNaiveBayesJob();
    trainNaiveBayes.setConf(conf);
    trainNaiveBayes.run(new String[] { "--input", inputFile.getAbsolutePath(), "--output", outputDir.getAbsolutePath(),
        "-el", "--trainComplementary",
        "--tempDir", tempDir.getAbsolutePath() });

    NaiveBayesModel naiveBayesModel = NaiveBayesModel.materialize(new Path(outputDir.getAbsolutePath()), conf);

    AbstractVectorClassifier classifier = new ComplementaryNaiveBayesClassifier(naiveBayesModel);

    //assertEquals(2, classifier.numCategories());

    Vector prediction = classifier.classifyFull(trainingInstance(COLOR_RED, TYPE_SUV, ORIGIN_DOMESTIC).get());

    // should be classified as not stolen
    //assertTrue(prediction.get(0) < prediction.get(1));
  }

  static VectorWritable trainingInstance(Vector.Element... elems) {
    DenseVector trainingInstance = new DenseVector(6);
    for (Vector.Element elem : elems) {
      trainingInstance.set(elem.index(), elem.get());
    }
    return new VectorWritable(trainingInstance);
  }
  
  /**
   * convenience method to create a {@link Vector.Element}
   */
  public static Vector.Element elem(int index, double value) {
    return new ElementToCheck(index, value);
  }

  /**
   * a simple implementation of {@link Vector.Element}
   */
  static class ElementToCheck implements Vector.Element {
    private final int index;
    private double value;

    ElementToCheck(int index, double value) {
      this.index = index;
      this.value = value;
    }
    @Override
    public double get() {
      return value;
    }
    @Override
    public int index() {
      return index;
    }
    @Override
    public void set(double value) {
      this.value = value;
    }
  }


}
