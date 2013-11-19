package edu.columbia.cs.psl.mountaindew.example;

import java.io.File;
import java.util.Arrays;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

//import weka.clusterers.CLOPE;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.Cobweb;
import weka.clusterers.EM;
import weka.clusterers.MakeDensityBasedClusterer;
/*import weka.clusterers.HierarchicalClusterer;*/
import weka.clusterers.SimpleKMeans;
//import weka.clusterers.XMeans;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class WekaClusterExample {
	
	public Instances loadData(String dataPath) {
		ArffLoader dataLoader = new ArffLoader ();
		try {
			dataLoader.setFile(new File(dataPath));
			
			Instances data = dataLoader.getDataSet();
			data.setClassIndex(data.numAttributes() - 1);
			
			Remove filter = new Remove();
			filter.setAttributeIndices("" + (data.classIndex() + 1));
			filter.setInputFormat(data);
			
			Instances clusterData = Filter.useFilter(data, filter);

			return clusterData;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	@Metamorphic
	public SimpleKMeans trainKMeans(Instances data) {
		SimpleKMeans sk = new SimpleKMeans();
		try {
			sk.setNumClusters(3);
			sk.buildClusterer(data);
			return sk;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/*@Metamorphic
	public Cobweb trainCobweb(Instances data) {
		Cobweb cw = new Cobweb();
		try {
			cw.setSeed(3);
			cw.buildClusterer(data);
			return cw;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}*/
	
	@Metamorphic
	public EM trainEM(Instances data) {
		EM em = new EM();
		try {
			em.setNumClusters(-1);
			em.buildClusterer(data);
			return em;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/*@Metamorphic
	public CLOPE trainCLOPE(Instances data) {
		CLOPE clope = new CLOPE();
		try {
			clope.buildClusterer(data);
			return clope;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}*/
	
	/*@Metamorphic
	public HierarchicalClusterer trainHC(Instances data) {
		HierarchicalClusterer hc = new HierarchicalClusterer();
		try {
			hc.setNumClusters(3);
			hc.buildClusterer(data);
			return hc;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}*/
	
	/*public XMeans trainXMeans(Instances data) {
		XMeans xmeans = new XMeans();
		try {
			xmeans.setMinNumClusters(3);
			xmeans.buildClusterer(data);
			return xmeans;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}*/
	
	@Metamorphic
	public MakeDensityBasedClusterer trainMDB(Instances data) {
		MakeDensityBasedClusterer mdb = new MakeDensityBasedClusterer();
		try {
			mdb.setNumClusters(3);
			mdb.buildClusterer(data);
			return mdb;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public void evalClusterer(Clusterer clusterer, Instances data) {
		ClusterEvaluation eval = null;
		try {
			eval = new ClusterEvaluation();
			eval.setClusterer(clusterer);
			
			eval.evaluateClusterer(data);
			
			//System.out.println("Check cluster number: " + clusterer.numberOfClusters());
			
			for (double tmp: eval.getClusterAssignments()) {
				System.out.println("" + tmp);
			}
			
			System.out.println(eval.clusterResultsToString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WekaClusterExample wce = new WekaClusterExample();
		Instances data = wce.loadData("data/iris.arff");
		
		MakeDensityBasedClusterer mdb = wce.trainMDB(data);
		if (mdb == null) {
			System.out.println("Fail to create mdb");
		}
		wce.evalClusterer(mdb, data);
		
		/*SimpleKMeans sk = wce.trainKMeans(data);
		if (sk == null) {
			System.out.println("Fail to create Simple KMeans");
		}
		wce.evalClusterer(sk, data);*/
		
		/*Cobweb cw = wce.trainCobweb(data);
		if (cw == null) {
			System.out.println("Fail to create Cobweb");
		}
		wce.evalClusterer(cw, data);*/
		
		/*EM em = wce.trainEM(data);
		if (em == null) {
			System.out.println("Fail to create EM");
		}
		wce.evalClusterer(em, data);*/
		
		/*CLOPE clope = wce.trainCLOPE(data);
		if (clope == null) {
			System.out.println("Fail to create clope");
		}
		wce.evalClusterer(clope, data);*/
		
		/*HierarchicalClusterer hc = wce.trainHC(data);
		if (hc == null) {
			System.out.println("Fail to create clope");
		}
		wce.evalClusterer(hc, data);*/
		
		/*XMeans xmeans = wce.trainXMeans(data);
		if (xmeans == null) {
			System.out.println("Fail to create xmeans");
		}
		wce.evalClusterer(xmeans, data);*/
		
	}

}
