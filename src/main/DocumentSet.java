package main;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import Common.FileUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class DocumentSet{
	protected int documentNum;
	protected int[] clusterNoArray;
	List<Document> documents = new ArrayList<Document>();
	
	public DocumentSet(String dataDir, HashMap<String, Integer> wordToIdMap) throws JSONException {
		ArrayList<String> lines = new ArrayList<String>();
		FileUtil.readLines(dataDir, lines);		
		this.documentNum = lines.size();
		clusterNoArray = new int[lines.size()];
		for(int lineNo = 0; lineNo < lines.size(); lineNo++) {
			String line = lines.get(lineNo);
			JSONObject obj = new JSONObject(line);
		    String text = obj.getString("textCleaned");
		    clusterNoArray[lineNo] = obj.getInt("clusterNo");
		    Document document = new Document(text, wordToIdMap);
		    documents.add(document);	
		}
	}
}
