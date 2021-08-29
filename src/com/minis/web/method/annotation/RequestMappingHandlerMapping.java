package com.minis.web.method.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import com.minis.beans.BeansException;
import com.minis.context.ApplicationContext;
import com.minis.context.ApplicationContextAware;
import com.minis.util.PatternMatchUtils;
import com.minis.web.bind.annotation.RequestMapping;
import com.minis.web.context.WebApplicationContext;
import com.minis.web.method.HandlerMethod;
import com.minis.web.servlet.HandlerMapping;

public class RequestMappingHandlerMapping implements HandlerMapping,ApplicationContextAware {
	ApplicationContext applicationContext;
	private MappingRegistry mappingRegistry = null;
	
	public RequestMappingHandlerMapping() {
	}
	
    protected void initMappings() {
    	Class<?> clz = null;
    	Object obj = null;
    	String[] controllerNames = this.applicationContext.getBeanDefinitionNames();
    	for (String controllerName : controllerNames) {
			try {
				clz = Class.forName(controllerName);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			try {
				obj = this.applicationContext.getBean(controllerName);
			} catch (BeansException e) {
				e.printStackTrace();
			}
    		Method[] methods = clz.getDeclaredMethods();
    		if(methods!=null){
    			for(Method method : methods){
    				boolean isRequestMapping = method.isAnnotationPresent(RequestMapping.class);
    				if (isRequestMapping){
    					String methodName = method.getName();
    					String urlmapping = method.getAnnotation(RequestMapping.class).value();
    					String requestMethod = method.getAnnotation(RequestMapping.class).method();
    					if (requestMethod.equals("")) {
    						requestMethod="GET";
    					}
    					String qualifiedName = requestMethod + ":" + urlmapping;

    					this.mappingRegistry.getUrlMappingNames().add(urlmapping);
    					this.mappingRegistry.getRequestMethods().add(requestMethod);
    					this.mappingRegistry.getQualifiedNames().add(qualifiedName);
    					
    					this.mappingRegistry.getMappingObjs().put(qualifiedName, obj);
    					this.mappingRegistry.getMappingMethods().put(qualifiedName, method);
    					this.mappingRegistry.getMappingMethodNames().put(qualifiedName, methodName);
    					this.mappingRegistry.getMappingClasses().put(qualifiedName, clz);
    					
    				}
    			}
    		}
    	}
    }


	@Override
	public HandlerMethod getHandler(HttpServletRequest request) throws Exception {
		if (this.mappingRegistry == null) { //to do initialization
			this.mappingRegistry = new MappingRegistry();
			initMappings();
		}
		
		for (int i=0; i<this.mappingRegistry.getQualifiedNames().size();i++) {
			System.out.println(this.mappingRegistry.getQualifiedNames().get(i));
		}
		
		
		String sPath = request.getServletPath();
		System.out.println("sPath:"+sPath);
		String sRequestMethod = request.getMethod();
		String qualifiedName = sRequestMethod+":"+sPath;
		System.out.println("qualifiedName:"+qualifiedName);
		
	    String sPattern = "";
		
		for (int i=0; i<this.mappingRegistry.getQualifiedNames().size();i++) {
		    if (PatternMatchUtils.URIMatch(this.mappingRegistry.getQualifiedNames().get(i), qualifiedName)) {
		        sPattern = 	this.mappingRegistry.getQualifiedNames().get(i);
		        break;
		    }		    
		}
		if (sPattern.equals("")) {
			return null;
		}

		System.out.println("sPattern:"+sPattern);
		
		Method method = this.mappingRegistry.getMappingMethods().get(sPattern);
		Object obj = this.mappingRegistry.getMappingObjs().get(sPattern);
		Class<?> clz = this.mappingRegistry.getMappingClasses().get(sPattern);
		String methodName = this.mappingRegistry.getMappingMethodNames().get(sPattern);
	
		HandlerMethod handlerMethod = new HandlerMethod(method, obj, clz, methodName);
		
		return handlerMethod;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
