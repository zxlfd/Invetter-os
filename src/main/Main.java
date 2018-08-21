package main;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;



import edu.stanford.nlp.util.Index;
import zcfg.CFG_Node;


public class Main {
	static HashMap<String, ArrayList<String>> SourceToTarget = new HashMap<>();
	static ArrayList<String> source = new ArrayList<>();
	static ArrayList<String> target = new ArrayList<>();
	static ArrayList<String> needAnalyze = new ArrayList<>();
	static PrintWriter out = null;
	public static void main(String[] args) {
		String[] soot_args = new String[] { "-pp", "-allow-phantom-refs", "-w",
				"-process-dir", Common.DebugDir ,"-f", "n" };
		System.out.println("System dir = " + System.getProperty("user.dir"));
		if (args.length > 0) {
			System.out.println("args[0]:"+args[0]);
			soot_args[4] = args[0];
		}

		try {	
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(!Common.database.isClosed())
				Common.database.close();
		}
	}

}

