package module.inputvalidation.concurrent;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import cfg.CFG;
import main.Memory;
import module.inputvalidation.SearchInputValidation;
import module.inputvalidation.validationtype.Check;
import module.inputvalidation.validationtype.Enforce;
import module.inputvalidation.validationtype.ValidationType;
import soot.SootMethod;
import util.LogUtil;
import util.TimeMeasurement;

public class ThreadForSearchInputValidation implements Callable<MethodAndValidations>{
	
	private String methodSignature;
	private int argsNum;
	
	public ThreadForSearchInputValidation(String signature,int argsNum) {
		if(Memory.swappedMethodSigntureMapActualMethodSignature.keySet().contains(signature))
			signature=Memory.swappedMethodSigntureMapActualMethodSignature.get(signature);
		methodSignature=signature;
		this.argsNum=argsNum;
	}

	@Override
	public MethodAndValidations call() throws Exception {
		
		LogUtil.debug(methodSignature, "Start with : "+argsNum);

		SootMethod method=Memory.methodSignatureMapSootMethod.get(methodSignature);
		if(method==null)
			return new MethodAndValidations(methodSignature, new ArrayList<ValidationType>());
		if(argsNum>method.getParameterCount()){
			LogUtil.debug("ThreadForSearchInputValidation", "Wrong argsNum : "+argsNum+" : in method : "+methodSignature);
			return new MethodAndValidations(methodSignature, new ArrayList<ValidationType>());
		}
		List<ValidationType> validationTypes=SearchInputValidation.searchValidationForInputInMethod(CFG.getCFG(method), argsNum);
		
//		boolean canTransform=true;
//	
//		List<ValidationType> checks=new ArrayList<>();
//		
//		int deep=1;
//		while(canTransform){
//			LogUtil.info4Thread(methodSignature, "deep : "+deep+++" with : "+validationTypes.size());
//			List<ValidationType> enforces=new ArrayList<>();
//			for(ValidationType validationType:validationTypes){
//				if(validationType instanceof Enforce){
//				//	enforces.add(validationType);
//				//	LogUtil.info4Thread(methodSignature, "Enforce : "+validationType.unit+" with : "+validationType.argsNumOfInput);
//				}
//				if(validationType instanceof Check){
//				//	checks.add(validationType);
//				//	LogUtil.info4Thread(methodSignature, "Check : "+validationType.unit+" with : "+validationType.argsNumOfInput);
//				}
//			}
//			canTransform=!enforces.isEmpty();
//			validationTypes.clear();
//			for(ValidationType enforce : enforces){
//					validationTypes.addAll(((Enforce)enforce).transform());
//			}
//		}
		
		LogUtil.debug(methodSignature, "End with : "+validationTypes.size());
		return new MethodAndValidations(methodSignature, validationTypes);
	}
	
	
	
	

}
