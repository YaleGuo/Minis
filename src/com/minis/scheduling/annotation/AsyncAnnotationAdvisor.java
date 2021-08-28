package com.minis.scheduling.annotation;

import com.minis.aop.Advice;
import com.minis.aop.Advisor;
import com.minis.aop.AsyncExecutionInterceptor;
import com.minis.aop.MethodInterceptor;

public class AsyncAnnotationAdvisor  implements Advisor{
	MethodInterceptor methodInterceptor;

	@Override
	public MethodInterceptor getMethodInterceptor() {
		return this.methodInterceptor;
	}

	@Override
	public void setMethodInterceptor(MethodInterceptor methodInterceptor) {
		this.methodInterceptor= methodInterceptor;
	}

	@Override
	public Advice getAdvice() {
		// TODO Auto-generated method stub
		return null;
	}

}
