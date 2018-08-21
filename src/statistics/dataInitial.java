package statistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import main.Common;
import util.StringUtil;

//hyy
public class dataInitial {
	
	public static void getDataFromTable() {
		getSecurityCheckMethodsTable();
		getPublicMethodsInStubServiceTable();
		getCallerCalleeRelationship();		
	}
	
	//------get data from .csv files
		private static void getCallerCalleeRelationship() {
			// 使用读文件形式读取数据库中导出的文件
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader("CALLEDGE.csv"));
				reader.readLine();// 第一行信息，为标题信息，不用，如果需要，注释掉
				String line = null;
				while ((line = reader.readLine()) != null) {
					String tmp = line.trim().toString();
					String item[] = tmp.split("\\*");// CSV格式文件为逗号分隔符文件，这里根据逗号切分
		/*			System.out.println("--" + item[1]);
					System.out.println(item[2]);*/
					if (!statistics.commonData.callerSigMapCalleeSig.containsKey(item[1])) {
						Set<String> callees = new HashSet<>();
						callees.add(item[2]);
						statistics.commonData.callerSigMapCalleeSig.put(item[1], callees);
					} else {
						statistics.commonData.callerSigMapCalleeSig.get(item[1]).add(item[2]);
					}
													
					if (!statistics.commonData.calleeSigMapCallerSig.containsKey(item[2])) {
						Set<String> callers = new HashSet<>();
						callers.add(item[1]);
						statistics.commonData.calleeSigMapCallerSig.put(item[2], callers);
					} else {
						statistics.commonData.calleeSigMapCallerSig.get(item[2]).add(item[1]);
					}								
				}
			} catch (Exception e) {
				e.printStackTrace();
			}		
	/*		long  cnt=0;
			for(String caller:callerSigMapCalleeSig.keySet()) {
				Set<String> callee = callerSigMapCalleeSig.get(caller);
				cnt=cnt+callee.size();
			}
			System.out.println("--"+cnt);*/		
		}
		
		
		
		private static void getPublicMethodsInStubServiceTable() {
			// 使用读文件形式读取数据库中导出的文件
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader("PublicMethodsInStubService.csv"));
				reader.readLine();// 第一行信息，为标题信息，不用，如果需要，注释掉
				String line = null;
				while ((line = reader.readLine()) != null) {
					String tmp = line.trim().toString();
					String item[] = tmp.split("\\*");// CSV格式文件为逗号分隔符文件，这里根据逗号切分
					statistics.commonData.publicMethods.add(item[2]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("publicMethodsNum:"+statistics.commonData.publicMethods.size());
		}
		
		
		private static void getSecurityCheckMethodsTable() {
			// 使用读文件形式读取数据库中导出的文件
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader("SecurityCheckMethods1.csv"));
				reader.readLine();// 第一行信息，为标题信息，不用，如果需要，注释掉
				String line = null;
				while ((line = reader.readLine()) != null) {
					String tmp = line.trim().toString();
					String item[] = tmp.split("\\*");// CSV格式文件为逗号分隔符文件，这里根据逗号切分

					if (!statistics.commonData.securiyCheckTable.containsKey(item[1])) {
						ArrayList<String> Sig = new ArrayList<>();
						Sig.add(item[2]);
						statistics.commonData.securiyCheckTable.put(item[1], Sig);
					} else {
						statistics.commonData.securiyCheckTable.get(item[1]).add(item[2]);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	       
			/*		 //使用jxl.jar处理数据库中导出的xls文件
			Sheet SecurityCheckMethodsTable;
	        Workbook workbook;
	        Cell cell1;      
	       try { 

	        	File inputWb= new File("SecurityCheckMethods.xls");
	        	   //t.xls为要读取的excel文件名
	        	workbook= Workbook.getWorkbook(inputWb);             
	            //获得第一个工作表对象(ecxel中sheet的编号从0开始,0,1,2,3,....)
	        	SecurityCheckMethodsTable=workbook.getSheet(0); 
	            //获取左上角的单元格
	            cell1=SecurityCheckMethodsTable.getCell(0,0);
	            System.out.println("标题："+cell1.getContents());        	       	
	        } catch (Exception e) { 
	            e.printStackTrace(); 
	        } 
			*/
			
			
			//直接读db数据库出bug
			/*String tableName="SecurityCheckMethods";
			ResultSet resultSet=Common.database.select(	
					"SELECT * FROM "+tableName+";"
					);
			try {
				
				//if( !resultSet.next()) {System.out.println("1111");}
				
				String TYPE=resultSet.getString("Type");
				String METHODNAME=resultSet.getString("MethodName");
				System.out.println(TYPE+ "---" +METHODNAME );
				
				while(resultSet!=null && resultSet.next()){ // resultSet.next() 为false
					
					;
					System.out.println("oooo");
					String TYPE=resultSet.getString("Type");
					String METHODNAME=resultSet.getString("MethodName");
					System.out.println(TYPE+ "---" +METHODNAME );
					
					//int rank=resultSet.getInt("RANKING");
					//parcelableClassNameMapRanking.put(className,rank);
					
					if(!securiyCheckTable.containsKey(TYPE))
						securiyCheckTable.put(TYPE,new Set<METHODNAME>());
					securiyCheckTable.get(TYPE).addAll(methodAndValidation.validations);
				}
							
			} catch (SQLException e) {
				e.printStackTrace();
			}*/
		}
		
		
		
		
		
		
		//----------save memory data to database------------------------
		//callerMethodSignatureMapCalleeMethodSignatures.get(sootMethod.getSignature()).add(callee.getSignature());
		private static void saveCallerCalleeRelationship() {
			createTableInDataProvider();
			for(String caller: main.Memory.callerMethodSignatureMapCalleeMethodSignatures.keySet()) {
				for(String callee:main.Memory.callerMethodSignatureMapCalleeMethodSignatures.get(caller)) {
					String value=StringUtil.sqlString(caller)+", "+ StringUtil.sqlString(callee);
					Common.database.executeUpdate(
							"INSERT INTO CallRelation (Caller,Callee) VALUES ("+value+");");	
				}
			}
		}
		
			
		private static void createTableInDataProvider(){
			String tableName="CallRelation";
			Common.database.executeUpdate(
					"CREATE TABLE IF NOT EXISTS "+tableName+" ("+
							"ID				INTEGER  PRIMARY KEY AUTOINCREMENT,"+
							"Caller		TEXT,"+
							"Callee     TEXT"+
							");"
					);
		}


}
