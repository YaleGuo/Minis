package com.minis.web.bind;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.minis.beans.AbstractPropertyAccessor;
import com.minis.beans.BeanWrapperImpl;
import com.minis.beans.PropertyEditor;
import com.minis.beans.PropertyValues;
import com.minis.util.WebUtils;

public class WebDataBinder {
	private Object target;
	private Class<?> clz;

	private String objectName;
	AbstractPropertyAccessor propertyAccessor;
	
	public WebDataBinder(Object target) {
		this(target,"");
	}
	public WebDataBinder(Object target, String targetName) {
		this.target = target;
		this.objectName = targetName;
		this.clz = this.target.getClass();
		this.propertyAccessor = new BeanWrapperImpl(this.target);
	}
	
	public void bind(HttpServletRequest request) {
		PropertyValues mpvs = assignParameters(request);
		addBindValues(mpvs, request);
		doBind(mpvs);
	}
	
	private void doBind(PropertyValues mpvs) {
		applyPropertyValues(mpvs);
		
	}
	
	protected void applyPropertyValues(PropertyValues mpvs) {
		getPropertyAccessor().setPropertyValues(mpvs);
	}
	
	protected AbstractPropertyAccessor getPropertyAccessor() {
		return this.propertyAccessor;
	}
	
	private PropertyValues assignParameters(HttpServletRequest request) {
		Map<String,Object> map = WebUtils.getParametersStartingWith(request, "");
		
		return new PropertyValues(map);
	}
	
	public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
		getPropertyAccessor().registerCustomEditor(requiredType, propertyEditor);
	}
	
	protected void addBindValues(PropertyValues mpvs, HttpServletRequest request) {
	}

}
