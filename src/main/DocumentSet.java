package main;
import java.util.ArrayList;
import java.util.HashMap;
import Common.FileUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class DocumentSet{
	protected int D;
	ArrayList<Document> documents = new ArrayList<Document>();
	
	public DocumentSet(String dataDir, HashMap<String, Integer> wordToIdMap) 
						throws JSONException {
		ArrayList<String> lines = new ArrayList<String>();
		FileUtil.readLines(dataDir, lines);		
		
		this.D = lines.size();
		String line;
		for(int lineNo = 0; lineNo < lines.size(); lineNo++) {
			line = lines.get(lineNo);
			JSONObject obj = new JSONObject(line);
		    String text = obj.getString("text");
		    Document document = new Document(text, wordToIdMap);
		    documents.add(document);	
		}
	}
}
