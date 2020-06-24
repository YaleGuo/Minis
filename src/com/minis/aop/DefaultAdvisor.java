package com.minis.aop;

public class DefaultAdvisor implements Advisor{
	private MethodInterceptor methodInterceptor;

	public DefaultAdvisor() {
	}
	
	public void setMethodInterceptor(MethodInterceptor methodInterceptor) {
		this.methodInterceptor = methodInterceptor;
	}

	public MethodInterceptor getMethodInterceptor() {
		return this.methodInterceptor;
	}

}
