package module.stubservice;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataflow.DataFlowAnalyzer;
import main.Common;
import main.Memory;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.jimple.internal.ImmediateBox;
import util.StringUtil;

public class SearchServiceInServiceManager {
	
	private static String table_publicMethods = "PublicMethodsInStubService";
	private static String table_stubServices = "StubService";
	
	public static List<String> serviceClassNameList=new ArrayList<>();
	public static List<String> publicMethodSignatureList=new ArrayList<>();
	
	
	private static final String getService_ServiceManager = "<android.os.ServiceManager: android.os.IBinder getService(java.lang.String)>";
	private static final String asInterface_Pattern = "<.*?Stub:.*?asInterface\\(android.os.IBinder\\)>";

	private static Map<String, String> stubServiceMapInServiceManager = new HashMap<String, String>();
	private static Map<String, String> stubServiceMapImplService = new HashMap<String, String>();
	private static Map<SootClass, SootClass> stubClassMapImplClass = new HashMap<SootClass, SootClass>();
	
	
	
	public void run(boolean needInsert) {
		System.out.println("StubService.Search : " + needInsert);
		if (needInsert) {
			searchStubServiceClassNames();
			mapStubServiceToImplService();
			System.out.println("Stub service in service manager:" + stubServiceMapInServiceManager.size());
			try {
				insertStubServices();
				SearchAndInsertPublicMethodInStubService();
			} catch (SQLException e) {
				e.printStackTrace();
			}	
		}else {			
			//initBySelectFromStubServiceTable();
		}
	}
	
	private static void searchStubServiceClassNames() {
		List<SootClass> allClasses = new ArrayList<>();
		allClasses.addAll(Scene.v().getClasses());
		for (SootClass sootClass : allClasses) {
			List<SootMethod> allMethods = new ArrayList<>();
			allMethods.addAll(sootClass.getMethods());
			for (SootMethod method : allMethods) {
				if (!method.isConcrete()) {
					continue;
				}
				Body body = null;
				try {
					body = method.retrieveActiveBody();
				} catch (Exception e) {

				}
				if (body == null) {
					continue;
				}
				for (Unit unit : body.getUnits()) {
					if (((Stmt) unit).containsInvokeExpr() && ((Stmt) unit).getInvokeExpr().getMethod().getSignature()
							.equals(getService_ServiceManager)) {
						String serviceKey = "";
						String serviceStubClass = "";
						if (((Stmt) unit).getInvokeExpr().getArgBox(0) instanceof ImmediateBox) {
							serviceKey = ((Stmt) unit).getInvokeExpr().getArgBox(0).getValue().toString();
						}
						if (!serviceKey.isEmpty() && stubServiceMapInServiceManager.containsKey(serviceKey))
							continue;
						HashMap<Unit, ValueBox> allUse = DataFlowAnalyzer.intraProcedural_findAllUseUnitsAndValueBoxes(
								method.retrieveActiveBody(), unit, unit.getDefBoxes().get(0)); 
						for (Unit target : allUse.keySet()) {
							if (((Stmt) target).containsInvokeExpr() && ((Stmt) target).getInvokeExpr().getMethod()
									.getSignature().matches(asInterface_Pattern)) {
								serviceStubClass = ((Stmt) target).getInvokeExpr().getMethod().getDeclaringClass()
										.getName();
								stubServiceMapInServiceManager.put(serviceKey, serviceStubClass);
								//System.out.println(serviceKey + " : " + serviceStubClass);
								break;
							}
						}
					}
				}
			}
		}
	}
	
	private static void mapStubServiceToImplService() {
		Set<String> stubServicesInServiceManager = new HashSet<String>();
		for (String stubServiceSignatures : stubServiceMapInServiceManager.values()) {
			stubServicesInServiceManager.add(stubServiceSignatures);
		}
		for (SootClass sootClass : Scene.v().getClasses()) {
			if (sootClass.hasSuperclass()
					&& stubServicesInServiceManager.contains(sootClass.getSuperclass().getName())) {
				stubServiceMapImplService.put(sootClass.getSuperclass().getName(), sootClass.getName());
				stubClassMapImplClass.put(sootClass.getSuperclass(), sootClass);
			}
		}

	}

	private static void insertStubServices() throws SQLException {
		String tableName = table_stubServices;
		Common.database.executeUpdate(
				"CREATE TABLE IF NOT EXISTS " + tableName + " (" + "ID				INTEGER  PRIMARY KEY AUTOINCREMENT,"
						+ "Key		TEXT," + "StubClassName     TEXT," + "ImplClassName     TEXT" + ");");
		for (String key : stubServiceMapInServiceManager.keySet()) {
			String stubClass = stubServiceMapInServiceManager.get(key) + "";
			String implClass = stubServiceMapImplService.get(stubClass) + "";

			String value = StringUtil.sqlString(key) + ", " + StringUtil.sqlString(stubClass) + ", "
					+ StringUtil.sqlString(implClass);
			Common.database.executeUpdate(
					"INSERT INTO " + tableName + " (Key,StubClassName,ImplClassName)" + "VALUES (" + value + ");");
			serviceClassNameList.add(implClass);
		}
		

	}

	private static void SearchAndInsertPublicMethodInStubService() throws SQLException{
		String tableName = table_publicMethods;
		Common.database.executeUpdate(
				"CREATE TABLE IF NOT EXISTS " + tableName + " (" + "ID				INTEGER  PRIMARY KEY AUTOINCREMENT,"
						+ "ClassName		TEXT," + "Signature     TEXT"+ ");");
		for(String stubClassName : stubServiceMapImplService.keySet()){
			
			SootClass proxyClass=Memory.classNameMapSootClass.get(stubClassName+"$Proxy");
			if(proxyClass==null){
				continue;
			}
			
			for(SootMethod proxyMethod:proxyClass.getMethods()){
				String proxySig=proxyMethod.getSignature();
				String implClassName=stubServiceMapImplService.get(stubClassName);
				String implSig=proxySig.replace(proxyClass.getName(), implClassName);
				SootMethod implMethod=Memory.methodSignatureMapSootMethod.get(implSig);
				if(implMethod!=null){
					publicMethodSignatureList.add(implSig);
					String value=StringUtil.sqlString(implClassName)+", "+StringUtil.sqlString(implSig);
					Common.database.executeUpdate(
							"INSERT INTO " + tableName + " (ClassName,Signature)" + "VALUES (" + value + ");"
							);
					
				}
				else{
				}
			}
			
		}
	}
	
}
