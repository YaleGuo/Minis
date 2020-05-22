package com.minis.beans;

public interface BeanFactory {
	Object getBean(String beanName) throws NoSuchBeanDefinitionException;
	void registerBeanDefinition(BeanDefinition bd);

}
