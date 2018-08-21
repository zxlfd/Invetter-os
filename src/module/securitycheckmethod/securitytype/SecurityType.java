package module.securitycheckmethod.securitytype;

import java.util.HashMap;

public class SecurityType {
	
	public static String TAG="SecurityType";
	private int level;
	private String type;
	
	public static final String TypeDefault="TypeDefault";
	public static final String TypePermissionCheck="TypePermissionCheck";
	public static final String TypePermissionEnforce="TypePermissionEnforce";
	public static final String TypePid="TypePid";
	public static final String TypeUid="TypeUid";
	public static final String TypeUserId="TypeUserId";
	
	public static final HashMap<String, Integer> securityTypes=new HashMap<>();
	
	static{
		securityTypes.put(TypeDefault, 0);
		securityTypes.put(TypeUserId, 1);
		securityTypes.put(TypePermissionEnforce, 2);
		securityTypes.put(TypePermissionCheck, 3);
		securityTypes.put(TypeUid, 4);
		securityTypes.put(TypePid, 5);
	}
	
	public SecurityType(String type){
		if(securityTypes.keySet().contains(type))
			this.type=type;
		else
			this.type=TypeDefault;
		
		level=securityTypes.get(this.type);
	}
	
	public int getLevel(){
		return level;
	}
	
	public String toString(){
		return type;
	}

}
