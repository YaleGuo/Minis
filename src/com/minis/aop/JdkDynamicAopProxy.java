package com.minis.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
	Object target;
	PointcutAdvisor advisor;
	
	public JdkDynamicAopProxy(Object target, PointcutAdvisor advisor) {
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
		Class<?> targetClass = (target != null ? target.getClass() : null);
		
		if (this.advisor.getPointcut().getMethodMatcher().matches(method, targetClass)) {
			//if (method.getName().equals("doAction")) {
			MethodInterceptor interceptor = this.advisor.getMethodInterceptor();
			MethodInvocation invocation =
						new ReflectiveMethodInvocation(proxy, target, method, args, targetClass);

			return interceptor.invoke(invocation);
		}
		return null;
	}
}
