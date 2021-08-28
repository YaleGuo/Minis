package com.minis.scheduling.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.minis.aop.framework.AopProxyFactory;
import com.minis.aop.framework.DefaultAopProxyFactory;
import com.minis.aop.framework.ProxyFactoryBean;
import com.minis.beans.BeansException;
import com.minis.beans.factory.BeanFactory;
import com.minis.beans.factory.BeanFactoryAware;
import com.minis.beans.factory.annotation.Autowired;
import com.minis.beans.factory.config.BeanPostProcessor;
import com.minis.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.minis.aop.Advisor;
import com.minis.aop.AsyncExecutionInterceptor;
import com.minis.aop.MethodInterceptor;

public class AsyncAnnotationBeanPostProcessor implements BeanPostProcessor,BeanFactoryAware{
	private BeanFactory beanFactory;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Object result = bean;
		
		Class<?> clazz = bean.getClass();
		Method[] methods = clazz.getDeclaredMethods();
		if(methods!=null){
			for(Method method : methods){
				boolean isAsync = method.isAnnotationPresent(Async.class);
	
				if(isAsync){
					System.out.println("AsyncAnnotationBeanPostProcessor is Async. "); 
					AopProxyFactory proxyFactory = new DefaultAopProxyFactory();
					ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
					Advisor advisor = (Advisor)beanFactory.getBean("asyncAnnotationAdvisor");
					MethodInterceptor methodInterceptor = (AsyncExecutionInterceptor)beanFactory.getBean("asyncExecutionInterceptor");
					advisor.setMethodInterceptor(methodInterceptor);
					proxyFactoryBean.setTarget(bean);
					proxyFactoryBean.setBeanFactory(beanFactory);
					proxyFactoryBean.setAopProxyFactory(proxyFactory);
					proxyFactoryBean.setInterceptorName("asyncAnnotationAdvisor");
					bean = proxyFactoryBean;
					return proxyFactoryBean;

				}
			}
		}
		
		return result;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}


	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

}
