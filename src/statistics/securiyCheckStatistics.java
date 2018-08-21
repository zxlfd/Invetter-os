package statistics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


//hyy
public class securiyCheckStatistics {

	public Set<String> getCheckMethods() {
		Set<String> securityCheckMethods = new HashSet<>();
		for (String checktype : statistics.commonData.securiyCheckTable.keySet()) {
			ArrayList<String> aList = statistics.commonData.securiyCheckTable.get(checktype);
			securityCheckMethods.addAll(aList);
		}
		return securityCheckMethods;
	}

	public Set<String> getCheckClass(Set<String> checkMethods) {
		Set<String> securityCheckClass = new HashSet<>();
		for (String signature : checkMethods) {
			String methodclass = signature.substring(1, signature.indexOf(":"));
			if (methodclass.contains("$")) {
				methodclass = methodclass.substring(0, methodclass.indexOf("$"));
			}
			securityCheckClass.add(methodclass);
		}
		return securityCheckClass;
	}
	
	
	public Set<String> getCheckPublicMethods(Set<String> securityCheckMethods) {
		Set<String> permissioncheckInPublicMethods=new HashSet<>();
		for(String securityCheckMethod:securityCheckMethods) {
			Set<String> checkPublicMethod=getCheckPublicMethod(securityCheckMethod);
			if(checkPublicMethod!=null) {
				permissioncheckInPublicMethods.addAll(checkPublicMethod);
			};
		}
		return permissioncheckInPublicMethods;
		//System.out.println("permissioncheckInPublicMethodsNum:"+statistics.commonData.permissioncheckInPublicMethods.size());	
	}
	
	
	
	
	
	
	private  Set<String> getCheckPublicMethod(String publicMethodSig) {
		//String publicMethodSig = "<com.android.server.wallpaper.WallpaperManagerService: void setWallpaperComponentChecked(android.content.ComponentName,java.lang.String,int)>";
		Set<String> permissioncheckInPublicMethods=new HashSet<>();
		Set<String> source = statistics.commonData.calleeSigMapCallerSig.get(publicMethodSig);		
		if(source!=null) {
			Set<String> processed = new HashSet<>();
			Queue<String> waitForProcessed = new LinkedList<>();		
			waitForProcessed.addAll(source);
			while(!waitForProcessed.isEmpty()) {
				String callee=waitForProcessed.poll();
				if(processed.contains(callee)) {
					continue;
				}
				processed.add(callee);
				if(statistics.commonData.publicMethods.contains(callee)) {
					permissioncheckInPublicMethods.add(callee);
				}
				Set<String> callers=statistics.commonData.calleeSigMapCallerSig.get(callee);
				if(callers!=null) {
					waitForProcessed.addAll(callers);			
				}
			}			
		}
		return permissioncheckInPublicMethods;
		//else System.out.println("null");		
	}
	
	
	
	
	
	
	
	
}
