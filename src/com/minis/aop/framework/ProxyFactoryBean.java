package com.minis.aop.framework;

import com.minis.aop.Advisor;
import com.minis.aop.PointcutAdvisor;
import com.minis.beans.BeansException;
import com.minis.beans.factory.BeanFactory;
import com.minis.beans.factory.BeanFactoryAware;
import com.minis.beans.factory.FactoryBean;
import com.minis.util.ClassUtils;

public class ProxyFactoryBean  implements FactoryBean<Object>,BeanFactoryAware {
	private BeanFactory beanFactory;
	private AopProxyFactory aopProxyFactory;
	private String interceptorName;
	private String targetName;
	private Object target;
	private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();
	private Object singletonInstance;
	private Advisor advisor;

	public ProxyFactoryBean() {
		this.aopProxyFactory = new DefaultAopProxyFactory();
	}
	
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void setAopProxyFactory(AopProxyFactory aopProxyFactory) {
		this.aopProxyFactory = aopProxyFactory;
	}
	public AopProxyFactory getAopProxyFactory() {
		return this.aopProxyFactory;
	}

	public void setAdvisor(PointcutAdvisor advisor) {
		this.advisor = advisor;
	}
	
	public void setInterceptorName(String interceptorName) {
		this.interceptorName = interceptorName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	public Object getTarget() {
		return target;
	}
	public void setTarget(Object target) {
		this.target = target;
	}

	@Override
	public Object getObject() throws Exception {
		initializeAdvisor();
		return getSingletonInstance();
	}
	
	private synchronized void initializeAdvisor() {
		Object advice = null;
//		MethodInterceptor mi = null;
		try {
			advice = this.beanFactory.getBean(this.interceptorName);
		} catch (BeansException e) {
			e.printStackTrace();
		}
		this.advisor = (Advisor) advice;
//		if (advice instanceof BeforeAdvice) {
//			mi = new MethodBeforeAdviceInterceptor((MethodBeforeAdvice)advice);
//		}
//		else if (advice instanceof AfterAdvice){
//			mi = new AfterReturningAdviceInterceptor((AfterReturningAdvice)advice);
//		}
//		else if (advice instanceof MethodInterceptor) {
//			mi = (MethodInterceptor)advice;
//		}
		
		//advisor = new NameMatchMethodPointcutAdvisor((Advice)advice);
		//advisor.setMethodInterceptor(mi);

	}

	private synchronized Object getSingletonInstance() {
		if (this.singletonInstance == null) {
			this.singletonInstance = getProxy(createAopProxy());
		}
		return this.singletonInstance;
	}
	protected AopProxy createAopProxy() {
		return getAopProxyFactory().createAopProxy(target,this.advisor);
	}
	protected Object getProxy(AopProxy aopProxy) {
		return aopProxy.getProxy();
	}
	
	@Override
	public Class<?> getObjectType() {
		return null;
	}

}
