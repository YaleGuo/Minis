package com.minis.beans.factory.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.minis.beans.BeansException;
import com.minis.beans.factory.config.AbstractAutowireCapableBeanFactory;
import com.minis.beans.factory.config.BeanDefinition;
import com.minis.beans.factory.config.ConfigurableListableBeanFactory;

public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory 
					implements ConfigurableListableBeanFactory{

	@Override
	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	@Override
	public String[] getBeanDefinitionNames() {
		return (String[]) this.beanDefinitionNames.toArray();
	}

	@Override
	public String[] getBeanNamesForType(Class<?> type) {
		List<String> result = new ArrayList<>();

		for (String beanName : this.beanDefinitionNames) {
			boolean matchFound = false;
			BeanDefinition mbd = this.getBeanDefinition(beanName);
			Class<?> classToMatch = mbd.getClass();
			if (type.isAssignableFrom(classToMatch)) {
				matchFound = true;
			}
			else {
				matchFound = false;
			}

			if (matchFound) {
				result.add(beanName);
			}
		}
		return (String[]) result.toArray();

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		String[] beanNames = getBeanNamesForType(type);
		Map<String, T> result = new LinkedHashMap<>(beanNames.length);
		for (String beanName : beanNames) {
			Object beanInstance = getBean(beanName);
			result.put(beanName, (T) beanInstance);
		}
		return result;
	}

}
