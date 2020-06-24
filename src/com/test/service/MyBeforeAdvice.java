package com.test.service;

import java.lang.reflect.Method;

import com.minis.aop.MethodBeforeAdvice;

public class MyBeforeAdvice implements MethodBeforeAdvice{

	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		System.out.println("----------my interceptor befor method call----------");
	}

}
