package com.minis.beans;

public interface BeanFactory {
    Object getBean(String name) throws BeansException;
	boolean containsBean(String name);
	//void registerBean(String beanName, Object obj);
	boolean isSingleton(String name);
	boolean isPrototype(String name);
	Class<?> getType(String name);

}
