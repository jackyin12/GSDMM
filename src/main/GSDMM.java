package main;
import java.util.HashMap;

public class GSDMM
{
	int K;
	double alpha;
	double beta;
	int iterNum;
	String dataset;
	
	HashMap<String, Integer> wordToIdMap;
	int V;
	DocumentSet documentSet;
	String dataDir = "data/"; 
	String outputPath = "result/";
	
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
		int K = 50;
		double alpha = 0.1;
		double beta = 0.1;
		int iterNum = 10;
		String dataset = "20ng";
		GSDMM gsdmm = new GSDMM(K, alpha, beta, iterNum, dataset);
		
		long startTime = System.currentTimeMillis();				
		gsdmm.getDocuments();
		long endTime = System.currentTimeMillis();
		System.out.println("getDocuments Time Used:" + (endTime-startTime)/1000.0 + "s");
		
		startTime = System.currentTimeMillis();	
		gsdmm.runGSDMM();
		endTime = System.currentTimeMillis();
		System.out.println("gibbsSampling Time Used:" + (endTime-startTime)/1000.0 + "s");
	}
	
	public void getDocuments() throws Exception
	{
		documentSet = new DocumentSet(dataDir + dataset, wordToIdMap);
		V = wordToIdMap.size();
	}
	
	public void runGSDMM() throws Exception
	{
		String ParametersStr = "K"+K+"iterNum"+ iterNum +"alpha" + String.format("%.3f", alpha)
								+ "beta" + String.format("%.3f", beta);
		Model model = new Model(K, V, iterNum,alpha, beta, dataset,  ParametersStr);
		model.intialize(documentSet);
		model.gibbsSampling(documentSet);
		model.output(documentSet, outputPath);
	}
}
