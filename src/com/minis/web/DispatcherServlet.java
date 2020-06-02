package com.minis.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.minis.test.HelloWorldBean;

/**
 * Servlet implementation class DispatcherServlet
 */
public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private String sContextConfigLocation;
    private Map<String,MappingValue> mappingValues;
    private Map<String,Class<?>> mappingClz = new HashMap<>();
    private Map<String,Object> mappingObjs = new HashMap<>();

    public DispatcherServlet() {
        super();
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
    	
        sContextConfigLocation = config.getInitParameter("contextConfigLocation");
        
        URL xmlPath = null;
		try {
			xmlPath = this.getServletContext().getResource(sContextConfigLocation);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		Resource rs = new ClassPathXmlResource(xmlPath);
        XmlConfigReader reader = new XmlConfigReader();
        mappingValues = reader.loadConfig(rs);
        Refresh();
        
    }
    
    protected void Refresh() {
    	for (Map.Entry<String,MappingValue> entry : mappingValues.entrySet()) {
    		String id = entry.getKey();
    		String className = entry.getValue().getClz();
    		Object obj = null;
    		Class<?> clz = null;
			try {
				clz = Class.forName(className);
				obj = clz.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mappingClz.put(id, clz);
    		mappingObjs.put(id, obj);
    	}
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sPath = request.getServletPath();
		if (this.mappingValues.get(sPath) == null) {
			return;
		}
		
		Class<?> clz = this.mappingClz.get(sPath);
		Object obj = this.mappingObjs.get(sPath);
		String methodName = this.mappingValues.get(sPath).getMethod();
		Object objResult = null;
		try {
			Method method = clz.getMethod(methodName);
			objResult = method.invoke(obj);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		response.getWriter().append(objResult.toString());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
