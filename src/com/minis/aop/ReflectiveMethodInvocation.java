package com.minis.aop;

import java.lang.reflect.Method;

public class ReflectiveMethodInvocation implements MethodInvocation{
	protected final Object proxy;
	protected final Object target;
	protected final Method method;
	protected Object[] arguments;
	private Class<?> targetClass;
	//protected MethodInterceptor methodInterceptor;

	protected ReflectiveMethodInvocation(
			Object proxy,  Object target, Method method,  Object[] arguments,
			 Class<?> targetClass) {

		this.proxy = proxy;
		this.target = target;
		this.targetClass = targetClass;
		this.method = method;
		this.arguments = arguments;
		//this.methodInterceptor = methodInterceptor;
	}

	public final Object getProxy() {
		return this.proxy;
	}

	public final Object getThis() {
		return this.target;
	}

	public final Method getMethod() {
		return this.method;
	}

	public final Object[] getArguments() {
		return this.arguments;
	}

	public void setArguments(Object... arguments) {
		this.arguments = arguments;
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(Class<?> targetClass) {
		this.targetClass = targetClass;
	}
	
	public Object proceed() throws Throwable {
		return this.method.invoke(this.target, this.arguments);
	}

}

