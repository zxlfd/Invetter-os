package statistics.securitycheck;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import main.Common;
import module.securitycheckmethod.SearchSecurityCheckType;
import soot.jimple.NewExpr;
import statistics.inputvalidation.MethodSignatureAndArgNum;
import util.LogUtil;

public class StatisticsForSecurityCheck {
	
	
	
	public HashSet<String> publicEntryHasPermission=new HashSet<>();
	
	
	
	public void run(){
		try {
			ResultSet 
			resultSet=Common.database.select(
					"Select * from PublicMethodsInStubService"
					);
			while(resultSet.next()){
				String signature=resultSet.getString("Signature");
				if(SearchSecurityCheckType.getSecurityCheckType(signature).getLevel()>1)
					publicEntryHasPermission.add(signature);
			}
			
			resultSet=Common.database.select(
					"Select * from AdditionalPublicMethods"
					);
			while(resultSet.next()){
				String signature=resultSet.getString("Signature");
				if(SearchSecurityCheckType.getSecurityCheckType(signature).getLevel()>1)
					publicEntryHasPermission.add(signature);
			}
			LogUtil.debug("StatisticsForSecurityCheck", "Permission : "+publicEntryHasPermission.size());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	

}
