package module.inputvalidation.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import dataflow.ClassAnalyzer;
import main.Memory;
import module.inputvalidation.validationtype.ValidationType;
import soot.SootMethod;
import util.LogUtil;
import util.TimeMeasurement;

public class SearchAllInputValidation {
	
	private HashSet<String> methodSignatures=new HashSet<>();
	private int threadSize=32;

	private String TAG = "SearchAllInputValidation";
	
	public SearchAllInputValidation(List<String> methodSignatures, int threadPoolSize){
		threadSize=threadPoolSize;
		
		HashSet<String> newSignatures=new HashSet<>();
		newSignatures.addAll(methodSignatures);
		while(!newSignatures.isEmpty()){
			HashSet<String> nextSignatures=new HashSet<>();
			this.methodSignatures.addAll(newSignatures);
			for(String caller : newSignatures){
				HashSet<String> callees=Memory.callerMethodSignatureMapCalleeMethodSignatures.get(caller);
				if(callees==null) continue;
				for(String callee : callees)
					if(!this.methodSignatures.contains(callee) && ClassAnalyzer.isValidMethodSignature(callee))
						nextSignatures.add(callee);
			}
			newSignatures=nextSignatures;
		}
		
//		HashSet<String> debugSig=new HashSet<>();
//		for(String signature : this.methodSignatures){
//			if(signature.contains("WindowManagerService"))
//				debugSig.add(signature);
//		}
//		this.methodSignatures=debugSig;
	
	}
	
	
	public HashMap<SootMethod, List<ValidationType>> start(){
		
		
		TimeMeasurement.show("Start SearchAllSecurityCheckMethod with Size : "+methodSignatures.size());
		ExecutorService executorService=Executors.newFixedThreadPool(threadSize);
		List<FutureTask<MethodAndValidations>> futureTasks=new ArrayList<>();
		for (String signature : methodSignatures) {
			for(int i = 1 ; i <= Memory.methodSignatureMapSootMethod.get(signature).getParameterCount(); i++) {
				ThreadForSearchInputValidation thread=new ThreadForSearchInputValidation(signature, i);
				FutureTask<MethodAndValidations> futureTask=new FutureTask<>(thread);
				futureTasks.add(futureTask);
				executorService.submit(futureTask);
			}
		}

		/*
		 * speed is too slow, so deprecated
		 * 
		for(int i=1;;i++){
			boolean newThread=false;
			for(String signature:methodSignatures){
				if(i>Memory.methodSignatureMapSootMethod.get(signature).getParameterCount())
					continue;
				ThreadForSearchInputValidation thread=new ThreadForSearchInputValidation(signature, i);
				newThread=true;
				FutureTask<MethodAndValidations> futureTask=new FutureTask<>(thread);
				futureTasks.add(futureTask);
				executorService.submit(futureTask);
			}
			if(!newThread)
				break;
		}
		*/
		List<MethodAndValidations> result=new ArrayList<>();
		
		for (FutureTask<MethodAndValidations> futureTask : futureTasks) {
			MethodAndValidations methodAndValidations;
			try {
					methodAndValidations = futureTask.get(300,TimeUnit.SECONDS);
				if (methodAndValidations != null) {
					result.add(methodAndValidations);
				} else {
					LogUtil.error("SearchAllSecurityCheckMethod", "FutureTask died.");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				LogUtil.exception("SearchAllInputValidation", "Time out.", e);
			}
		}
		LogUtil.debug(TAG, "after futuretask await shutdown");
		TimeMeasurement.show(" after futuretask await shutdown");
		
		executorService.shutdown();
		try {
			executorService.awaitTermination(600, TimeUnit.SECONDS);
			executorService.shutdownNow();
		} catch (InterruptedException e) {
			LogUtil.exception("SearchAllSecurityCheckMethod", "ExecutorService close exception.", e);
		}
		LogUtil.debug(TAG, "after pool await shutdown");
		TimeMeasurement.show("after pool await shutdown.");
		
		HashMap<SootMethod, List<ValidationType>> methodAndValidations=new HashMap<>();
		for(MethodAndValidations methodAndValidation:result){
			SootMethod method=Memory.methodSignatureMapSootMethod.get(methodAndValidation.signature);
			if(!methodAndValidations.containsKey(method))
				methodAndValidations.put(method,new ArrayList<ValidationType>());
			methodAndValidations.get(method).addAll(methodAndValidation.validations);
		}
		TimeMeasurement.show("Finish SearchAllInputValidation.");
		return methodAndValidations;
	}
	

}
