package module.securitycheckmethod.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import dataflow.DataFlowAnalyzer;
import main.Memory;
import soot.SootMethod;

public class ThreadForSearchCallerMethodsCanReturnCallee implements Callable<List<String>> {
	
	
	private String calleeMethodSignature;
	
	public ThreadForSearchCallerMethodsCanReturnCallee(String calleeSignature) {
		calleeMethodSignature=calleeSignature;
	}

	@Override
	public List<String> call() throws Exception {
		List<String> result=new ArrayList<>();
		Set<String> callerMethodSignatures=Memory.calleeMethodSignatureMapCallerMethodSignatures.get(calleeMethodSignature);
		if(callerMethodSignatures==null) return result;
		
		for(String callerSig:callerMethodSignatures){
			SootMethod caller=Memory.methodSignatureMapSootMethod.get(callerSig);
			SootMethod callee=Memory.methodSignatureMapSootMethod.get(calleeMethodSignature);
			if(callee!=null && caller!=null
					&& DataFlowAnalyzer.isCalleeCanBeReturnedByCaller(caller, callee)){
				result.add(callerSig);
			}
		}
		return result;
	}

}
