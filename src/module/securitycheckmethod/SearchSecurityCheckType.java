package module.securitycheckmethod;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import main.Common;
import module.securitycheckmethod.concurrent.SearchAllSecurityCheckMethod;
import module.securitycheckmethod.securitytype.SecurityType;
import util.LogUtil;
import util.StringUtil;

public class SearchSecurityCheckType {
	
	
	public static String Type_Default=SecurityType.TypeDefault;
	public static String Type_Permission_enforce=SecurityType.TypePermissionEnforce;
	public static String Type_Permission_check=SecurityType.TypePermissionCheck;
	public static String Type_Uid=SecurityType.TypeUid;
	public static String Type_Pid=SecurityType.TypePid;
	public static String Type_UserId=SecurityType.TypeUserId;
	
	
	private static List<String> Uid_get_methods_As_seed = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("getCallingUid");
			add("myUid");
			add("getUid");
			add("binderGetCallingUid");
		}

	};
	private static List<String> Pid_get_methods_As_seed = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("getCallingPid");
			add("myPid");
			add("binderGetCallingPid");
			add("getPid");
		}

	};
	
	private static List<String> Permission_check_methods_As_seed = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("checkCallingOrSelfPermission"); // check* 只在权限验证通过时返回Permission
													// Granted
			add("checkComponentPermission");
			add("checkCallingPermission");
			add("checkSelfPermission");
			add("checkUidPermission");
			add("checkUriPermission");
			add("checkPermission");
			add("hasPermission"); // 返回 是否Permission Granted
			add("noteOp"); // 返回是否Mode_Allowed

			add("hasWifiNetworkListenPermission");
		}
	};
	
	private static List<String> Permission_enforce_methods_As_seed = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("enforceCallingOrSelfPermission"); // enforce* 在权限验证不通过时会throw
													// SecurityException
			add("enforceCallingPermission");
			add("enforcePermission");
			add("enforceSystemUserOrPermission");
		
		}
	};
	
	private static List<String> UserId_get_methods_As_seed = new ArrayList<String>() {

		private static final long serialVersionUID = 1L;
		{
			add("getUserId");
			add("handleIncomingUser");
			add("resolveUserId");
			add("getGroupParentLocked");
			add("resolveProfileParentLocked");
			add("getEffectiveUserId");
			add("resolveCallingUserEnforcingPermissions");
			add("resolveCallingProfileParentLocked");
			add("resolveProfileParent");
			add("getCurrentUser");
			add("getCallingUserId");
		}
	};
	
	public void run(boolean needInsert) {
		if(needInsert){
			SearchAllSecurityCheckMethod searchAllSecurityCheckMethod=new SearchAllSecurityCheckMethod(5);
			searchAllSecurityCheckMethod.add(Type_Permission_check, Permission_check_methods_As_seed);
			searchAllSecurityCheckMethod.add(Type_Permission_enforce, Permission_enforce_methods_As_seed);
			searchAllSecurityCheckMethod.add(Type_Pid, Pid_get_methods_As_seed);
			searchAllSecurityCheckMethod.add(Type_Uid, Uid_get_methods_As_seed);
			searchAllSecurityCheckMethod.add(Type_UserId, UserId_get_methods_As_seed);
			HashMap< String, List<String>> result=searchAllSecurityCheckMethod.start();
			for(String type:result.keySet()){
				LogUtil.debug("SearchSecurityCheckType", "type : "+type+" : Size : "+result.get(type).size());
				if(type.equals(Type_Permission_check)){
					Permission_check_methods_As_seed=result.get(type);
				}
				else if(type.equals(Type_Permission_enforce)){
					Permission_enforce_methods_As_seed=result.get(type);
				}
				else if(type.equals(Type_Pid)){
					Pid_get_methods_As_seed=result.get(type);
				}
				else if(type.equals(Type_Uid)){
					Uid_get_methods_As_seed=result.get(type);
				}
				else if(type.equals(Type_UserId)){
					UserId_get_methods_As_seed=result.get(type);
				}
				else{
					LogUtil.error("SearchSecurityChecjType", "Wrong type from SearchAllSecurityCheckMethod : "+type);
				}
			}
			insertIntoDatabase();
			
		}
		else{
			initFromDatabase();
		}
	}
	
	
	
	
	public static SecurityType getSecurityCheckType(String signature){
			String methodSig=signature;
			if(Permission_enforce_methods_As_seed.contains(methodSig))
					return new SecurityType(SecurityType.TypePermissionEnforce);
			if(Permission_check_methods_As_seed.contains(methodSig)){
					return new SecurityType(SecurityType.TypePermissionCheck);
			}
			if(Uid_get_methods_As_seed.contains(methodSig)){
					return new SecurityType(SecurityType.TypeUid);
			}
			if(Pid_get_methods_As_seed.contains(methodSig)){
					return new SecurityType(SecurityType.TypePid);
			}
			if(UserId_get_methods_As_seed.contains(methodSig)){
					return new SecurityType(SecurityType.TypeUserId);
			}
		return new SecurityType(SecurityType.TypeDefault);
	}
	
	private void initFromDatabase(){
		
		
		try {
			ResultSet resultSet=Common.database.select(
					"select * from SecurityCheckMethods;"
					);
			
			while(resultSet.next()){
				String type=resultSet.getString("Type");
				String signature=resultSet.getString("MethodName");
				if(type.equals(Type_Permission_check))
					Permission_check_methods_As_seed.add(signature);
				if(type.equals(Type_Permission_enforce))
					Permission_enforce_methods_As_seed.add(signature);
				if(type.equals(Type_Pid))
					Pid_get_methods_As_seed.add(signature);
				if(type.equals(Type_Uid))
					Uid_get_methods_As_seed.add(signature);
				if(type.equals(Type_UserId))
					UserId_get_methods_As_seed.add(signature);
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LogUtil.debug("Permission_check_methods_As_seed", "Size is "+Permission_check_methods_As_seed.size());
		LogUtil.debug("Permission_enforce_methods_As_seed", "Size is "+Permission_enforce_methods_As_seed.size());
		LogUtil.debug("Pid_get_methods_As_seed", "Size is "+Pid_get_methods_As_seed.size());
		LogUtil.debug("Uid_get_methods_As_seed", "Size is "+Uid_get_methods_As_seed.size());
		LogUtil.debug("UserId_get_methods_As_seed", "Size is "+UserId_get_methods_As_seed.size());
	}


	private void insertIntoDatabase() {
		String tableName="SecurityCheckMethods";
		Common.database.executeUpdate(
				"CREATE TABLE IF NOT EXISTS " + tableName + " (" + "ID				INTEGER  PRIMARY KEY AUTOINCREMENT,"
						+ "Type		TEXT," 
						+ "MethodName     TEXT" 
						+ ");");
		
		LogUtil.debug("SearchSecurityCheckType", "Permission_enforce size : "+Permission_enforce_methods_As_seed.size());
		for(String methodName:Permission_enforce_methods_As_seed){
			String value = StringUtil.sqlString(SearchSecurityCheckType.Type_Permission_enforce) + ", "
					+ StringUtil.sqlString(methodName);
			Common.database.executeUpdate(
					"INSERT INTO " + tableName + " (Type,MethodName)" + "VALUES (" + value + ");");
		}
		LogUtil.debug("SearchSecurityCheckType", "Permission_Check size : "+Permission_check_methods_As_seed.size());
		for(String methodName:Permission_check_methods_As_seed){
			String value = StringUtil.sqlString(SearchSecurityCheckType.Type_Permission_check) + ", "
					+ StringUtil.sqlString(methodName);
			Common.database.executeUpdate(
					"INSERT INTO " + tableName + " (Type,MethodName)" + "VALUES (" + value + ");");
		}
		LogUtil.debug("SearchSecurityCheckType", "Uid size : "+Uid_get_methods_As_seed.size());
		for(String methodName:Uid_get_methods_As_seed){
			String value = StringUtil.sqlString(SearchSecurityCheckType.Type_Uid) + ", "
					+ StringUtil.sqlString(methodName);
			Common.database.executeUpdate(
					"INSERT INTO " + tableName + " (Type,MethodName)" + "VALUES (" + value + ");");
		}
		LogUtil.debug("SearchSecurityCheckType", "Pid size : "+Pid_get_methods_As_seed.size());
		for(String methodName:Pid_get_methods_As_seed){
			String value = StringUtil.sqlString(SearchSecurityCheckType.Type_Pid) + ", "
					+ StringUtil.sqlString(methodName);
			Common.database.executeUpdate(
					"INSERT INTO " + tableName + " (Type,MethodName)" + "VALUES (" + value + ");");
		}
		LogUtil.debug("SearchSecurityCheckType", "UserId : "+UserId_get_methods_As_seed.size());
		for(String methodName:UserId_get_methods_As_seed){
			String value = StringUtil.sqlString(SearchSecurityCheckType.Type_UserId) + ", "
					+ StringUtil.sqlString(methodName);
			Common.database.executeUpdate(
					"INSERT INTO " + tableName + " (Type,MethodName)" + "VALUES (" + value + ");");
		}
		
	}
	
	
	
	
}
