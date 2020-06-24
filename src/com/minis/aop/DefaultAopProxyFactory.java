package com.minis.aop;

public class DefaultAopProxyFactory implements AopProxyFactory {

	@Override
	public AopProxy createAopProxy(Object target, Advisor advisor) {
		return new JdkDynamicAopProxy(target, advisor);
	}
}
