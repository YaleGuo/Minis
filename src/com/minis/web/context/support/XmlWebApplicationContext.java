package com.minis.web.context.support;

import javax.servlet.ServletContext;
import com.minis.context.ClassPathXmlApplicationContext;
import com.minis.web.context.WebApplicationContext;

public class XmlWebApplicationContext 
					extends ClassPathXmlApplicationContext implements WebApplicationContext{
	private ServletContext servletContext;
	
	public XmlWebApplicationContext(String fileName) {
		super(fileName);
	}

	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}
