package module.stubservice;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

//import com.google.protobuf.Method;

import main.Common;
import main.Memory;
import soot.SootClass;
import soot.SootMethod;
import util.LogUtil;
import util.StringUtil;

public class SearchAdditionalStubService {
	
	private static String table_publicMethods = "AdditionalPublicMethods";
	private static String table_stubServices = "AdditionalStubService";
	
	public static List<String> serviceClassNameList=new ArrayList<>();
	public static List<String> publicMethodSignatureList=new ArrayList<>();
	
	
	
	public void run(boolean needInsert){
		if(needInsert){
			try {
				Common.database.executeUpdate(
						"CREATE TABLE IF NOT EXISTS " + table_stubServices + " (" + "ID				INTEGER  PRIMARY KEY AUTOINCREMENT,"
								 + "StubClassName     TEXT," + "ImplClassName     TEXT" + ");");
			Common.database.executeUpdate(
					"CREATE TABLE IF NOT EXISTS " + table_publicMethods + " (" + "ID				INTEGER  PRIMARY KEY AUTOINCREMENT,"
							+ "ClassName		TEXT," + "Signature     TEXT"+ ");");
			searchAndInsertAdditionalStubClassAndPublicMethods();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	
	private void searchAndInsertAdditionalStubClassAndPublicMethods() throws SQLException{
		for(String methodSig:SearchServiceInServiceManager.publicMethodSignatureList){
			SootMethod methodInStubService=Memory.methodSignatureMapSootMethod.get(methodSig);
			if(methodInStubService==null) continue;
			String returnType=methodSig.split(" ")[1].replace("[]", "");
			SootClass proxyClass=Memory.classNameMapSootClass.get(returnType+"$Stub$Proxy");
			if(proxyClass==null) continue;
			String implClassName=Memory.stubClassNameMapImplClassName.get(returnType+"$Stub");
			if(implClassName==null) continue;
			SootClass implClass=Memory.classNameMapSootClass.get(implClassName);
			if(implClass==null) continue;
			serviceClassNameList.add(implClassName);
			String value=StringUtil.sqlString(returnType+"$Stub")+", "+StringUtil.sqlString(implClassName);
			Common.database.executeUpdate(
					"INSERT INTO " + table_stubServices + " (StubClassName,ImplClassName)" + "VALUES (" + value + ");"
					);
			
			for(SootMethod proxyMethod:proxyClass.getMethods()){
				String proxySig=proxyMethod.getSignature();
				String implSig=proxySig.replace(proxyClass.getName(), implClassName);
				SootMethod implMethod=Memory.methodSignatureMapSootMethod.get(implSig);
				if(implMethod!=null){
					publicMethodSignatureList.add(implSig);
					value=StringUtil.sqlString(implClassName)+", "+StringUtil.sqlString(implSig);
					Common.database.executeUpdate(
							"INSERT INTO " + table_publicMethods + " (ClassName,Signature)" + "VALUES (" + value + ");"
							);
				}
				else{
					LogUtil.debug("SearchAdditionalStubService", "Impl is null : "+proxySig+" : "+implSig);
				}
			}
			
			
			
		}
	}
	

}
