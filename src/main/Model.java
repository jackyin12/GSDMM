package main;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import Common.FileUtil;

public class Model{
	protected int K; 
	protected double alpha;
	protected double beta;
	protected String dataset;
	protected String ParametersStr;
	protected int V; 
	protected int D; 
	protected int iterNum; 
	protected double alpha0;
	protected double beta0;
	protected double[][] phi_zv;
	protected int[] z; 
	protected int[] m_z; 
	protected int[][] n_zv;
	protected int[] n_z; 

	public Model(int K, int V, int iterNum, double alpha, double beta, 
			String dataset, String ParametersStr)
	{
		this.dataset = dataset;
		this.ParametersStr = ParametersStr;
		this.alpha = alpha;
		this.beta = beta;
		this.K = K;
		this.V = V;
		this.iterNum = iterNum;
		this.alpha0 = K * alpha;
		this.beta0 = V * beta;
		
		m_z = new int[K];
		n_z = new int[K];
		n_zv = new int[K][V];
		for(int k = 0; k < K; k++){
			n_z[k] = 0;
			m_z[k] = 0;
			for(int t = 0; t < V; t++){
				n_zv[k][t] = 0;
			}
		}
	}
	public void intialize(DocumentSet documentSet)
	{
		D = documentSet.D;
		z = new int[D];
		for(int d = 0; d < D; d++){
			Document document = documentSet.documents.get(d);
			int cluster = (int) (K * Math.random());
			z[d] = cluster;
			m_z[cluster]++;
			for(int w = 0; w < document.wordNum; w++){
				int wordNo = document.wordIdArray[w];
				int wordFre = document.wordFreArray[w];
				n_zv[cluster][wordNo] += wordFre; 
				n_z[cluster] += wordFre; 
			}
		}
	}
	public void gibbsSampling(DocumentSet documentSet)
	{
		for(int i = 0; i < iterNum; i++){
			for(int d = 0; d < D; d++){
				Document document = documentSet.documents.get(d);
				int cluster = z[d];
				m_z[cluster]--;
				for(int w = 0; w < document.wordNum; w++){
					int wordNo = document.wordIdArray[w];
					int wordFre = document.wordFreArray[w];
					n_zv[cluster][wordNo] -= wordFre;
					n_z[cluster] -= wordFre;
				}

				cluster = sampleCluster(d, document);
				
				z[d] = cluster;
				m_z[cluster]++;
				for(int w = 0; w < document.wordNum; w++){
					int wordNo = document.wordIdArray[w];
					int wordFre = document.wordFreArray[w];
					n_zv[cluster][wordNo] += wordFre; 
					n_z[cluster] += wordFre; 
				}
			}
		}
	}

	private int sampleCluster(int d, Document document)
	{ 
		double[] p = new double[K];
		int[] overflowCount = new int[K];

		for(int k = 0; k < K; k++){
			p[k] = (m_z[k] + alpha) / (D - 1 + alpha0);
			double valueOfRule2 = 1.0;
			int i = 0;
			for(int w=0; w < document.wordNum; w++){
				int wordNo = document.wordIdArray[w];
				int wordFre = document.wordFreArray[w];
				for(int j = 0; j < wordFre; j++){
					valueOfRule2 = isOverFlow(valueOfRule2, overflowCount, k); 
					valueOfRule2 *= (n_zv[k][wordNo] + beta + j) 
							 / (n_z[k] + beta0 + i);
					i++;
				}
			}
			p[k] *= valueOfRule2;			
		}
		
		reComputeProbs(p, overflowCount, K);

		for(int k = 1; k < K; k++){
			p[k] += p[k - 1];
		}
		double thred = Math.random() * p[K - 1];
		int kChoosed;
		for(kChoosed = 0; kChoosed < K; kChoosed++){
			if(thred < p[kChoosed]){
				break;
			}
		}
		
		return kChoosed;
	}
	
	private void reComputeProbs(double[] p, int[] overflowCount, int K)
	{
		int max = overflowCount[0];
		for(int k = 1; k < K; k++){
			if(overflowCount[k] > max && p[k] > 0){
				max = overflowCount[k];
			}
		}
		
		for(int k = 0; k < K; k++){			
			if(p[k] > 0){
				p[k] = p[k] * Math.pow(1e150, overflowCount[k] - max);
			}
		}		
	}

	private double isOverFlow(double valueOfRule2, int[] overflowCount, int k)
	{
		if(valueOfRule2 < 1e-150){
			overflowCount[k]--;
			return valueOfRule2 * 1e150;
		}
		return valueOfRule2;
	}

	public void output(DocumentSet documentSet, String outputPath)
	{
		String outputDir = outputPath + dataset + ParametersStr + "/";
		FileUtil.makeDir(outputDir);
		try{
			outputClusteringResult(outputDir, documentSet);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void outputClusteringResult(String outputDir, 
			DocumentSet documentSet) throws Exception
	{
		String outputPath = outputDir + this.dataset + "ClusteringResult" + ".txt";
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(outputPath), "UTF-8"));
		for(int d=0; d < documentSet.D; d++){
			int topic = z[d];
			writer.write(topic + "\n");
		}
		writer.flush();
		writer.close();
	}
}
