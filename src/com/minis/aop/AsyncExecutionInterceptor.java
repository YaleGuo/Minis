package com.minis.aop;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.minis.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.minis.util.concurrent.ListenableFuture;

public class AsyncExecutionInterceptor implements MethodInterceptor{
	ThreadPoolTaskExecutor executor;
	
	public ThreadPoolTaskExecutor getExecutor() {
		return this.executor;
	}
	public void setExecutor(ThreadPoolTaskExecutor defaultExecutor) {
		this.executor = defaultExecutor;
	}
	
	public AsyncExecutionInterceptor() {
	}

	public Object invoke(final MethodInvocation invocation) throws Throwable {
		Callable<Object> task = () -> {
			try {
				Object result = invocation.proceed();
				if (result instanceof Future) {
					return ((Future<?>) result).get();
				}
			}
			catch (ExecutionException ex) {
				throw ex;
			}
			catch (Throwable ex) {
			}
			return null;
		};

		System.out.println("Async Interceptor.invoke().Execute method asynchronously. "  
			      + Thread.currentThread().getName()); 
		return doSubmit(task, executor, invocation.getMethod().getReturnType());
	}
	
	protected Object doSubmit(Callable<Object> task, ThreadPoolTaskExecutor executor, Class<?> returnType) {
		if (ListenableFuture.class.isAssignableFrom(returnType)) {
			System.out.println("Async dosubmit() listenableFuture. "  
				      + Thread.currentThread().getName()); 
			return executor.submitListenable(task);
		}
		else if (Future.class.isAssignableFrom(returnType)) {
			System.out.println("Async dosubmit() Future. "  
				      + Thread.currentThread().getName()); 
			return executor.submit(task);
		}
		else {
			System.out.println("Async dosubmit(). "  
				      + Thread.currentThread().getName()); 
			executor.submit(task);
			return null;
		}
	}

}

