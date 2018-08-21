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
//		try {
//			int q = 0;
//			ResultSet resultSet = Common.database.select("Select * from CallEdge;");
//			while(resultSet.next()) {
//				q++;
//				String s = resultSet.getString("SOURCE");
//				String t = resultSet.getString("TARGET");
//				if(q <= 10) {
//					System.out.println("q=" + q);
//					System.out.println("s=" + s);
//					System.out.println("t=" + t);
//				}
//				if(q > 10) {
//					return;
//				}
//				source.add(s);
//				target.add(t);
//				ArrayList<String> children = null;
//				if(SourceToTarget.containsKey(s)) {
//					children = SourceToTarget.get(s);
//				} else {
//					children = new ArrayList<String>();
//					SourceToTarget.put(s, children);
// 				}
//				children.add(t);
//			}
//			System.out.println("Size=" + SourceToTarget.size());
//			PrintWriter out = null;
//			int sum = 0;
//			for(ArrayList<String> strList : SourceToTarget.values()) {
//				out = new PrintWriter("D:\\feature_aosp\\feature" + (sum++) + ".txt");
//				for(int i = 0; i < strList.size(); i++) {
//					out.println(strList.get(i));
//				}
//				out.close();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.exit(0);
		//下面是华为的提取代码
		try {	
			ResultSet resultSet = Common.database.select("Select * from CallEdge;");
			while(resultSet.next()) {
				String s = resultSet.getString("SOURCE");
				String t = resultSet.getString("TARGET");
				source.add(s);
				target.add(t);
				ArrayList<String> children = null;
				if(SourceToTarget.containsKey(s)) {
					children = SourceToTarget.get(s);
				} else {
					children = new ArrayList<String>();
					SourceToTarget.put(s, children);
 				}
				children.add(t);
			}
			ResultSet resultSet1 = Common.database.select("select * from PublicMethodsInStubService;");
			while(resultSet1.next()) {
				String a = resultSet1.getString("METHOD_SIGNATURE");
				needAnalyze.add(a);
			}
			System.out.println("Size=" + needAnalyze.size());
			int sum = 0;
			for(int i = 0; i < needAnalyze.size(); i++) {
				out = new PrintWriter("D:\\feature\\feature" + (sum++) + ".txt");
				String analyzeName = needAnalyze.get(i);
				if(!ProcessedSignature.containsKey(analyzeName)) {
					CFG_Node root = new CFG_Node(analyzeName);
					ProcessedSignature.put(analyzeName, true);
					dfs_build(root);
				}
				out.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(!Common.database.isClosed())
				Common.database.close();
		}
	}
	static HashMap<String, Boolean> ProcessedSignature = new HashMap<>();
	public static void dfs_build(CFG_Node root) {
		if(!SourceToTarget.containsKey(root.getSignature())) {
			return;
		}
		ArrayList<String> children = SourceToTarget.get(root.getSignature());
		if(children.size() <= 0) {
			return;
		}
		out.println(root.getSignature());
		for(int i = 0; i < children.size(); i++) {
			String childName = children.get(i);
			if(ProcessedSignature.containsKey(childName)) {
				continue;
			}
			CFG_Node tRoot = new CFG_Node(childName);
			root.getNext().add(tRoot);
			ProcessedSignature.put(childName, true);
			dfs_build(tRoot);
		}
	}
}

