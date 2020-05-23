package com.minis.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory{
    private Map<String,BeanDefinition> beanDefinitions=new ConcurrentHashMap<>(256);

    public SimpleBeanFactory() {
    }

    public Object getBean(String beanName) throws BeansException{
        Object singleton = this.getSingleton(beanName);
        if (singleton == null) {
        		BeanDefinition bd = beanDefinitions.get(beanName);
        		if (bd == null) {
        			throw new BeansException("no bean");
        		}
        		try {
            		singleton=Class.forName(bd.getClassName()).newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				this.registerSingleton(beanName, singleton);
        }
        if (singleton == null) {
        	throw new BeansException("bean is null.");
        }
        return singleton;
    }
    public void registerBeanDefinition(BeanDefinition bd){
    	this.beanDefinitions.put(bd.getId(),bd);
    }

	@Override
	public boolean containsBean(String name) {
		return containsSingleton(name);
	}

	@Override
	public void registerBean(String beanName, Object obj) {
		this.registerSingleton(beanName, obj);
		
	}
    
}
