package com.minis.aop.framework.autoproxy;

import com.minis.aop.PointcutAdvisor;
import com.minis.aop.framework.AopProxy;
import com.minis.aop.framework.AopProxyFactory;
import com.minis.aop.framework.DefaultAopProxyFactory;
import com.minis.aop.framework.ProxyFactoryBean;
import com.minis.beans.BeansException;
import com.minis.beans.factory.BeanFactory;
import com.minis.beans.factory.config.BeanPostProcessor;
import com.minis.util.PatternMatchUtils;

public class BeanNameAutoProxyCreator implements BeanPostProcessor{
	String pattern;
	private BeanFactory beanFactory;
	private AopProxyFactory aopProxyFactory;
	private String interceptorName;
	private PointcutAdvisor advisor;
	
	public BeanNameAutoProxyCreator() {
		this.aopProxyFactory = new DefaultAopProxyFactory();
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	
	public void setAdvisor(PointcutAdvisor advisor) {
		this.advisor = advisor;
	}
	
	public void setInterceptorName(String interceptorName) {
		this.interceptorName = interceptorName;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
System.out.println(" try to create proxy for : " + beanName);		
		if (isMatch(beanName, this.pattern)) {
System.out.println(beanName + "bean name matched, " + this.pattern + " create proxy for " + bean);		
//			initializeAdvisor();
//			Object ret = getSingletonInstance(bean);
//System.out.println(" created proxy  " + ret);	
//			bean = ret;
//			return ret;
//		}
//		return bean;
			ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
			proxyFactoryBean.setTarget(bean);
			proxyFactoryBean.setBeanFactory(beanFactory);
			proxyFactoryBean.setAopProxyFactory(aopProxyFactory);
			proxyFactoryBean.setInterceptorName(interceptorName);
//			Object ret = null;
//			try {
//				ret = proxyFactoryBean.getObject();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			bean = proxyFactoryBean;
			return proxyFactoryBean;
		}
		else {
			return bean;
		}

	
	}
	

	
	protected AopProxy createAopProxy(Object target) {
		return this.aopProxyFactory.createAopProxy(target,this.advisor);
	}
	protected Object getProxy(AopProxy aopProxy) {
		return aopProxy.getProxy();
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return null;
	}

	protected boolean isMatch(String beanName, String mappedName) {
System.out.println(" match?" + beanName + ":" +mappedName);		
		return PatternMatchUtils.simpleMatch(mappedName, beanName);
	}


}
