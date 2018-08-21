package util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;



public class StringUtil {
	
	public static String[] getArgsFromSignature(String methodSignature){
		String[] result = null;
		if(methodSignature.indexOf(")") - methodSignature.indexOf("(") > 1){
			String argsString = methodSignature.substring(methodSignature.indexOf("(")+1,methodSignature.lastIndexOf(")"));
			result = argsString.split(",");
		}
		return result;
	}
	
	public static String getMethodNameFromSignature(String methodSignature){
		return methodSignature.substring(methodSignature.lastIndexOf(" ")+1, methodSignature.lastIndexOf("("));
	}
	
	public static String getClassNameFromSignature(String methodSignature){
		return methodSignature.substring(1, methodSignature.indexOf(":"));
	}
	
	
	public static String join(String[] strings, String delimiter){
		if(strings == null){
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		for(String string : strings){
			builder.append(string);
			builder.append(delimiter);
		}
		return builder.toString();
	}
	public static String join(HashSet<String> strings, String delimiter) {
		if(strings == null){
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		for(String string : strings){
			builder.append(string);
			builder.append(delimiter);
		}
		return builder.toString();
	}
	
	
	public static String join(List<String> strings, String delimiter){
		return join(strings.toArray(new String[strings.size()]), delimiter);
	}
	
	public static boolean isEmpty(String str){
		return str == null || str.length() <= 0;
	}
	
	public static String sqlString(String string){
		return "'"+string.replace("\\", "").replace("'", "")+"'";
	}
	
//	public static String conditionFormater(String type, BackTrackResult left, BackTrackResult right){
//		StringBuilder stringBuilder = new StringBuilder();
//		stringBuilder.append(type+" :\n");
//		stringBuilder.append("left Box :");
//		stringBuilder.append(left.toString()+"\n");
//		stringBuilder.append("right Box :");
//		stringBuilder.append(right.toString()+"");
//		
//		return stringBuilder.toString();
//	}
//	public static String conditionFormater(String type, List<CallCondition> conditions) {
//		StringBuilder stringBuilder = new StringBuilder();
//		stringBuilder.append(type+" :\n");
//		for(CallCondition callCondition: conditions){
//			stringBuilder.append(callCondition);
//		}
//		stringBuilder.append("\n");
//		return  stringBuilder.toString();
//	}
	
	
//	public static String conditionString(CallCondition condition){
//		String string= null;
//		if (condition instanceof GreaterCallCondition) {			
//			string = conditionFormater("Greater", 
//					((GreaterCallCondition) condition).getLeftResultBox(),  
//					((GreaterCallCondition) condition).getRightResultBox());
//		}else if (condition instanceof LessCallCondition) {			
//			string = conditionFormater("Less", 
//					((LessCallCondition) condition).getLeftResultBox(),  
//					((LessCallCondition) condition).getRightResultBox());
//		} else if (condition instanceof EqualCallCondition) {			
//			string = conditionFormater("Equal", 
//					((EqualCallCondition) condition).getLeftResultBox(),  
//					((EqualCallCondition) condition).getRightResultBox());
//		} else if (condition instanceof NotCallCondition) {			
//			string = "Not :\n"+condition.toString();
//		} else if (condition instanceof OrCallCondition) {			
//			string = conditionFormater("Or", ((OrCallCondition) condition).getConditions());
//		} else if (condition instanceof AndCallCondition) {			
//			string = conditionFormater("And", ((AndCallCondition) condition).getConditions());
//		} else if (condition instanceof TrueCallCondition){
//			string = "TrueCallCondition";
//		} else if (condition instanceof FalseCallCondition){
//			string = "FalseCallCondition";
//		} 
//		return "{"+string+"}";
//
//	}
	
	public static String mapString(Map<String, String> map){
		StringBuilder stringBuilder = new StringBuilder();
		for(Map.Entry<String, String> entry : map.entrySet()){
			stringBuilder.append(entry.getValue() + "->\n" + entry.getKey()+"\n\n");
		}
		return stringBuilder.toString();
	}

	
}

