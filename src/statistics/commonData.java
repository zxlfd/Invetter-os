package statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
//hyy
public class commonData {
	public static Map<String,ArrayList<String>> securiyCheckTable = new HashMap<>();
	public static Set<String> publicMethods = new HashSet<>();
	public static Map<String,Set<String>> callerSigMapCalleeSig = new HashMap<>();
	public static Map<String,Set<String>> calleeSigMapCallerSig = new HashMap<>();
	public static Set<String> permissioncheckInPublicMethods = new HashSet<>();
}
