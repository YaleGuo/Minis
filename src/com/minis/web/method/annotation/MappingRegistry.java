package com.minis.web.method.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingRegistry {
    private List<String> urlMappingNames = new ArrayList<>();
    private List<String> requestMethods = new ArrayList<>();
    private List<String> qualifiedNames = new ArrayList<>();
    private Map<String,Object> mappingObjs = new HashMap<>();
    private Map<String,Method> mappingMethods = new HashMap<>();
    private Map<String,String> mappingMethodNames = new HashMap<>();
    private Map<String,Class<?>> mappingClasses = new HashMap<>();
    
	public List<String> getUrlMappingNames() {
		return urlMappingNames;
	}
	public void setUrlMappingNames(List<String> urlMappingNames) {
		this.urlMappingNames = urlMappingNames;
	}
	public Map<String,Object> getMappingObjs() {
		return mappingObjs;
	}
	public void setMappingObjs(Map<String,Object> mappingObjs) {
		this.mappingObjs = mappingObjs;
	}
	public Map<String,Method> getMappingMethods() {
		return mappingMethods;
	}
	public void setMappingMethods(Map<String,Method> mappingMethods) {
		this.mappingMethods = mappingMethods;
	}
	public Map<String,Class<?>> getMappingClasses() {
		return mappingClasses;
	}
	public void setMappingClasses(Map<String,Class<?>> mappingClasses) {
		this.mappingClasses = mappingClasses;
	}
	public Map<String,String> getMappingMethodNames() {
		return mappingMethodNames;
	}
	public void setMappingMethodNames(Map<String,String> mappingMethodNames) {
		this.mappingMethodNames = mappingMethodNames;
	}
	public List<String> getRequestMethods() {
		return requestMethods;
	}
	public void setRequestMethods(List<String> requestMethods) {
		this.requestMethods = requestMethods;
	}
	public List<String> getQualifiedNames() {
		return qualifiedNames;
	}
	public void setQualifiedNames(List<String> qualifiedNames) {
		this.qualifiedNames = qualifiedNames;
	}

}
