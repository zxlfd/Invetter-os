package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dataflow.ClassAnalyzer;
import module.inputvalidation.validationtype.ValidationType;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import util.StringUtil;

public class Memory {
	
	
	public static ConcurrentHashMap<String, SootClass> classNameMapSootClass=new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, String> stubClassNameMapImplClassName=new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, String> sonClassNameMapFatherClassName=new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, List<String>> implClassNameMapInterfaceClassNames=new ConcurrentHashMap<>();
	
	public static ConcurrentHashMap<String, SootMethod> methodSignatureMapSootMethod=new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, HashSet<String>> methodNameMapMethodSignatures=new ConcurrentHashMap<>();
	
	public static ConcurrentHashMap<String, HashSet<String>> calleeMethodSignatureMapCallerMethodSignatures=new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, HashSet<String>> callerMethodSignatureMapCalleeMethodSignatures=new ConcurrentHashMap<>();
	

	
	public static ConcurrentHashMap<String, String> swappedMethodSigntureMapActualMethodSignature=new ConcurrentHashMap<>();
	
	public static ConcurrentHashMap<String, List<ValidationType>> sootMethodSignatureAndArgsNumMapValidationTypes=new ConcurrentHashMap<String, List<ValidationType>>();
	
	
	
	
	
	
	public static void init(){
		
		for(SootClass sootClass : Scene.v().getClasses()){
			classNameMapSootClass.put(sootClass.getName(), sootClass);
			if(sootClass.hasSuperclass()){
				SootClass superClass=sootClass.getSuperclass();
				sonClassNameMapFatherClassName.put(sootClass.getName(), superClass.getName());
				if(superClass.getName().contains("$Stub"))
					stubClassNameMapImplClassName.put(superClass.getName(), sootClass.getName());
			}
			for(SootClass interFace : sootClass.getInterfaces()){
				if(!implClassNameMapInterfaceClassNames.containsKey(sootClass.getName())){
					implClassNameMapInterfaceClassNames.put(sootClass.getName(), new ArrayList<String>());
				}
				implClassNameMapInterfaceClassNames.get(sootClass.getName()).add(interFace.getName());	
			}
			
			for(SootMethod sootMethod:sootClass.getMethods()){
				if(!ClassAnalyzer.isValidMethod(sootMethod)) continue;
				methodSignatureMapSootMethod.put(sootMethod.getSignature(), sootMethod);
				
				callerMethodSignatureMapCalleeMethodSignatures.put(sootMethod.getSignature(), new HashSet<String>());
				if(!methodNameMapMethodSignatures.containsKey(sootMethod.getName())){
					methodNameMapMethodSignatures.put(sootMethod.getName(), new HashSet<String>());
				}
				methodNameMapMethodSignatures.get(sootMethod.getName()).add(sootMethod.getSignature());
				
				if(sootMethod.isConcrete()){
					Body body=sootMethod.retrieveActiveBody();
					for(Unit unit : body.getUnits()){
						if(((Stmt)unit).containsInvokeExpr()){
							SootMethod callee=((Stmt)unit).getInvokeExpr().getMethod();
							if(!ClassAnalyzer.isValidMethod(callee)) continue;
							if(!methodNameMapMethodSignatures.containsKey(callee.getName())){
								methodNameMapMethodSignatures.put(callee.getName(), new HashSet<String>());
							}
							methodNameMapMethodSignatures.get(callee.getName()).add(callee.getSignature());
							if(!calleeMethodSignatureMapCallerMethodSignatures.containsKey(callee.getSignature()))
								calleeMethodSignatureMapCallerMethodSignatures.put(callee.getSignature(), new HashSet<String>());
							calleeMethodSignatureMapCallerMethodSignatures.get(callee.getSignature()).add(sootMethod.getSignature());
							callerMethodSignatureMapCalleeMethodSignatures.get(sootMethod.getSignature()).add(callee.getSignature());
						}
						
					}
				}
				
			}
			
			
		}
		
		insert();
	}
	
	private static void insert(){
		Common.database.executeUpdate(
				"CREATE TABLE IF NOT EXISTS " + "MethodNameMapMethodSignatures" + " (" + "ID				INTEGER  PRIMARY KEY AUTOINCREMENT,"
						+ "MethodName     TEXT," 
						+ "MethodSignatures		TEXT" 
						+ ");");
		for(String name : methodNameMapMethodSignatures.keySet()){
			String value=StringUtil.sqlString(name)+","+
						StringUtil.sqlString(StringUtil.join(methodNameMapMethodSignatures.get(name),"\\|"));
			Common.database.executeUpdate(
					"INSERT INTO " + "MethodNameMapMethodSignatures" + " ("
							+ "MethodName,MethodSignatures)" 
							+ "VALUES (" + value + ");"
					);
		}
		
	}

}
