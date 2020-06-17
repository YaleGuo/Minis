package com.test;

import com.minis.beans.BeansException;
import com.minis.beans.factory.BeanFactory;
import com.minis.beans.factory.config.BeanFactoryPostProcessor;

public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor{

	@Override
	public void postProcessBeanFactory(BeanFactory beanFactory) throws BeansException {
		System.out.println(".........MyBeanFactoryPostProcessor...........");
		
	}

}
