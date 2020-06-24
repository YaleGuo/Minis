package com.minis.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
	Object target;
	Advisor advisor;
	
	public JdkDynamicAopProxy(Object target, Advisor advisor) {
		this.target = target;
		this.advisor = advisor;
	}

	@Override
	public Object getProxy() {
		Object obj = Proxy.newProxyInstance(JdkDynamicAopProxy.class.getClassLoader(), target.getClass().getInterfaces(), this);
		return obj;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("doAction")) {
		
			Class<?> targetClass = (target != null ? target.getClass() : null);
			MethodInterceptor interceptor = this.advisor.getMethodInterceptor();
			MethodInvocation invocation =
						new ReflectiveMethodInvocation(proxy, target, method, args, targetClass);

			return interceptor.invoke(invocation);
		}
		return null;
	}
}
