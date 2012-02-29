package org.apache.mahout.clustering;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.VectorWritable;

public class CIMapper extends Mapper<WritableComparable<?>,VectorWritable,IntWritable,ClusterWritable> {
  
  private ClusterClassifier classifier;
  
  private ClusteringPolicy policy;
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.hadoop.mapreduce.Mapper#setup(org.apache.hadoop.mapreduce.Mapper
   * .Context)
   */
  @Override
  protected void setup(Context context) throws IOException, InterruptedException {
    String priorClustersPath = context.getConfiguration().get(ClusterIterator.PRIOR_PATH_KEY);
    String policyPath = context.getConfiguration().get(ClusterIterator.POLICY_PATH_KEY);
    classifier = ClusterIterator.readClassifier(new Path(priorClustersPath));
    policy = ClusterIterator.readPolicy(new Path(policyPath));
    policy.update(classifier);
    super.setup(context);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.apache.hadoop.mapreduce.Mapper#map(java.lang.Object,
   * java.lang.Object, org.apache.hadoop.mapreduce.Mapper.Context)
   */
  @Override
  protected void map(WritableComparable<?> key, VectorWritable value, Context context) throws IOException,
      InterruptedException {
    Vector probabilities = classifier.classify(value.get());
    Vector selections = policy.select(probabilities);
    for (Iterator<Element> it = selections.iterateNonZero(); it.hasNext();) {
      Element el = it.next();
      classifier.train(el.index(), value.get(), el.get());
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.hadoop.mapreduce.Mapper#cleanup(org.apache.hadoop.mapreduce.
   * Mapper.Context)
   */
  @Override
  protected void cleanup(Context context) throws IOException, InterruptedException {
    List<Cluster> clusters = classifier.getModels();
    ClusterWritable cw = new ClusterWritable();
    for (int index = 0; index < clusters.size(); index++) {
      cw.setValue(clusters.get(index));
      context.write(new IntWritable(index), cw);
    }
    super.cleanup(context);
  }
  
}
