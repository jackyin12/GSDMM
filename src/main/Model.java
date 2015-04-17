package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Model{
	int K; 
	double alpha;
	double beta;
	String dataset;
	String ParametersStr;
	int V; 
	int D; 
	int iterNum; 
	double alpha0;
	double beta0;
	double[][] phi_zv;
	int[] z; 
	int[] m_z; 
	int[][] n_zv;
	int[] n_z; 
	double smallDouble = 1e-150;
	double largeDouble = 1e150;

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
		
		this.m_z = new int[K];
		this.n_z = new int[K];
		this.n_zv = new int[K][V];
		for(int k = 0; k < K; k++){
			this.n_z[k] = 0;
			this.m_z[k] = 0;
			for(int t = 0; t < V; t++){
				this.n_zv[k][t] = 0;
			}
		}
	}
	public void intialize(DocumentSet documentSet)
	{
		this.D = documentSet.D;
		this.z = new int[this.D];
		for(int d = 0; d < this.D; d++){
			Document document = documentSet.documents.get(d);
			int cluster = (int) (this.K * Math.random());
			this.z[d] = cluster;
			this.m_z[cluster]++;
			for(int w = 0; w < document.wordNum; w++){
				int wordNo = document.wordIdArray[w];
				int wordFre = document.wordFreArray[w];
				this.n_zv[cluster][wordNo] += wordFre; 
				this.n_z[cluster] += wordFre; 
			}
		}
	}
	public void gibbsSampling(DocumentSet documentSet)
	{
		for(int i = 0; i < this.iterNum; i++){
			for(int d = 0; d < this.D; d++){
				Document document = documentSet.documents.get(d);
				int cluster = this.z[d];
				this.m_z[cluster]--;
				for(int w = 0; w < document.wordNum; w++){
					int wordNo = document.wordIdArray[w];
					int wordFre = document.wordFreArray[w];
					this.n_zv[cluster][wordNo] -= wordFre;
					this.n_z[cluster] -= wordFre;
				}

				cluster = sampleCluster(d, document);
				
				this.z[d] = cluster;
				this.m_z[cluster]++;
				for(int w = 0; w < document.wordNum; w++){
					int wordNo = document.wordIdArray[w];
					int wordFre = document.wordFreArray[w];
					this.n_zv[cluster][wordNo] += wordFre; 
					this.n_z[cluster] += wordFre; 
				}
			}
		}
	}

	private int sampleCluster(int d, Document document)
	{ 
		double[] prob = new double[this.K];
		int[] overflowCount = new int[this.K];

		for(int k = 0; k < this.K; k++){
			prob[k] = (this.m_z[k] + this.alpha) / (this.D - 1 + this.alpha0);
			double valueOfRule2 = 1.0;
			int i = 0;
			for(int w=0; w < document.wordNum; w++){
				int wordNo = document.wordIdArray[w];
				int wordFre = document.wordFreArray[w];
				for(int j = 0; j < wordFre; j++){
					if(valueOfRule2 < this.smallDouble){
						overflowCount[k]--;
						valueOfRule2 *= this.largeDouble;
					}
					valueOfRule2 *= (this.n_zv[k][wordNo] + this.beta + j) 
							 / (this.n_z[k] + this.beta0 + i);
					i++;
				}
			}
			prob[k] *= valueOfRule2;			
		}
		
		reComputeProbs(prob, overflowCount, this.K);

		for(int k = 1; k < this.K; k++){
			prob[k] += prob[k - 1];
		}
		double thred = Math.random() * prob[this.K - 1];
		int kChoosed;
		for(kChoosed = 0; kChoosed < this.K; kChoosed++){
			if(thred < prob[kChoosed]){
				break;
			}
		}
		
		return kChoosed;
	}
	
	private void reComputeProbs(double[] prob, int[] overflowCount, int K)
	{
		int max = Integer.MIN_VALUE;
		for(int k = 0; k < K; k++){
			if(overflowCount[k] > max && prob[k] > 0){
				max = overflowCount[k];
			}
		}
		
		for(int k = 0; k < K; k++){			
			if(prob[k] > 0){
				prob[k] = prob[k] * Math.pow(this.largeDouble, overflowCount[k] - max);
			}
		}		
	}

	public void output(DocumentSet documentSet, String outputPath) throws Exception
	{
		String outputDir = outputPath + dataset + ParametersStr + "/";
		
		File file = new File(outputDir);
		if(!file.exists()){
			if(!file.mkdirs()){
				System.out.println("Failed to create directory:" + outputDir);
			}
		}
		
		outputClusteringResult(outputDir, documentSet);
	}

	public void outputClusteringResult(String outputDir, DocumentSet documentSet) throws Exception
	{
		String outputPath = outputDir + this.dataset + "ClusteringResult.txt";
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(outputPath), "UTF-8"));
		for(int d = 0; d < documentSet.D; d++){
			int topic = this.z[d];
			writer.write(topic + "\n");
		}
		writer.flush();
		writer.close();
	}
}
