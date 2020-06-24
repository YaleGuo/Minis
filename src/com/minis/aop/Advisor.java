package com.minis.aop;

public interface Advisor {
	MethodInterceptor getMethodInterceptor();
	void setMethodInterceptor(MethodInterceptor methodInterceptor);
}
