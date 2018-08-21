package module.inputcluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;


import main.Common;
import main.Memory;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import util.LogUtil;
import util.StringUtil;


public class SearchAllInputVariables {
	
	
	public void run(){
		HashSet<String> inputFields=new HashSet<>();
		HashSet<String> inputParcel=new HashSet<>();
		String allSignatureOfPublicMethods=getAllSignatureForPublicMethodsFromStubService();
		try {
			ResultSet resultSet=Common.database.select(
					"select * from ParcelableClass"
					);
			while(resultSet.next()){
				String parcelClassName=resultSet.getString("CLASSNAME");
				if(allSignatureOfPublicMethods.contains(parcelClassName))
					inputParcel.add(parcelClassName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		HashSet<String> writeToParcels=Memory.methodNameMapMethodSignatures.get("writeToParcel");
		for(String signature : writeToParcels){
			SootMethod writeToParcel=Memory.methodSignatureMapSootMethod.get(signature);
			if(writeToParcel.isConcrete()){
				for(Unit unit : writeToParcel.retrieveActiveBody().getUnits()){
					if(((Stmt)unit).containsFieldRef()){
						SootField field=((Stmt)unit).getFieldRef().getField();
						if(field.getDeclaringClass().equals(writeToParcel.getDeclaringClass()))
							inputFields.add(field.getSignature());
					}
				}
			}
		}
		
		LogUtil.debug("InputFields", "Size of InputFields : "+inputFields.size());
		
		String tableName="InputFields";
		Common.database.executeUpdate(
				"CREATE TABLE IF NOT EXISTS " + tableName + " (" + "ID				INTEGER  PRIMARY KEY AUTOINCREMENT,"
						+ "Signature		TEXT" 
						+ ");");
		for(String field:inputFields){
			String value=StringUtil.sqlString(field);
			Common.database.executeUpdate("INSERT INTO " + tableName + " (Signature)" + "VALUES (" + value + ");");
		}
		LogUtil.debug("InputFields", "Finished.");
	}
	
	
	private String getAllSignatureForPublicMethodsFromStubService(){
		String result="";
		try {
			ResultSet resultSet=Common.database.select(
					"select * from AdditionalPublicMethods"
					);
			while(resultSet.next()){
				result+=(resultSet.getString("Signature")+";");
			}
			resultSet=Common.database.select(
					"select * from PublicMethodsInStubService"
					);
			while(resultSet.next()){
				result+=(resultSet.getString("Signature")+";");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

}
