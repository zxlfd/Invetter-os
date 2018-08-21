package module.parcel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.Common;
import main.Memory;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import util.StringUtil;
import util.TimeMeasurement;


public class SearchParcelableClass {
	

			public static Map<String,Integer> parcelableClassNameMapRanking=new HashMap<String, Integer>();
			private static final String Android_Parcelable_Interface="android.os.Parcelable";
			private static final String TAG="ParcelableClass";
			public static List<String> tmpUseStrings = new LinkedList<>();
			//private static Map<String, Integer> parceableNameCount = new HashMap<>(); 
			public static Map<SootClass,Integer>ParcelableClassMapRank =new HashMap<>();
			
			public void run(boolean needInsert){
				if(needInsert){
					initByInsertIntoDataProvider();
				}
				else
					initBySelectFromDataProvider();

			}
			private static void initByInsertIntoDataProvider(){
				Set<String> father_or_interface=new HashSet<String>();
				father_or_interface.add(Android_Parcelable_Interface); //"android.os.Parcelable"				
				createTableInDataProvider();

				int lastSize=0;
				while(father_or_interface.size()>lastSize){
					lastSize=father_or_interface.size();
					for(String clazz:Memory.classNameMapSootClass.keySet()){
						String fatherClassName=Memory.sonClassNameMapFatherClassName.get(clazz);
						List<String> interfaceClassNames=Memory.implClassNameMapInterfaceClassNames.get(clazz);
						if(fatherClassName!=null){
							if(father_or_interface.contains(fatherClassName))
								father_or_interface.add(clazz);
						}
						if(interfaceClassNames!=null){
							for(String interfaceClassName:interfaceClassNames)
								if(father_or_interface.contains(interfaceClassName))
									father_or_interface.add(clazz);
						}
					}
				}
				
				for(String className:father_or_interface){					
					SootClass sootClass=Memory.classNameMapSootClass.get(className); //add by hyy
					int rank=queryRanking(sootClass);//add by hyy
					insertParcelableClass(sootClass, rank);//amend by hyy
					tmpUseStrings.add(className);
				}			
				TimeMeasurement.show(SearchParcelableClass.class.getName()+" finish : "+parcelableClassNameMapRanking.size());
				
			}


			
			//add by hyy
			private static int queryRanking(SootClass sootClass) {
				int ranking = -1;		
				try {
					SootMethod parcelMethod = sootClass.getMethodByName("writeToParcel");
					
					String fullClassName=sootClass.getName().toLowerCase(); //modify "toLowerCase()" by hyy
					String className = fullClassName.substring(fullClassName.lastIndexOf('.')+1);
					if (className.contains("package")||className.contains("id")||
							className.contains("token")||className.contains("file")||
							className.contains("url")||className.contains("uri")||
							className.contains("name")) {						
						ranking = 2;
					} else if(className.contains("type")||className.contains("flag")) {
						ranking = 1;
					} else {
						if(parcelMethod == null) return -1; 
						for(Unit unit:parcelMethod.getActiveBody().getUnits()) {						
							if (((Stmt)unit) instanceof AssignStmt ) {
								String invokeString = unit.getUseBoxes().get(0).toString().toLowerCase();
								if(!invokeString.contains("<") || !invokeString.contains(">")) continue;
								String variableName = invokeString.substring(invokeString.lastIndexOf(' ')+1,invokeString.indexOf('>')).toLowerCase();
								if (variableName.contains("package")||variableName.contains("id")||
										variableName.contains("token")||variableName.contains("file")||
										variableName.contains("url")||variableName.contains("uri")||
										variableName.contains("name")) {
									ranking = 2;
									break;
								} else if(variableName.contains("type")||variableName.contains("flag")) {
									ranking = 1;
								} else {
									if(ranking <0) ranking = 0;									
								}
/*								if (parceableNameCount.containsKey(variableName)) {
									int cnt = parceableNameCount.get(variableName);
									parceableNameCount.put(variableName, cnt +1);
								} else {
									parceableNameCount.put(variableName, 1);
								}*/
							}
						}
					}
				} catch (RuntimeException exception) {
					//exception.printStackTrace();
					ranking = -1;
				}
				return ranking;
		}
						
			
			private static void initBySelectFromDataProvider(){
				String tableName=TAG;
				ResultSet resultSet=Common.database.select(
						"SELECT * FROM "+tableName+";"
						);
				try {
					while(resultSet!=null && resultSet.next()){
						String className=resultSet.getString("CLASSNAME");
						int rank=resultSet.getInt("RANKING");
						parcelableClassNameMapRanking.put(className,rank);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			private static void createTableInDataProvider(){
				String tableName=TAG;//ParcelableClass table
				Common.database.executeUpdate(
						"CREATE TABLE IF NOT EXISTS "+tableName+" ("+
								"ID				INTEGER  PRIMARY KEY AUTOINCREMENT,"+
								"CLASSNAME		TEXT,"+
								"SUPERCLASS     TEXT,"+
								"INTERFACES     TEXT,"+
								"RANKING        TEXT"+
								");"
						);
			}
			private static void insertParcelableClass(SootClass clazz, int ranking){
				String tableName=TAG;//ParcelableClass table
				String interfaces="";
				for(SootClass interfaceClass : clazz.getInterfaces()){
					interfaces+=(interfaceClass.getName()+";");
				}

				String value=StringUtil.sqlString(clazz.getName())+", "+
						StringUtil.sqlString(clazz.getSuperclass().getName())+","+
						StringUtil.sqlString(interfaces)+", "+
						StringUtil.sqlString(String.valueOf(ranking));


				Common.database.executeUpdate(
						"INSERT INTO "+tableName+" (CLASSNAME,SUPERCLASS,INTERFACES,RANKING)"+
								"VALUES ("+value+");"
						);

			}
			
			public void test() {}

}
