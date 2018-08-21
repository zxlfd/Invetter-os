package module.inputvalidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import cfg.CFG;
import cfg.termination.TerminationType;
import dataflow.ClassAnalyzer;
import dataflow.DataFlowAnalyzer;
import main.Common;
import main.Memory;
import module.inputvalidation.concurrent.SearchAllInputValidation;
import module.inputvalidation.validationtype.Check;
import module.inputvalidation.validationtype.Enforce;
import module.inputvalidation.validationtype.ValidationType;
import module.stubservice.SearchAdditionalStubService;
import module.stubservice.SearchServiceInServiceManager;
import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.coffi.method_info;
import soot.jimple.Stmt;
import soot.jimple.internal.JIfStmt;
import util.LogUtil;
import util.StringUtil;


public class SearchInputValidation {
	
	
	public void run(boolean needInsert){
		if(needInsert){
			List<String> publicMethodSignatureList=new ArrayList<>();
			publicMethodSignatureList.addAll(SearchServiceInServiceManager.publicMethodSignatureList);
			publicMethodSignatureList.addAll(SearchAdditionalStubService.publicMethodSignatureList);
			
			createTableInDatabase();
			
			SearchAllInputValidation allInputValidation=new SearchAllInputValidation(publicMethodSignatureList, 48);
			HashMap<SootMethod, List<ValidationType>> result=allInputValidation.start();
			
			LogUtil.debug("SearchInputValidation", "Finish with Size : "+result.size());
			for(Entry<SootMethod, List<ValidationType>> entry: result.entrySet()) {
				insertIntoTableOfInputValidationInMethod(entry.getValue());
			}
		}
	}
	
	
	
	public static List<ValidationType> searchValidationForInputInMethod(CFG cfg,int argNumOfInput){
		List<ValidationType> result=new ArrayList<>();
		Unit sourceUnit = null;
		ValueBox sourceValueBox = null;
		if(cfg==null) return result;
		SootMethod method=cfg.method;
		if(!method.isConcrete() || !ClassAnalyzer.isValidMethod(method)) return result;
		
		
		if(Memory.sootMethodSignatureAndArgsNumMapValidationTypes.containsKey(method.getSignature()+" : "+argNumOfInput))
			return Memory.sootMethodSignatureAndArgsNumMapValidationTypes.get(method.getSignature()+" : "+argNumOfInput);
		
		Body body=method.retrieveActiveBody();
		if(argNumOfInput==0){
			for(Unit unit:body.getUnits()){
				if(unit.toString().contains("@this")){
					sourceUnit=unit;
					sourceValueBox=unit.getDefBoxes().get(0);
					break;
				}
			}
		}else if(argNumOfInput>0){
			String param="@parameter"+(argNumOfInput-1);
			for(Unit unit:body.getUnits()){
				if(unit.toString().contains(param)){
					sourceUnit=unit;
					sourceValueBox=unit.getDefBoxes().get(0);
					break;
				}
			}
		}else{
			LogUtil.error("SearchInputValidation", "Wrong argNumOfInput : "+argNumOfInput);
			return result;
		}
		if(sourceUnit==null || sourceValueBox==null){
			LogUtil.debug("SearchInputValidation", "No sourceUnit : "+method.getSignature()+" : "+argNumOfInput);
			return result;
		}
		
		HashMap<Unit, ValueBox> allUsedUnitAndValueBox=DataFlowAnalyzer.intraProcedural_findAllUseUnitsAndValueBoxes(body, sourceUnit, sourceValueBox);
		
		Set<Unit> unitsUsedByIfStmt=new HashSet<>();
		
		for(Unit unit:allUsedUnitAndValueBox.keySet()){
			if(unit instanceof JIfStmt){
				Check check=new Check(cfg, unit, argNumOfInput,method.getSignature());
				result.add(check);
				unitsUsedByIfStmt.addAll(check.leftTaintedUnits);
				unitsUsedByIfStmt.addAll(check.rightTaintedUnits);
			}
		}
		for (Entry<Unit, ValueBox> entry : allUsedUnitAndValueBox.entrySet()) {
			Unit unit = entry.getKey();
			ValueBox valueBox = entry.getValue();
			if(((Stmt)unit).containsInvokeExpr() && !unitsUsedByIfStmt.contains(unit)){
				int argNumOfCallee=ClassAnalyzer.getArgNumOfValueBoxInUnit(unit, valueBox);
				result.add(new Enforce(unit,argNumOfInput,argNumOfCallee,method.getSignature()));
			}
		}
		
		Memory.sootMethodSignatureAndArgsNumMapValidationTypes.put(method.getSignature()+" : "+argNumOfInput,result);
		return result;
	}
	
