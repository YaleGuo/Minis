package com.minis.context;

import com.minis.beans.BeanFactory;
import com.minis.beans.BeansException;
import com.minis.beans.SimpleBeanFactory;
import com.minis.beans.XmlBeanDefinitionReader;
import com.minis.core.ClassPathXmlResource;
import com.minis.core.Resource;

public class ClassPathXmlApplicationContext implements BeanFactory,ApplicationEventPublisher{
	SimpleBeanFactory beanFactory;

    public ClassPathXmlApplicationContext(String fileName){
    	this(fileName, true);
    }

    public ClassPathXmlApplicationContext(String fileName, boolean isRefresh){
    	Resource res = new ClassPathXmlResource(fileName);
    	SimpleBeanFactory bf = new SimpleBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.loadBeanDefinitions(res);
        this.beanFactory = bf;
        
        if (isRefresh) {
        	this.beanFactory.refresh();
        }
    }
    
	@Override
	public Object getBean(String beanName) throws BeansException {
		return this.beanFactory.getBean(beanName);
	}

	@Override
	public boolean containsBean(String name) {
		return this.beanFactory.containsBean(name);
	}

	public void registerBean(String beanName, Object obj) {
		this.beanFactory.registerBean(beanName, obj);		
	}

	@Override
	public void publishEvent(ApplicationEvent event) {
	}

	@Override
	public boolean isSingleton(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrototype(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<?> getType(String name) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
