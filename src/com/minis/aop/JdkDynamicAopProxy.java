package com.minis.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
	Object target;
	
	public JdkDynamicAopProxy(Object target) {
		this.target = target;
	}


	@Override
	public Object getProxy() {
		System.out.println("----------Proxy new psroxy instance for  ---------"+target);		
		System.out.println("----------Proxy new psroxy instance  classloader ---------"+JdkDynamicAopProxy.class.getClassLoader());
		System.out.println("----------Proxy new psroxy instance  interfaces  ---------"+target.getClass().getInterfaces());
		
		Object obj = Proxy.newProxyInstance(JdkDynamicAopProxy.class.getClassLoader(), target.getClass().getInterfaces(), this);
		System.out.println("----------Proxy new psroxy instance created r ---------"+obj);		
		return obj;
	}



	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("doAction")) {
			 System.out.println("-----before call real object, dynamic proxy........");
			 return method.invoke(target, args); 
		}
		return null;

	}


}
