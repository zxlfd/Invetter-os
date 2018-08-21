package dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cfg.SearchSwappedClass;
import database.SwappedClassAndMethod;
import main.Common;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JRetStmt;
import soot.jimple.internal.JReturnStmt;
import util.LogUtil;

public class ClassAnalyzer {

	
	public  ClassAnalyzer() {
	};
	
	public static int getArgNumOfValueBoxInUnit(Unit unit,ValueBox valueBox){
		if(!((Stmt)unit).containsInvokeExpr()){
			LogUtil.error("ClassAnalyzer", "Unit does not have a method invoke : "+unit);
			return -1;
		}
		int index=unit.getUseBoxes().indexOf(valueBox);
		if(index<0){
			LogUtil.error("ClassAnalyzer", "Unit does not have the ValueBox : "+unit+" : "+valueBox);
		}
		if(index==((Stmt)unit).getInvokeExpr().getMethod().getParameterCount())
			index=0;
		else
			++index;
		return index;
	}
	
	public static boolean isValueUsedInUnit(Unit unit,Value value){
		List<String> usedValue=new ArrayList<>();
		for(ValueBox useBox:unit.getUseBoxes())
			usedValue.add(useBox.getValue().toString());
		return usedValue.contains(value.toString());
	}
	public static boolean isValueDefinedInUnit(Unit unit,Value value){
		List<String> definedValue=new ArrayList<>();
		for(ValueBox defBox:unit.getDefBoxes())
			definedValue.add(defBox.getValue().toString());
		return definedValue.contains(value.toString());
	}
	public static boolean isValueDefinedInUnit(Unit unit,String valueString){
		List<String> definedValue=new ArrayList<>();
		for(ValueBox defBox:unit.getDefBoxes())
			definedValue.add(defBox.getValue().toString());
		return definedValue.contains(valueString);
	}
	public static ValueBox findValueboxByValue(Unit unit,String valueString){
		for(ValueBox valueBox:unit.getUseAndDefBoxes())
			if(valueBox.getValue().toString().equals(valueString))
				return valueBox;
		return null;
	}
	public static ValueBox findValueboxByValue(Unit unit,Value value){
		for(ValueBox valueBox:unit.getUseAndDefBoxes())
			if(valueBox.getValue().toString().equals(value.toString()))
				return valueBox;
		return null;
	}
	
	public static boolean isUnitUsedInMethod(SootMethod method,Unit unit){
		if(!method.isConcrete())
			return false;
		
		for(Unit methodUnit:method.retrieveActiveBody().getUnits()){
			if(methodUnit.equals(unit))
				return true;
		}
		return false;
	}
	public static boolean isValidMethod(SootMethod method){
		return isValidMethodSignature(method.getSignature());
	}
	public static boolean isValidMethodSignature(String signature){
		for(String illegalMethisSigPattern : Common.illegalSignature){
			if(signature.matches(illegalMethisSigPattern)
					|| signature.equals(illegalMethisSigPattern))
				return false;
		}
		if(signature.toLowerCase().contains("huawei") || signature.toLowerCase().contains("samsung") || signature.toLowerCase().contains("miui") ){
			return true;
		}
		for(String valideMethodSigPattern : Common.validSignature)
			if(signature.matches(valideMethodSigPattern)){
				return true;
			}
		return false;
	}
	public static boolean isSwappedClassOrMethod(String signature){
		return SwappedClassAndMethod.contains(signature);
	}
	
//	public static boolean hasIllegalParcelableParam(String signature){
//		Pattern param=Pattern.compile("<.*\\((.*)\\)>");
//		Matcher matcher=param.matcher(signature);
//		if(matcher.matches()){
//			String[] params=matcher.group(1).split(",");
//			for(String parameter : params)
//				for(String illegalParam : common.illegalParcelableParam)
//					if(parameter.matches(illegalParam))
//						return false;
//		}
//		
//		return true;
//	}

	
	
//	public static boolean isPathReturnConstant(Path path){
//		Unit tail=path.getTail();
//		if(tail instanceof JReturnStmt && !tail.getUseBoxes().isEmpty()){
//			ValueBox valueBox=tail.getUseBoxes().get(0);
//			HashMap<Unit, ValueBox> defBoxes=(new DataFlowAnalyzer()).introProcedure_findAllDefUnitsAndValueBoxesInPath(path,tail,valueBox);
//			for(Unit key : defBoxes.keySet())
//				if(!isConstant(defBoxes.get(key)))
//					return false;
//			return true;
//		}
//		return false;
//	}
	public static boolean isValueBoxConstant(ValueBox valueBox){
		String value=valueBox.getValue().toString();
		if(value.matches("[0-9]*[.]?[0-9]*"))
			return true;
		if(value.matches("\".*\""))
			return true;
		if(value.matches("null"))
			return true;
		return false;
	}
	

	
	//// 
	public boolean isOnlyCalleeCanBeReturnedByCaller(SootMethod caller,String calleeSignature){
		
		String returnType=calleeSignature.split(" ")[1];
		if(!caller.isConcrete()|| !caller.getSignature().contains(" "+returnType+" ") ) return false;
		
		Body body=caller.retrieveActiveBody();
		if(body == null) {
			System.out.println("isOnlyCalleeCanBeReturnedByCaller caller body = null : "+ "caller "+caller.getSignature() );
			return false;
		}
		
		
		List<Unit> invokeCallee=new ArrayList<>();
		for(Unit unit : body.getUnits()){
			if( ((Stmt)unit).containsInvokeExpr()){
				if(((Stmt)unit).getInvokeExpr().getMethod().getSignature().equals(calleeSignature)){
					invokeCallee.add(unit);
				}
			}
		}
		if(returnType.equals("void")){
			if(invokeCallee.size()>0){
							return true;
			}
		}
		else{
			for(Unit sourceUnit : invokeCallee){
				if(sourceUnit.getDefBoxes().isEmpty())
					continue;
				HashMap<Unit, ValueBox> directUseUnitAndValueBox=DataFlowAnalyzer.introProcedural_findDirectUseUnitsAndValueBoxes(body, sourceUnit, sourceUnit.getDefBoxes().get(0));
				for(Unit sinkUnit : directUseUnitAndValueBox.keySet())
					if(sinkUnit instanceof JRetStmt || sinkUnit instanceof JReturnStmt){
						
						return true;
					}
						
			}
		}

		return false;
	}
	

	
}
