package util;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class TimableFuture<V> extends FutureTask<V>  {
	private int timeLimit;

	public TimableFuture(Callable<V> callable) {
		super(callable);
		// TODO Auto-generated constructor stub
	}

}
