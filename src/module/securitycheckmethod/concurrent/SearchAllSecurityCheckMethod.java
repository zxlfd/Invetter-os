package module.securitycheckmethod.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
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
import dataflow.ClassAnalyzer;
import dataflow.DataFlowAnalyzer;
import main.Common;
import main.Memory;
import module.securitycheckmethod.SearchSecurityCheckType;
import soot.SootMethod;
import util.LogUtil;
import util.StringUtil;

import util.TimeMeasurement;

public class SearchAllSecurityCheckMethod {
	
	private int size;
	
	private List<TypeAndMethodNames> seedsList=new ArrayList<TypeAndMethodNames>();
	
	public SearchAllSecurityCheckMethod(int size){
		this.size=size;
	}
	
	public void add(String type,List<String> seeds){
		seedsList.add(new TypeAndMethodNames(type, seeds));
	}
	
	public HashMap<String, List<String>> start(){
		if(seedsList.size()!=size){
			LogUtil.error("SearchAllSecurityCheckMethod", "seedMaps has a wrong size : "+seedsList.size()+" != "+size);
			return null;
		}
		TimeMeasurement.show("Start SearchAllSecurityCheckMethod ...");
		ExecutorService executorService=Executors.newFixedThreadPool(size);
		List<FutureTask<TypeAndMethodNames>> futureTasks=new ArrayList<>();
		for(TypeAndMethodNames seeds : seedsList){
			ThreadForSearchSecurityCheckMethods thread=new ThreadForSearchSecurityCheckMethods((Common.ThreadSize-size)/size,seeds);
			FutureTask<TypeAndMethodNames> futureTask=new FutureTask<>(thread);
			futureTasks.add(futureTask);
			executorService.submit(futureTask);
		}
		
		List<TypeAndMethodNames> result = new ArrayList<>();
		for (FutureTask<TypeAndMethodNames> futureTask : futureTasks) {
			TypeAndMethodNames typedSecurityCheckMethodNames;
			try {
				typedSecurityCheckMethodNames = futureTask.get();
				if (typedSecurityCheckMethodNames != null) {
					result.add(typedSecurityCheckMethodNames);
					TimeMeasurement.show(typedSecurityCheckMethodNames.type+" has finished.");
				} else {
					LogUtil.error("SearchAllSecurityCheckMethod", "FutureTask died.");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(7200, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LogUtil.exception("SearchAllSecurityCheckMethod", "ExecutorService close exception.", e);
		}
		
		HashMap<String, List<String>> typedMethodNameMaps=new HashMap<>();
		
		for(TypeAndMethodNames typedSecurityCheckMethodNames:result){
			typedMethodNameMaps.put(typedSecurityCheckMethodNames.type, typedSecurityCheckMethodNames.methodNames);
		}
		
		TimeMeasurement.show("Finish SearchAllSecurityCheckMethod.");
		return typedMethodNameMaps;
	}
	

	

}
