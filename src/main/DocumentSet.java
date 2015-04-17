package main;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;

public class DocumentSet{
	int D = 0;
	ArrayList<Document> documents = new ArrayList<Document>();
	
	public DocumentSet(String dataDir, HashMap<String, Integer> wordToIdMap) 
			 					throws Exception
	{
		BufferedReader in = new BufferedReader(new FileReader(dataDir));
		String line;
		
		while((line=in.readLine()) != null){
			this.D++;
			JSONObject obj = new JSONObject(line);
			String text = obj.getString("text");
			Document document = new Document(text, wordToIdMap);
			this.documents.add(document);
		}
		
		in.close();
	}
}
