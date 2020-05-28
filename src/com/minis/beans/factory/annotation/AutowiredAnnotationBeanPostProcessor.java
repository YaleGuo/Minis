package com.minis.beans.factory.annotation;

import java.lang.reflect.Field;

import com.minis.beans.BeansException;
import com.minis.beans.factory.BeanFactory;
import com.minis.beans.factory.config.BeanPostProcessor;

public class AutowiredAnnotationBeanPostProcessor implements BeanPostProcessor {
	private BeanFactory beanFactory;
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Object result = bean;
		
		Class<?> clazz = bean.getClass();
		Field[] fields = clazz.getDeclaredFields();
		if(fields!=null){
			for(Field field : fields){
				boolean isAutowired = field.isAnnotationPresent(Autowired.class);
				if(isAutowired){
					String fieldName = field.getName();
					Object autowiredObj = this.getBeanFactory().getBean(fieldName);
					try {
						field.setAccessible(true);
						field.set(bean, autowiredObj);
						System.out.println("autowire " + fieldName + " for bean " + beanName);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}

				}
			}
		}
		
		return result;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}


}
