package main;
import java.io.*;
import java.util.HashMap;
import org.json.JSONException;

public class GSDMM
{
	protected int K;
	protected double alpha;
	protected double beta;
	protected int iterNum;
	protected String dataset;
	
	protected HashMap<String, Integer> wordToIdMap;
	protected int V;
	protected DocumentSet documentSet;
	protected String dataDir = "data/"; 
	protected String outputPath = "result/";
	
	public GSDMM(int K, double alpha, double beta, int iterNum, String dataset)
	{
		this.K = K;
		this.alpha = alpha;
		this.beta = beta;
		this.iterNum = iterNum;
		this.dataset = dataset;
		this.wordToIdMap = new HashMap<String, Integer>();
	}
	public static void main(String args[]) throws Exception
	{
		int K = 200;
		double alpha = 0.1;
		double beta = 0.1;
		int iterNum = 10;
		String dataset = "Tweet";
		GSDMM gsdmm = new GSDMM(K, alpha, beta, iterNum, dataset);
		
		long startTime = System.currentTimeMillis();				
		gsdmm.getDocuments();
		long endTime = System.currentTimeMillis();
		System.out.println("getDocuments Time Used:" + (endTime-startTime)/1000.0 + "s");
		
		gsdmm.runGSDMM();
	}
	
	public void getDocuments() throws JSONException
	{
		documentSet = new DocumentSet(dataDir + dataset, wordToIdMap);
		V = wordToIdMap.size();
	}
	
	public void runGSDMM() throws IOException
	{
		String ParametersStr = "K"+K+"iterNum"+ iterNum +"alpha" + String.format("%.3f", alpha)
								+ "beta" + String.format("%.3f", beta);
		Model model = new Model(K, V, iterNum,alpha, beta, dataset,  ParametersStr);
		
		long startTime = System.currentTimeMillis();		
		model.intialize(documentSet);
		model.gibbsSampling(documentSet, iterNum);
		long endTime = System.currentTimeMillis();
		System.out.println("gibbsSampling Time Used:" + (endTime-startTime)/1000.0 + "s");
		
		startTime = System.currentTimeMillis();				
		model.output(documentSet, outputPath);
		endTime = System.currentTimeMillis();
		System.out.println("output Time Used:" + (endTime-startTime)/1000.0 + "s");
		
		System.out.println("Final Done");
	}
}
