package module.securitycheckmethod.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cfg.SearchSwappedClass;
import main.Memory;
import util.LogUtil;
import util.StringUtil;

public class ThreadForSearchSecurityCheckMethods implements Callable<TypeAndMethodNames> {
	
	private List<String> methodNames;
	
	private String type;
	private int threadSize=1;

	public ThreadForSearchSecurityCheckMethods(int size, TypeAndMethodNames typedSecurityCheckMethodNames) {
		super();
		this.methodNames = typedSecurityCheckMethodNames.methodNames;
		this.type = typedSecurityCheckMethodNames.type;
		this.threadSize=size;
	}

	public TypeAndMethodNames call() {

		String namesForType = StringUtil.join(methodNames, ";");

		System.out.println("names is " + type + "\n" + namesForType);
		
		List<String> calleeSignatures=new ArrayList<>();
		for(String name : methodNames){
			Set<String> signatures=Memory.methodNameMapMethodSignatures.get(name);
			if(signatures!=null)
				calleeSignatures.addAll(signatures);
		}
		
		methodNames.clear();
		List<String> methodSignatures=new ArrayList<>();
		int deep=1;
		while(!calleeSignatures.isEmpty()){
			
			ExecutorService executorService=Executors.newFixedThreadPool(threadSize);
			List<FutureTask<List<String>>> futureTasks=new ArrayList<>();
			List<String> taintedCallerSignatures=new ArrayList<>();
			
			for(String callee:calleeSignatures){
				if(methodSignatures.contains(callee))
					continue;
				ThreadForSearchCallerMethodsCanReturnCallee thread=new ThreadForSearchCallerMethodsCanReturnCallee(callee);
				FutureTask<List<String>> futureTask=new FutureTask<>(thread);
				futureTasks.add(futureTask);
				executorService.submit(futureTask);
			}
			
			for(FutureTask<List<String>> futureTask : futureTasks){
				List<String> threadResult;
				try {
					threadResult = futureTask.get(60,TimeUnit.SECONDS);
					if (threadResult != null) {
						taintedCallerSignatures.addAll(threadResult);
					} else {
						LogUtil.error("ConcurrentSearchSecurityCheckMethods", "FutureTask died : "+type+" : "+(deep-1));
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch(TimeoutException e){
					LogUtil.error("ConcurrentSearchSecurityCheckMethods", "FutureTask Timeout : "+type+" : "+(deep-1));
				}
			}
			for(String signature : calleeSignatures){
				if(!SearchSwappedClass.isGetOrWrap(signature))
					methodSignatures.add(signature);
			}
			calleeSignatures=taintedCallerSignatures;
		}
		
		return new TypeAndMethodNames(type, methodSignatures);

	}

}
