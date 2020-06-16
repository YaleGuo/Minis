package com.minis.beans;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public abstract class AbstractPropertyAccessor extends PropertyEditorRegistrySupport{

	PropertyValues pvs;
	
	public AbstractPropertyAccessor() {
		super();

	}

	
	public void setPropertyValues(PropertyValues pvs) {
		this.pvs = pvs;
		for (PropertyValue pv : this.pvs.getPropertyValues()) {
			setPropertyValue(pv);
		}
	}
	
	public abstract void setPropertyValue(PropertyValue pv) ;

}