	protected void createTableInDatabase(){
		String tableName="InputValidationInMethodCheck";
		Common.database.executeUpdate(
				"CREATE TABLE IF NOT EXISTS " + tableName + " (" + "ID				INTEGER  PRIMARY KEY AUTOINCREMENT,"
						+ "CurrentMethod		TEXT," + "CurrentArgsNum     TEXT,"+ "CurrentUnit		TEXT,"
						+ "LeftTaintedUnits	  TEXT,"+"RightTaintedUnits    TEXT,"+"SecurityType TEXT,"+"TerminationType TEXT"+
						");");
		tableName="InputValidationInMethodEnforce";
		Common.database.executeUpdate(
				"CREATE TABLE IF NOT EXISTS " + tableName + " (" + "ID				INTEGER  PRIMARY KEY AUTOINCREMENT,"
						+ "CurrentMethod		TEXT," + "CurrentArgsNum     TEXT,"+ "CurrentUnit		TEXT,"+ "CalleeArgsNum		TEXT,"
						+"SecurityType TEXT"+
						");");
		
	}
	
	protected void insertIntoTableOfInputValidationInMethod(List<ValidationType>validations){
		LogUtil.debug("InsertInputValidation","Size : "+validations.size());
		if(validations.size()>0){
			LogUtil.debug("InsertInputValidation","Method : "+validations.get(0).signatureOfCurrentMethod+" Arg : "+validations.get(0).argsNumOfInput);
		}

		for(ValidationType validation:validations){
			if(validation instanceof Enforce){
				String value=
						StringUtil.sqlString(validation.signatureOfCurrentMethod)+","
						+StringUtil.sqlString(validation.argsNumOfInput+"")+","
						+StringUtil.sqlString(validation.unit.toString())+","
						+StringUtil.sqlString(((Enforce)validation).argsNumOfCallee+"")+","
						+StringUtil.sqlString("NULL");
				Common.database.executeUpdate(
						"INSERT INTO " + "InputValidationInMethodEnforce" + " ("
								+ "CurrentMethod,CurrentArgsNum,CurrentUnit,CalleeArgsNum,SecurityType)" 
								+ "VALUES (" + value + ");"
						);
				
				
			}else{
				String leftTaintedUnits="";
				for(Unit unit:((Check)validation).leftTaintedUnits){
					leftTaintedUnits+=(unit.toString()+"|");
				}
				String rightTaintedUnits="";
				for(Unit unit:((Check)validation).rightTaintedUnits){
					rightTaintedUnits+=(unit.toString()+"|");
				}
				
				LogUtil.debug("TerminationType", validation.signatureOfCurrentMethod+" : "+validation.argsNumOfInput);
				
				TerminationType type=((Check)validation).getTerminationType();
				
				LogUtil.debug("TerminationType", type.getClass().getName());
				
				
				
				String value=
						StringUtil.sqlString(validation.signatureOfCurrentMethod)+","
						+StringUtil.sqlString(validation.argsNumOfInput+"")+","
						+StringUtil.sqlString(validation.unit.toString())+","
						+StringUtil.sqlString(leftTaintedUnits)+","
						+StringUtil.sqlString(rightTaintedUnits)+","
						+StringUtil.sqlString(((Check)validation).getSecurityCheckType().toString())+","
						+StringUtil.sqlString(((Check)validation).getTerminationType().toString());
					Common.database.executeUpdate(
							"INSERT INTO " + "InputValidationInMethodCheck" + " ("
									+ "CurrentMethod,CurrentArgsNum,CurrentUnit,LeftTaintedUnits,RightTaintedUnits,SecurityType,TerminationType)" 
									+ "VALUES (" + value + ");"
							);
			}
			
		}
	}
	

}
