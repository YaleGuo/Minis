package com.minis.aop;

public interface AopProxyFactory {
	AopProxy createAopProxy(Object target,Advisor adviseor);
}
