package com.minis.web.context;

import javax.servlet.ServletContext;

import com.minis.context.ApplicationContext;

public interface WebApplicationContext extends ApplicationContext {
	String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";

	ServletContext getServletContext();
	void setServletContext(ServletContext servletContext);
}
