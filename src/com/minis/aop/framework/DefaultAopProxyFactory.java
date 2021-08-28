package com.minis.aop.framework;

import com.minis.aop.Advisor;
import com.minis.aop.PointcutAdvisor;

public class DefaultAopProxyFactory implements AopProxyFactory {

	@Override
	public AopProxy createAopProxy(Object target, Advisor advisor) {
		return new JdkDynamicAopProxy(target, advisor);
	}
}
