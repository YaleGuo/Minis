package com.minis.web.servlet;

import java.lang.reflect.Method;

public class HandlerMethod {
	private  Object bean;
	private  Class<?> beanType;
	private  Method method;
	private  Class<?> returnType;
	private  String description;
	private  String className;
	private  String methodName;
	
	public HandlerMethod(Method method, Object obj, Class<?> clz, String methodName) {
		this.method = method;
		this.bean = obj;
		this.beanType = clz;
		this.methodName = methodName;
		
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object getBean() {
		return bean;
	}

	public void setBean(Object bean) {
		this.bean = bean;
	}

	public Class<?> getReturnType() {
		return returnType;
	}

	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}

	public Class<?> getBeanType() {
		return beanType;
	}

	public void setBeanType(Class<?> beanType) {
		this.beanType = beanType;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
