package com.minis.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleBeanFactory implements BeanFactory{
    private List<BeanDefinition> beanDefinitions=new ArrayList<>();
    private List<String> beanNames=new ArrayList<>();
    private Map<String, Object> singletons =new HashMap<>();

    public SimpleBeanFactory() {
    }

    public Object getBean(String beanName) throws NoSuchBeanDefinitionException{
        Object singleton = singletons.get(beanName);
        if (singleton == null) {
        	int i = beanNames.indexOf(beanName);
        	if (i == -1) {
        		throw new NoSuchBeanDefinitionException();
        	}
        	else {
        		BeanDefinition bd = beanDefinitions.get(i);
        		try {
            		singleton=Class.forName(bd.getClassName()).newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				singletons.put(bd.getId(),singleton);
        	}       	
        }
        return singleton;
    }
    public void registerBeanDefinition(BeanDefinition bd){
    	this.beanDefinitions.add(bd);
    	this.beanNames.add(bd.getId());
    }
    
}
