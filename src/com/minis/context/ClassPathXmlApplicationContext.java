package com.minis.context;

import java.util.ArrayList;
import java.util.List;

import com.minis.beans.BeansException;
import com.minis.beans.factory.BeanFactory;
import com.minis.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import com.minis.beans.factory.config.AutowireCapableBeanFactory;
import com.minis.beans.factory.config.BeanFactoryPostProcessor;
import com.minis.beans.factory.support.AbstractBeanFactory;
import com.minis.beans.factory.xml.XmlBeanDefinitionReader;
import com.minis.core.ClassPathXmlResource;
import com.minis.core.Resource;

public class ClassPathXmlApplicationContext implements BeanFactory,ApplicationEventPublisher{
	AutowireCapableBeanFactory beanFactory;
	private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors =
			new ArrayList<BeanFactoryPostProcessor>();	

    public ClassPathXmlApplicationContext(String fileName){
    	this(fileName, true);
    }

    public ClassPathXmlApplicationContext(String fileName, boolean isRefresh){
    	Resource res = new ClassPathXmlResource(fileName);
    	AutowireCapableBeanFactory bf = new AutowireCapableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.loadBeanDefinitions(res);
        
        this.beanFactory = bf;
        
        if (isRefresh) {
            try {
				refresh();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (BeansException e) {
				e.printStackTrace();
			}
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
	
	public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
		return this.beanFactoryPostProcessors;
	}
	
	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
		this.beanFactoryPostProcessors.add(postProcessor);
	}
	
	public void refresh() throws BeansException, IllegalStateException {
		// Register bean processors that intercept bean creation.
		registerBeanPostProcessors(this.beanFactory);

		// Initialize other special beans in specific context subclasses.
		onRefresh();
	}

	private void registerBeanPostProcessors(AutowireCapableBeanFactory bf) {
		//if (supportAutowire) {
			bf.addBeanPostProcessor(new AutowiredAnnotationBeanPostProcessor());
		//}
	}

	private void onRefresh() {
		this.beanFactory.refresh();
	}
    
}
