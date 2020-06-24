package com.test.service;

import java.lang.reflect.Method;

import com.minis.aop.AfterReturningAdvice;

public class MyAfterAdvice implements AfterReturningAdvice{
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		System.out.println("----------my interceptor after method call----------");
	}

}
