package module.inputvalidation.validationtype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cfg.CFG;
import cfg.Node;
import cfg.Path;
import cfg.termination.TerminationType;
import dataflow.DataFlowAnalyzer;
import module.securitycheckmethod.SearchSecurityCheckType;
import module.securitycheckmethod.securitytype.SecurityType;
import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.jimple.internal.JIfStmt;
import util.LogUtil;

public class Check extends ValidationType{
	
	public List<Unit> leftTaintedUnits=new ArrayList<>();
	public List<Unit> rightTaintedUnits=new ArrayList<>();
	
	public static String STANDARD="";
	public static String Standard_JIf="JIf";         // The input is transformed into a JIf-Stmt.
	public static String Standard_Middle="Middle";   
	public static String Standard_TerminationBranch="TerminationBranch";       // The input is transformed into a JIf-Stmt, which can trigger a termination.
	
	private CFG cfg;
	public Check(CFG cfg, Unit unit,int argNum, String currentMethod){
		this.unit=unit;
		this.argsNumOfInput=argNum;
		this.originalUnitInEntryMethod=unit;
		this.originalArgsNum=argNum;
		signatureOfCurrentMethod=currentMethod;
		this.cfg=cfg;
		
		ValueBox left=null;
		ValueBox right=null;
		
		if(unit instanceof JIfStmt){
			left=unit.getUseBoxes().get(0);
			right=unit.getUseBoxes().get(1);
		}
		if(left==null || right==null){
			LogUtil.error("Check", "Check can not be inited with NULL : "+unit);
			return;
		}
		
		Body body=cfg.method.retrieveActiveBody();
		
		HashMap<Unit, ValueBox> leftTainted=DataFlowAnalyzer.introProcedural_findAllDefUnitsAndValueBoxes(body, unit, left);
		HashMap<Unit, ValueBox> rightTainted=DataFlowAnalyzer.introProcedural_findAllDefUnitsAndValueBoxes(body, unit, right);
		
		leftTaintedUnits.addAll(leftTainted.keySet());
		rightTaintedUnits.addAll(rightTainted.keySet());
		
	}
	
	public SecurityType getSecurityCheckType(){
		List<Unit> allTaintedUnits=new ArrayList<>();
		allTaintedUnits.addAll(leftTaintedUnits);
		allTaintedUnits.addAll(rightTaintedUnits);
		SecurityType type = new SecurityType(SecurityType.TypeDefault);
		
		for(Unit unit : allTaintedUnits){
			if(((Stmt)unit).containsInvokeExpr()){
				SootMethod method=((Stmt)unit).getInvokeExpr().getMethod();
				SecurityType methodType=SearchSecurityCheckType.getSecurityCheckType(method.getSignature());
				if(type.getLevel()<methodType.getLevel()){
					type=methodType;
				}
			}
		}
		return type;
	}
	
	public TerminationType getTerminationType(){
		List<Path> paths=cfg.getPathsFromUnit(unit,new HashSet<Node>());
		TerminationType type=new TerminationType(TerminationType.TerminationNot);
		for(Path path : paths){
			if(type.getLevel()<path.getTerminationType().getLevel()){
				type=path.getTerminationType();
			}
		}
		return type;
	}
	
	
	public boolean isTaintedByUidOrPid(){
		List<Unit> allTaintedUnits=new ArrayList<>();
		allTaintedUnits.addAll(leftTaintedUnits);
		allTaintedUnits.addAll(rightTaintedUnits);
		
		for(Unit unit : allTaintedUnits){
			if(((Stmt)unit).containsInvokeExpr()){
				SootMethod method=((Stmt)unit).getInvokeExpr().getMethod();
				if(SearchSecurityCheckType.getSecurityCheckType(method.getSignature()).equals(SearchSecurityCheckType.Type_Uid)
						||SearchSecurityCheckType.getSecurityCheckType(method.getSignature()).equals(SearchSecurityCheckType.Type_Pid)){
					return true;
				}
			}
		}
		return false;
	}
	public boolean isTaintedByUserId(){
		List<Unit> allTaintedUnits=new ArrayList<>();
		allTaintedUnits.addAll(leftTaintedUnits);
		allTaintedUnits.addAll(rightTaintedUnits);
		
		for(Unit unit : allTaintedUnits){
			if(((Stmt)unit).containsInvokeExpr()){
				SootMethod method=((Stmt)unit).getInvokeExpr().getMethod();
				if(SearchSecurityCheckType.getSecurityCheckType(method.getSignature()).equals(SearchSecurityCheckType.Type_UserId))
					return true;
			}
		}
		return false;
	}
	public boolean isTaintedByPermission(){
		List<Unit> allTaintedUnits=new ArrayList<>();
		allTaintedUnits.addAll(leftTaintedUnits);
		allTaintedUnits.addAll(rightTaintedUnits);
		
		for(Unit unit : allTaintedUnits){
			if(((Stmt)unit).containsInvokeExpr()){
				SootMethod method=((Stmt)unit).getInvokeExpr().getMethod();
				if(SearchSecurityCheckType.getSecurityCheckType(method.getSignature()).equals(SearchSecurityCheckType.Type_Permission_check)
						||SearchSecurityCheckType.getSecurityCheckType(method.getSignature()).equals(SearchSecurityCheckType.Type_Permission_enforce) )
					return true;
			}
		}
		return false;
	}
	
	public boolean isValidation(){
		if(STANDARD.equals(Standard_JIf)){
			return true;
		}
		else if(STANDARD.equals(Standard_Middle)){
			return true;
		}
		else if(STANDARD.equals(Standard_TerminationBranch)){
			return true;
		}
		else {
			LogUtil.error("Check", "Wrong Standard for Validation : "+STANDARD);
		}
		return false;
	}
	
	private boolean isValidationByHighStandard(){
		
		
		
		
		return false;
	}
	
}
