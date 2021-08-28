package com.minis.aop.framework.adapter;

import com.minis.aop.MethodBeforeAdvice;
import com.minis.aop.MethodInterceptor;
import com.minis.aop.MethodInvocation;

public class MethodBeforeAdviceInterceptor implements MethodInterceptor {
	private final MethodBeforeAdvice advice;

	public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
		this.advice = advice;
	}

	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		this.advice.before(mi.getMethod(), mi.getArguments(), mi.getThis());
		return mi.proceed();
	}
}
