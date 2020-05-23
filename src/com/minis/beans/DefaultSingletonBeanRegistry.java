package com.minis.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    protected List<String> beanNames=new ArrayList<>();
    protected Map<String, Object> singletons =new ConcurrentHashMap<>(256);

	@Override
	public void registerSingleton(String beanName, Object singletonObject) {
		synchronized(this.singletons) {
	    	this.singletons.put(beanName, singletonObject);
	    	this.beanNames.add(beanName);
		}
	}

	@Override
	public Object getSingleton(String beanName) {
		return this.singletons.get(beanName);
	}

	@Override
	public boolean containsSingleton(String beanName) {
		return this.singletons.containsKey(beanName);
	}

	@Override
	public String[] getSingletonNames() {
		return (String[]) this.beanNames.toArray();
	}
	
	protected void removeSingleton(String beanName) {
	    synchronized (this.singletons) {
		    this.singletons.remove(beanName);
		    this.beanNames.remove(beanName);
	    }
	}

}
