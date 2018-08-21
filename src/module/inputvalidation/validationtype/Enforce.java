package module.inputvalidation.validationtype;

import java.util.ArrayList;
import java.util.List;

import cfg.CFG;
import dataflow.ClassAnalyzer;
import module.inputvalidation.SearchInputValidation;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

public class Enforce extends ValidationType{
	
	public int argsNumOfCallee;
	
	
	public Enforce(Unit unit,int argsNumOfInput,int argsNumOfCallee, String currentMethod){
		this.unit=unit;
		this.argsNumOfCallee=argsNumOfCallee;
		this.argsNumOfInput=argsNumOfInput;
		this.originalArgsNum=argsNumOfInput;
		originalUnitInEntryMethod=unit;
		signatureOfCurrentMethod=currentMethod;
	}
	
	
	public List<ValidationType> transform(){
		List<ValidationType> result=new ArrayList<>();
		if(((Stmt)unit).containsInvokeExpr()){
			SootMethod method=((Stmt)unit).getInvokeExpr().getMethod();
			if(!method.isConcrete() || !ClassAnalyzer.isValidMethod(method)) return result;
			for(ValidationType validation:SearchInputValidation.searchValidationForInputInMethod(CFG.getCFG(method), argsNumOfCallee)){
				validation.originalUnitInEntryMethod=originalUnitInEntryMethod;
				validation.originalArgsNum=originalArgsNum;
				result.add(validation);
			}
		}
		return result;
	}

}
