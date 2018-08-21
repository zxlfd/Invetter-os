package statistics.inputvalidation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cfg.termination.TerminationType;
import dataflow.ClassAnalyzer;
import main.Common;
import module.securitycheckmethod.SearchSecurityCheckType;
import util.LogUtil;
import util.StringUtil;

public class StatisticsForSensitiveInputValidation {
	
	
	private static  Set<String> sensitiveParcelable=new HashSet<>();
	private static HashSet<MethodSignatureAndArgNum> publicEntrySignatureMapSenstiveArgNum=new HashSet<MethodSignatureAndArgNum>();
	
	public static HashSet<MethodSignatureAndArgNum> publicEntrySignatureMapSensitiveArgNumInInputValidation=new HashSet<MethodSignatureAndArgNum>();
	public static HashSet<MethodSignatureAndArgNum> publicEntrySignatureMapSensitiveArgNumInInputValidationNotTaintedByBinder=new HashSet<MethodSignatureAndArgNum>();
	
	public void run() {
		initFromDatabase();
		LogUtil.debug("StatisticsForInputValidation", "SensitiveParcelable : "+sensitiveParcelable.size()+" : PublicEntry :"
				+publicEntrySignatureMapSenstiveArgNum.size());
		
		for(MethodSignatureAndArgNum method : publicEntrySignatureMapSenstiveArgNum){
			int argNum=method.argNum;
			String signature=method.signature;
			if(StatisticsForInputValidation.isSecurityCheck(signature, argNum, new HashMap<String, Integer>())){
				publicEntrySignatureMapSensitiveArgNumInInputValidation.add(new MethodSignatureAndArgNum(signature, argNum));
			}
			if(StatisticsForInputValidation.isSecurityCheckNotTaintedByBinder(signature, argNum,  new HashMap<String, Integer>())){
				publicEntrySignatureMapSensitiveArgNumInInputValidationNotTaintedByBinder.add(new MethodSignatureAndArgNum(signature, argNum));
			}
		}
		LogUtil.debug("StatisticsForInputValidation","SensitiveInputValidation : "+publicEntrySignatureMapSensitiveArgNumInInputValidation.size());
		LogUtil.debug("StatisticsForInputValidation","SensitiveInputValidationNotTaintedByBinder : "+publicEntrySignatureMapSensitiveArgNumInInputValidationNotTaintedByBinder.size());
		Set<String> signatures=new HashSet<>();
		
		for(MethodSignatureAndArgNum method : publicEntrySignatureMapSensitiveArgNumInInputValidationNotTaintedByBinder)
			signatures.add(method.signature);
		for(String method : signatures)
			System.out.println(method);
		
	}
	
	
	private void initFromDatabase(){
		try {
			ResultSet resultSet=Common.database.select(
				"Select * from ParcelableClass"
				);
		
			while(resultSet.next()){
				String className=resultSet.getString("CLASSNAME");
				int sensitive=resultSet.getInt("RANKING");
				if(sensitive>0)
					sensitiveParcelable.add(className);
			}
			
			resultSet=Common.database.select(
					"Select * from PublicMethodsInStubService"
					);
			while(resultSet.next()){
				String signature=resultSet.getString("Signature");
				String[] params=signature.split("\\(")[1].split("\\)")[0].split(",");
				int index=0;
				for(String param : params){
					index++;
					if(sensitiveParcelable.contains(param.replace("[]", "")))
						publicEntrySignatureMapSenstiveArgNum.add(new MethodSignatureAndArgNum(signature, index));
				}
			}
			
			resultSet=Common.database.select(
					"Select * from AdditionalPublicMethods"
					);
			while(resultSet.next()){
				String signature=resultSet.getString("Signature");
				String[] params=signature.split("\\(")[1].split("\\)")[0].split(",");
				int index=0;
				for(String param : params){
					index++;
					if(sensitiveParcelable.contains(param.replace("[]", "")))
						publicEntrySignatureMapSenstiveArgNum.add(new MethodSignatureAndArgNum(signature, index));
				}
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

}
