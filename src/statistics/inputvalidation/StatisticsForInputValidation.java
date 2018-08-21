package statistics.inputvalidation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cfg.termination.TerminationType;
import main.Common;
import module.securitycheckmethod.SearchSecurityCheckType;
import util.LogUtil;
import util.StringUtil;

public class StatisticsForInputValidation {
	
	
	
	public static HashSet<MethodSignatureAndArgNum> publicEntrySignatureMapArgNumInInputValidation = new HashSet<MethodSignatureAndArgNum>();
	
	public static HashSet<MethodSignatureAndArgNum> publicEntrySignatureMapArgNumInInputValidationNotTaintedByBinder = new HashSet<MethodSignatureAndArgNum>();
	
	
	public static HashSet<MethodSignatureAndArgNum> methodAndArgs=new HashSet<>();
	
	
	public void run() {
		initFromDatabase();
		LogUtil.debug("StatisticsForInputValidation", "PublicEntry :"
				+methodAndArgs.size());
		
		for(MethodSignatureAndArgNum methodAndArg : methodAndArgs){
			int argNum=methodAndArg.argNum;
			String signature=methodAndArg.signature;
			if(isSecurityCheck(signature, argNum, new HashMap<String, Integer>())){
				publicEntrySignatureMapArgNumInInputValidation.add(methodAndArg);
			}
			if(isSecurityCheckNotTaintedByBinder(signature, argNum,  new HashMap<String, Integer>())){
				publicEntrySignatureMapArgNumInInputValidationNotTaintedByBinder.add(methodAndArg);
			}
		}
		LogUtil.debug("StatisticsForInputValidation","InputValidation : "+publicEntrySignatureMapArgNumInInputValidation.size());
		LogUtil.debug("StatisticsForInputValidation","InputValidationNotTaintedByBinder : "+publicEntrySignatureMapArgNumInInputValidation.size());
	}
	
	
	private void initFromDatabase(){
		try {
			ResultSet 
			resultSet=Common.database.select(
					"Select * from PublicMethodsInStubService"
					);
			while(resultSet.next()){
				String signature=resultSet.getString("Signature");
				String[] params=signature.split("\\(")[1].split("\\)")[0].split(",");
				int index=0;
				for(String param : params){
					index++;
					methodAndArgs.add(new MethodSignatureAndArgNum(signature, index));
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
					methodAndArgs.add(new MethodSignatureAndArgNum(signature, index));
				}
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public static boolean isSecurityCheck(String signature,int argNum,HashMap<String, Integer> history){
		if(history.containsKey(signature) && history.get(signature)==argNum)
			return false;
		history.put(signature, argNum);
		try {
		//	LogUtil.debug("Sql", "Select * from InputValidationInMethodCheck where CurrentMethod="+StringUtil.sqlString(signature)+" and CurrentArgsNum="+argNum+";");
			ResultSet resultSet=Common.database.select(
					"Select * from InputValidationInMethodCheck where CurrentMethod="+StringUtil.sqlString(signature)+" and CurrentArgsNum="+argNum+";"
					);
			
			while(resultSet.next()){
				String terminationType=resultSet.getString("TerminationType");
				if(!terminationType.equals(TerminationType.TerminationNot))
					return true;
			}
			
			resultSet=Common.database.select(
					"Select * from InputValidationInMethodEnforce where CurrentMethod="+StringUtil.sqlString(signature)+" and CurrentArgsNum="+argNum+";"
					);
			while(resultSet.next()){
				String unit=resultSet.getString("CurrentUnit");
				String calleeSig=unit.substring(
						unit.indexOf("<"), 
						unit.indexOf(">")+1
						);
				int calleeArgs=resultSet.getInt("CalleeArgsNum");
				HashMap<String, Integer> subHistory=new HashMap<>(history);
				if(isSecurityCheck(calleeSig, calleeArgs, subHistory)){
					return true;
				}
					
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
	
	return false;
}
public static boolean isSecurityCheckNotTaintedByBinder(String signature,int argNum,HashMap<String, Integer> history){
	if(history.containsKey(signature) && history.get(signature)==argNum)
		return false;
	history.put(signature, argNum);
	try {
	//	LogUtil.debug("Sql", "Select * from InputValidationInMethodCheck where CurrentMethod="+StringUtil.sqlString(signature)+" and CurrentArgsNum="+argNum+";");
		ResultSet resultSet=Common.database.select(
				"Select * from InputValidationInMethodCheck where CurrentMethod="+StringUtil.sqlString(signature)+" and CurrentArgsNum="+argNum+";"
				);
		
		while(resultSet.next()){
			String terminationType=resultSet.getString("TerminationType");
			if(!terminationType.equals(TerminationType.TerminationNot)){
				List<String> taintedSigs=new ArrayList<>();
				String[] leftUnits=resultSet.getString("LeftTaintedUnits").split("\\|");
				String[] rightUnits=resultSet.getString("RightTaintedUnits").split("\\|");
				
				for(String unit : leftUnits){
					if(!(unit.contains("<") && unit.contains(">")))
						continue;
					String calleeSig=unit.substring(
							unit.indexOf("<"), 
							unit.indexOf(">")+1
							);
				//	LogUtil.debug("CalleeSig", calleeSig);
					taintedSigs.add(calleeSig);
				}
				for(String unit : rightUnits){
					if(!(unit.contains("<") && unit.contains(">")))
						continue;
					String calleeSig=unit.substring(
							unit.indexOf("<"), 
							unit.indexOf(">")+1
							);
				//	LogUtil.debug("CalleeSig", calleeSig);
					taintedSigs.add(calleeSig);
				}
				for(String sig : taintedSigs){
					if(SearchSecurityCheckType.getSecurityCheckType(sig).getLevel()>1)
						return false;
				}
				return true;
			}
				
		}
		
		resultSet=Common.database.select(
				"Select * from InputValidationInMethodEnforce where CurrentMethod="+StringUtil.sqlString(signature)+" and CurrentArgsNum="+argNum+";"
				);
		while(resultSet.next()){
			String unit=resultSet.getString("CurrentUnit");
			String calleeSig=unit.substring(
					unit.indexOf("<"), 
					unit.indexOf(">")+1
					);
			int calleeArgs=resultSet.getInt("CalleeArgsNum");
			HashMap<String, Integer> subHistory=new HashMap<>(history);
			if(isSecurityCheck(calleeSig, calleeArgs, subHistory))
				return true;
		}
		
	} catch (SQLException e) {
		e.printStackTrace();
	}


return false;
}
	

}
