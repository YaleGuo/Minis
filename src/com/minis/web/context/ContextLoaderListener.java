package com.minis.web.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.minis.web.context.support.XmlWebApplicationContext;

public class ContextLoaderListener implements ServletContextListener {
	public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";
	private WebApplicationContext context;
	
	public ContextLoaderListener() {
	}
	
	public ContextLoaderListener(WebApplicationContext context) {
		this.context = context;
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		initWebApplicationContext(event.getServletContext());
	}

	private void initWebApplicationContext(ServletContext servletContext) {
		String sContextLocation = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
	
		WebApplicationContext wac = new XmlWebApplicationContext(sContextLocation);
		wac.setServletContext(servletContext);
		this.context = wac;
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.context);
	}
	

}
