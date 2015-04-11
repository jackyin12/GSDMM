package main;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import Common.FileUtil;
import org.json.JSONObject;

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
		D = documentSet.documentNum;
		z = new int[D];
		for(int d = 0; d < D; d++){
			Document document = documentSet.documents.get(d);
			int clusterNo = (int) (K * Math.random());
			z[d] = clusterNo;
			m_z[clusterNo]++;
			for(int w = 0; w < document.wordNum; w++){
				int wordNo = document.wordIdArray[w];
				int wordFre = document.wordFreArray[w];
				n_zv[clusterNo][wordNo] += wordFre; 
				n_z[clusterNo] += wordFre; 
			}
		}
	}
	public void gibbsSampling(DocumentSet documentSet, int iterNum)
	{
		for(int i = 0; i < iterNum; i++){
			for(int d = 0; d < D; d++){
				Document document = documentSet.documents.get(d);
				int clusterNo = z[d];
				m_z[clusterNo]--;
				for(int w = 0; w < document.wordNum; w++){
					int wordNo = document.wordIdArray[w];
					int wordFre = document.wordFreArray[w];
					n_zv[clusterNo][wordNo] -= wordFre;
					n_z[clusterNo] -= wordFre;
				}

				clusterNo = sampleClusterNo(d, document);
				
				z[d] = clusterNo;
				m_z[clusterNo]++;
				for(int w = 0; w < document.wordNum; w++){
					int wordNo = document.wordIdArray[w];
					int wordFre = document.wordFreArray[w];
					n_zv[clusterNo][wordNo] += wordFre; 
					n_z[clusterNo] += wordFre; 
				}
			}
		}
	}

	private int sampleClusterNo(int d, Document document)
	{ 
		double[] p = new double[K];
		int[] overFlowCount = new int[K];

		for(int k = 0; k < K; k++){
			p[k] = (m_z[k] + alpha) / (D - 1 + alpha0);
			double valueOfRule2 = 1.0;
			int i = 0;
			for(int w=0; w < document.wordNum; w++){
				int wordNo = document.wordIdArray[w];
				int wordFre = document.wordFreArray[w];
				for(int j = 0; j < wordFre; j++){
					double tmp = (n_zv[k][wordNo] + beta + j) 
								 / (n_z[k] + beta0 + i);
					i++;
					valueOfRule2 *= tmp;
					valueOfRule2 = isOverFlow(valueOfRule2, overFlowCount, k); 
				}
			}
			p[k] *= valueOfRule2;			
		}
		
		reComputeProbs(p, overFlowCount, K);

		for(int k = 1; k < K; k++){
			p[k] += p[k - 1];
		}
		double thred = Math.random() * p[K - 1];
		int kchoosed;
		for(kchoosed = 0; kchoosed < K; kchoosed++){
			if(thred < p[kchoosed]){
				break;
			}
		}
		
		return kchoosed;
	}
	
	private void reComputeProbs(double[] p, int[] overFlowCount, int K)
	{
		int max = overFlowCount[0];
		for(int k = 1; k < K; k++){
			if(overFlowCount[k] > max && p[k] > 0){
				max = overFlowCount[k];
			}
		}
		
		for(int k = 0; k < K; k++){			
			if(p[k] > 0){
				p[k] = p[k] * Math.pow(1e150, overFlowCount[k] - max);
			}
		}		
	}

	private double isOverFlow(double valueOfRule2, int[] overFlowCount, int k)
	{
		if(valueOfRule2 < 1e-150){
			overFlowCount[k]--;
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
		String documentsJsonPath = outputDir + this.dataset + "ClusteringResult" + ".txt";
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(documentsJsonPath), "UTF-8"));
		for(int d=0; d < documentSet.documentNum; d++){
			JSONObject obj = new JSONObject();
			int topic = z[d];
			obj.put("trueCluster", documentSet.clusterNoArray[d]);
			obj.put("predictedCluster", topic);
			writer.write(obj + "\n");
		}
		writer.flush();
		writer.close();
	}
}
