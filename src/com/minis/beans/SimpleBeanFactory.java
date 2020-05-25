package com.minis.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory,BeanDefinitionRegistry{
    private Map<String,BeanDefinition> beanDefinitionMap=new ConcurrentHashMap<>(256);
    private List<String> beanDefinitionNames=new ArrayList<>();

    public SimpleBeanFactory() {
    }

    public Object getBean(String beanName) throws BeansException{
        Object singleton = this.getSingleton(beanName);
        if (singleton == null) {
        		BeanDefinition bd = beanDefinitionMap.get(beanName);
            	singleton=createBean(bd);
				this.registerBean(beanName, singleton);
				
				if (bd.getInitMethodName() != null) {
					//init method
				}
        }
        if (singleton == null) {
        	throw new BeansException("bean is null.");
        }
        return singleton;
    }
	@Override
	public boolean containsBean(String name) {
		return containsSingleton(name);
	}

	public void registerBean(String beanName, Object obj) {
		this.registerSingleton(beanName, obj);
		
	}

	@Override
	public void registerBeanDefinition(String name, BeanDefinition bd) {
    	this.beanDefinitionMap.put(name,bd);
    	this.beanDefinitionNames.add(name);
    	if (!bd.isLazyInit()) {
        	try {
				getBean(name);
			} catch (BeansException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}

	@Override
	public void removeBeanDefinition(String name) {
		this.beanDefinitionMap.remove(name);
		this.beanDefinitionNames.remove(name);
		this.removeSingleton(name);
		
	}

	@Override
	public BeanDefinition getBeanDefinition(String name) {
		return this.beanDefinitionMap.get(name);
	}

	@Override
	public boolean containsBeanDefinition(String name) {
		return this.beanDefinitionMap.containsKey(name);
	}

	@Override
	public boolean isSingleton(String name) {
		return this.beanDefinitionMap.get(name).isSingleton();
	}

	@Override
	public boolean isPrototype(String name) {
		return this.beanDefinitionMap.get(name).isPrototype();
	}

	@Override
	public Class<?> getType(String name) {
		return this.beanDefinitionMap.get(name).getClass();
	}
	
	private Object createBean(BeanDefinition bd) {
		Class<?> clz = null;
		Object obj = null;
		Constructor<?> con = null;

		try {
    		clz = Class.forName(bd.getClassName());
    		
    		//handle constructor
    		ArgumentValues argumentValues = bd.getConstructorArgumentValues();
    		if (!argumentValues.isEmpty()) {
        		Class<?>[] paramTypes = new Class<?>[argumentValues.getArgumentCount()];
        		Object[] paramValues =   new Object[argumentValues.getArgumentCount()];  
    			for (int i=0; i<argumentValues.getArgumentCount(); i++) {
    				ArgumentValue argumentValue = argumentValues.getIndexedArgumentValue(i);
    				if ("String".equals(argumentValue.getType()) || "java.lang.String".equals(argumentValue.getType())) {
    					paramTypes[i] = String.class;
        				paramValues[i] = argumentValue.getValue();
    				}
    				else if ("Integer".equals(argumentValue.getType()) || "java.lang.Integer".equals(argumentValue.getType())) {
    					paramTypes[i] = Integer.class;
        				paramValues[i] = Integer.valueOf((String) argumentValue.getValue());
    				}
    				else if ("int".equals(argumentValue.getType())) {
    					paramTypes[i] = int.class;
        				paramValues[i] = Integer.valueOf((String) argumentValue.getValue()).intValue();
    				}
    				else {
    					paramTypes[i] = String.class;
        				paramValues[i] = argumentValue.getValue();    					
    				}
    			}
				try {
					con = clz.getConstructor(paramTypes);
					obj = con.newInstance(paramValues);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}  
    		}
    		else {
    			obj = clz.newInstance();
    		}

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		//handle properties
		PropertyValues propertyValues = bd.getPropertyValues();
		if (!propertyValues.isEmpty()) {
			for (int i=0; i<propertyValues.size(); i++) {
				PropertyValue propertyValue = propertyValues.getPropertyValueList().get(i);
				String pName = propertyValue.getName();
				String pType = propertyValue.getType();
    			Object pValue = propertyValue.getValue();
    			
    			Class<?>[] paramTypes = new Class<?>[1];    			
				if ("String".equals(pType) || "java.lang.String".equals(pType)) {
					paramTypes[0] = String.class;
				}
				else if ("Integer".equals(pType) || "java.lang.Integer".equals(pType)) {
					paramTypes[0] = Integer.class;
				}
				else if ("int".equals(pType)) {
					paramTypes[0] = int.class;
				}
				else {
					paramTypes[0] = String.class;
				}
				
				Object[] paramValues =   new Object[1];  
				paramValues[0] = pValue;

    			String methodName = "set" + pName.substring(0,1).toUpperCase() + pName.substring(1);
				    			
    			Method method = null;
				try {
					method = clz.getMethod(methodName, paramTypes);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
    			try {
					method.invoke(obj, paramValues);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
    			
			}
		}
		
		
		return obj;

	}
    
}
