package com.minis.aop.framework;

import com.minis.aop.Advisor;
import com.minis.aop.PointcutAdvisor;

public interface AopProxyFactory {
	AopProxy createAopProxy(Object target, Advisor adviseor);
}
