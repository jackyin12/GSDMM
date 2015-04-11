package Common;

import java.util.*;
import java.io.*;
import java.util.StringTokenizer;

public class FileUtil 
{
	public static void makeDir(String fileName)
	{
		File file = new File(fileName);
		if(!file.exists()){
			if(!file.mkdir()){
				System.out.println("Failed to create directory:" + fileName);
			}
		}
	}
	
	public static void readLines(String file, ArrayList<String> lines) 
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					lines.add(line);
				}
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void tokenize(String line, ArrayList<String> tokens) 
	{
		StringTokenizer st = new StringTokenizer(line);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			tokens.add(token);
		}
	}
}